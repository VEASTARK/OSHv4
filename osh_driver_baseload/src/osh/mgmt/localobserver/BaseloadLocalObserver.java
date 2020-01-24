package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.BaseloadObserverExchange;
import osh.hal.exchange.BaseloadPredictionExchange;
import osh.mgmt.localobserver.baseload.ipp.BaseloadIPP;
import osh.registry.interfaces.IDataRegistryListener;
import osh.utils.time.TimeConversion;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class BaseloadLocalObserver
        extends LocalObserver
        implements IDataRegistryListener {

    private final int profileResolutionInSec = 900;

    private float weightForOtherWeekday;
    private float weightForSameWeekday;
    private int usedDaysForPrediction;

    private List<SparseLoadProfile> lastDayProfiles = new ArrayList<>();

    private SparseLoadProfile lastDayProfile;
    private SparseLoadProfile predictedBaseloadProfile;

    private long timeFromMidnight = Long.MAX_VALUE;
    private int timeRangeCounter;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    /**
     * CONSTRUCTOR
     */
    public BaseloadLocalObserver(IOSHOC osh) {
        super(osh);

        this.lastDayProfile = new SparseLoadProfile();
        this.predictedBaseloadProfile = this.lastDayProfile;
    }

    private void monitorBaseloadProfile(int activeBaseload, int reactiveBaseload) {

        if (this.timeFromMidnight == 0) {
            // a brand new day...let's make a new prediction

            if (this.lastDayProfile.getEndingTimeOfProfile() != 0) {
                while (this.lastDayProfiles.size() >= this.usedDaysForPrediction)
                    this.lastDayProfiles.remove(0);
                this.lastDayProfile.setEndingTimeOfProfile(86400);
                this.lastDayProfiles.add(this.lastDayProfile);
            }
            this.predictedBaseloadProfile = new SparseLoadProfile();
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
            predictedLoadToday.setEndingTimeOfProfile(86400);
            predictedLoadToday.multiplyLoadsWithFactor(1 / weightSumToday);

            //Prediction for tomorrow
            for (int i = 0; i < this.lastDayProfiles.size() - 1; i++) {
                double weight = ((i + 2) % 7 == 0) ? this.weightForSameWeekday : this.weightForOtherWeekday;
                SparseLoadProfile weightedProfile = this.lastDayProfiles.get(i).clone();
                weightedProfile.multiplyLoadsWithFactor(weight);
                predictedLoadTomorrow = predictedLoadTomorrow.merge(weightedProfile, 0);
                weightSumTomorrow += weight;
            }
            predictedLoadTomorrow.setEndingTimeOfProfile(86400);
            predictedLoadTomorrow.multiplyLoadsWithFactor(1 / weightSumTomorrow);

            this.predictedBaseloadProfile = predictedLoadToday.merge(
                    predictedLoadTomorrow, 86400).getProfileWithoutDuplicateValues();

            //create a new profile for the prediction
            this.lastDayProfile = new SparseLoadProfile();
        } else {
            if (this.timeFromMidnight == 86400 - 1) {
                this.lastDayProfile.setLoad(
                        Commodity.ACTIVEPOWER,
                        this.timeFromMidnight,
                        activeBaseload);
                this.lastDayProfile.setLoad(
                        Commodity.REACTIVEPOWER,
                        this.timeFromMidnight,
                        reactiveBaseload);
            } else if (this.timeRangeCounter >= this.profileResolutionInSec) {
                this.lastDayProfile.setLoad(
                        Commodity.ACTIVEPOWER,
                        this.timeFromMidnight,
                        activeBaseload);
                this.lastDayProfile.setLoad(
                        Commodity.REACTIVEPOWER,
                        this.timeFromMidnight,
                        reactiveBaseload);
                this.timeRangeCounter = 1;
            } else {
                this.timeRangeCounter++;
            }
        }
    }

    @Override
    public void onDeviceStateUpdate() {

        IHALExchange _oxObj = this.getObserverDataObject();

        if (_oxObj instanceof BaseloadObserverExchange) {
            BaseloadObserverExchange _ox = (BaseloadObserverExchange) _oxObj;

            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentEpochSecond(),
                    DeviceTypes.OTHER);

            for (Commodity c : _ox.getCommodities()) {
                int power = _ox.getPower(c);
                cpse.addPowerState(c, power);
            }

            this.getOCRegistry().publish(
                    CommodityPowerStateExchange.class,
                    this,
                    cpse);

            long lastTimeFromMidnight = this.timeFromMidnight;
            this.timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(this.getTimeDriver().getCurrentEpochSecond());

            //monitor the baseload
            this.monitorBaseloadProfile(_ox.getActivePower(), _ox.getReactivePower());

            if (lastTimeFromMidnight > this.timeFromMidnight) {
                //a new day has begun...
                this.updateIPP();
            }
        } else if (_oxObj instanceof BaseloadPredictionExchange) {
            BaseloadPredictionExchange _pred = (BaseloadPredictionExchange) _oxObj;

            this.lastDayProfiles = _pred.getPredictions();
            this.usedDaysForPrediction = _pred.getUsedDaysForPrediction();
            this.weightForOtherWeekday = _pred.getWeightForOtherWeekday();
            this.weightForSameWeekday = _pred.getWeightForSameWeekday();

        } else if (_oxObj instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) _oxObj;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }


    private void updateIPP() {
        long now = this.getTimeDriver().getCurrentEpochSecond();

        BaseloadIPP ipp = new BaseloadIPP(
                this.getUUID(),
                this.getGlobalLogger(),
                now,
                false,
                DeviceTypes.BASELOAD,
                now,
                this.predictedBaseloadProfile.cloneWithOffset(now),
                this.compressionType,
                this.compressionValue);

        this.getOCRegistry().publish(InterdependentProblemPart.class, this, ipp);
    }


    @Override
    public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
        return null;
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        //NOTHING
    }

}
