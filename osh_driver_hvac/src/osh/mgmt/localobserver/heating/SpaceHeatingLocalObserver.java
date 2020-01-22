package osh.mgmt.localobserver.heating;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.HotWaterDemandObserverExchange;
import osh.hal.exchange.prediction.WaterDemandPredictionExchange;
import osh.mgmt.ipp.HotWaterDemandNonControllableIPP;
import osh.mgmt.localobserver.ThermalDemandLocalObserver;
import osh.utils.time.TimeConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class SpaceHeatingLocalObserver
        extends ThermalDemandLocalObserver {

    private float weightForOtherWeekday;
    private float weightForSameWeekday;
    private int pastDaysPrediction;

    private List<SparseLoadProfile> lastDayProfiles = new ArrayList<>();

    private SparseLoadProfile lastDayProfile;
    private SparseLoadProfile predictedWaterDemand;

    private int hotWaterPower;
    private long timeFromMidnight;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public SpaceHeatingLocalObserver(IOSHOC osh) {
        super(osh);
        //no profile, yet
        this.lastDayProfile = new SparseLoadProfile();
        //start with an empty prediction
        this.predictedWaterDemand = this.lastDayProfile;
    }


    @Override
    public void onDeviceStateUpdate() {
        IHALExchange hx = this.getObserverDataObject();

        if (hx instanceof HotWaterDemandObserverExchange) {
            HotWaterDemandObserverExchange ox = (HotWaterDemandObserverExchange) hx;
            this.hotWaterPower = ox.getHotWaterPower();

            long now = this.getTimer().getUnixTime();

            // set current power state
            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getUUID(),
                    now,
                    DeviceTypes.SPACEHEATING);
            cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, this.hotWaterPower);
            this.getOCRegistry().publish(
                    CommodityPowerStateExchange.class,
                    this,
                    cpse);

            long lastTimeFromMidnight = this.timeFromMidnight;
            this.timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);


            this.monitorLoad();

            boolean firstDay = this.getTimer().getUnixTime() - this.getTimer().getUnixTimeAtStart() < 86400;

            if (firstDay || lastTimeFromMidnight > this.timeFromMidnight) {
                //a new day has begun...
                this.sendIPP();
            }
            if (lastTimeFromMidnight <= this.timeFromMidnight && now % 3600 == 0) {
                double predVal = this.predictedWaterDemand.getLoadAt(Commodity.HEATINGHOTWATERPOWER, this.timeFromMidnight);

                if ((predVal != 0 && (this.hotWaterPower / predVal > 1.25
                        || this.hotWaterPower / predVal < 0.75))
                        || (predVal == 0 && this.hotWaterPower != 0)) {
                    //only using the actual value for the next hour, restore the predicted value if there is no other value set in t+3600
                    int oldVal = this.predictedWaterDemand.getLoadAt(Commodity.HEATINGHOTWATERPOWER, this.timeFromMidnight);
                    Long nextLoadChange = this.predictedWaterDemand.getNextLoadChange(Commodity.HEATINGHOTWATERPOWER, this.timeFromMidnight);

                    if ((nextLoadChange != null && nextLoadChange > this.timeFromMidnight + 3600)
                            || (nextLoadChange == null && this.predictedWaterDemand.getEndingTimeOfProfile() > this.timeFromMidnight + 3600)) {
                        this.predictedWaterDemand.setLoad(Commodity.HEATINGHOTWATERPOWER, this.timeFromMidnight + 3600, oldVal);
                    }

                    this.predictedWaterDemand.setLoad(Commodity.HEATINGHOTWATERPOWER, this.timeFromMidnight, this.hotWaterPower);
                    this.sendIPP();
                }
            }
        } else if (hx instanceof WaterDemandPredictionExchange) {
            WaterDemandPredictionExchange _pred = (WaterDemandPredictionExchange) hx;

            this.lastDayProfiles = _pred.getPredictions();
            this.pastDaysPrediction = _pred.getPastDaysPrediction();
            this.weightForOtherWeekday = _pred.getWeightForOtherWeekday();
            this.weightForSameWeekday = _pred.getWeightForSameWeekday();
        } else if (hx instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) hx;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }


    private void monitorLoad() {

        if (this.timeFromMidnight == 0) {
            // a brand new day...let's make a new prediction

            if (this.lastDayProfile.getEndingTimeOfProfile() != 0) {
                while (this.lastDayProfiles.size() >= this.pastDaysPrediction)
                    this.lastDayProfiles.remove(0);
                this.lastDayProfile.setEndingTimeOfProfile(86400);
                this.lastDayProfiles.add(this.lastDayProfile);
            }
            this.predictedWaterDemand = new SparseLoadProfile();
            SparseLoadProfile predictedLoadToday = new SparseLoadProfile();
            SparseLoadProfile predictedLoadTomorrow = new SparseLoadProfile();

            double weightSumToday = 0.0;
            double weightSumTomorrow = 0.0;

            //Prediction for today
            for (int i = 0; i < this.lastDayProfiles.size(); i++) {
                double weight = ((i + 1) % 7 == 0) ? this.weightForSameWeekday : this.weightForOtherWeekday;
                SparseLoadProfile weightedProfile = this.lastDayProfiles.get(i).clone();
                weightedProfile.multiplyLoadsWithFactor(weight);
                predictedLoadToday = predictedLoadToday.merge(weightedProfile, 0);
                weightSumToday += weight;
            }
            predictedLoadToday.multiplyLoadsWithFactor(1.0 / weightSumToday);
            predictedLoadToday.setEndingTimeOfProfile(86400);

            //Prediction for tomorrow
            for (int i = 0; i < this.lastDayProfiles.size() - 1; i++) {
                double weight = ((i + 2) % 7 == 0) ? this.weightForSameWeekday : this.weightForOtherWeekday;
                SparseLoadProfile weightedProfile = this.lastDayProfiles.get(i).clone();
                weightedProfile.multiplyLoadsWithFactor(weight);
                predictedLoadTomorrow = predictedLoadTomorrow.merge(weightedProfile, 0);
                weightSumTomorrow += weight;
            }
            predictedLoadTomorrow.multiplyLoadsWithFactor(1.0 / weightSumTomorrow);
            predictedLoadTomorrow.setEndingTimeOfProfile(86400);

            this.predictedWaterDemand = predictedLoadToday.merge(predictedLoadTomorrow, 86400).getProfileWithoutDuplicateValues();

            //create a new profile for the prediction
            this.lastDayProfile = new SparseLoadProfile();

            this.lastDayProfile.setLoad(
                    Commodity.HEATINGHOTWATERPOWER,
                    0,
                    this.hotWaterPower);
        } else {
            this.lastDayProfile.setLoad(
                    Commodity.HEATINGHOTWATERPOWER,
                    this.timeFromMidnight,
                    this.hotWaterPower);
        }
    }


    private void sendIPP() {
        long now = this.getTimer().getUnixTime();
        long secondsSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
        long startOfDay = now - secondsSinceMidnight;

        HotWaterDemandNonControllableIPP ipp =
                new HotWaterDemandNonControllableIPP(
                        this.getUUID(),
                        this.getDeviceType(),
                        this.getGlobalLogger(),
                        now,
                        false,
                        this.predictedWaterDemand.cloneWithOffset(startOfDay),
                        Commodity.HEATINGHOTWATERPOWER,
                        this.compressionType,
                        this.compressionValue);
        this.getOCRegistry().publish(
                InterdependentProblemPart.class, this, ipp);
    }

}
