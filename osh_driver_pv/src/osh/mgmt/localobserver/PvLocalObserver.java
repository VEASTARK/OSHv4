package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.details.energy.ElectricCurrentOCDetails;
import osh.datatypes.registry.oc.details.energy.ElectricPowerOCDetails;
import osh.datatypes.registry.oc.details.energy.ElectricVoltageOCDetails;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.PvObserverExchange;
import osh.hal.exchange.PvPredictionExchange;
import osh.mgmt.ipp.PvNonControllableIPP;
import osh.utils.time.TimeConversion;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Florian Allerding, Ingo Mauser
 */
public class PvLocalObserver extends LocalObserver {

    // Configuration variables
    private static final int profileResolutionInSec = 900;
    private double lastActivePowerLevel;
    private double lastReactivePowerLevel;
    private SparseLoadProfile lastDayProfile;
    private SparseLoadProfile predictedPVProfile;
    private int usedDaysForPrediction;
    private List<SparseLoadProfile> lastDayProfiles = new ArrayList<>();
    private int timeRangeCounter;
    private long timeFromMidnight = Long.MAX_VALUE;
    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public PvLocalObserver(IOSHOC osh) {
        super(osh);
        this.lastDayProfile = new SparseLoadProfile();

        //start with an empty prediction
        this.predictedPVProfile = this.lastDayProfile;
    }

    @Override
    public void onDeviceStateUpdate() {

        IHALExchange _oxObj = this.getObserverDataObject();

        if (_oxObj instanceof PvObserverExchange) {
            PvObserverExchange _ox = (PvObserverExchange) _oxObj;

            ElectricCurrentOCDetails _currentDetails = new ElectricCurrentOCDetails(_ox.getDeviceID(), _ox.getTimestamp());
            _currentDetails.setCurrent(_ox.getCurrent());

            ElectricPowerOCDetails _powDetails = new ElectricPowerOCDetails(_ox.getDeviceID(), _ox.getTimestamp());
            _powDetails.setActivePower(_ox.getActivePower());
            _powDetails.setReactivePower(_ox.getReactivePower());

            ElectricVoltageOCDetails _voltageDetails = new ElectricVoltageOCDetails(_ox.getDeviceID(), _ox.getTimestamp());
            _voltageDetails.setVoltage(_ox.getVoltage());

            if (Math.abs(this.lastActivePowerLevel - _powDetails.getActivePower()) > 1) {
                this.lastActivePowerLevel = _powDetails.getActivePower();
                this.lastReactivePowerLevel = _powDetails.getReactivePower();

                CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentTime(),
                        DeviceTypes.PVSYSTEM);
                cpse.addPowerState(Commodity.ACTIVEPOWER, this.lastActivePowerLevel);
                cpse.addPowerState(Commodity.REACTIVEPOWER, this.lastReactivePowerLevel);

                this.getOCRegistry().publish(
                        CommodityPowerStateExchange.class,
                        this,
                        cpse);
            }

            //refresh time from Midnight
            this.timeFromMidnight = TimeConversion.getSecondsSinceDayStart(this.getTimeDriver().getCurrentTime());

            //monitor the load profile
            this.runPvProfilePredictor(_powDetails);

            //refresh the EApart
            if (this.getTimeDriver().getCurrentTimeEvents().contains(TimeSubscribeEnum.DAY)) {
                //a new day has begun...
                this.updateEAPart();
            }
        } else if (_oxObj instanceof PvPredictionExchange) {
            PvPredictionExchange _pvPred = (PvPredictionExchange) _oxObj;

            this.usedDaysForPrediction = _pvPred.getPastDaysPrediction();
            this.lastDayProfiles = _pvPred.getPredictions();
        } else if (_oxObj instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) _oxObj;

            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }

    private void updateEAPart() {
        long now = this.getTimeDriver().getCurrentEpochSecond();

        //Prediction is always in relative Time from midnight, we need to extend it and then convert to absolute time
        SparseLoadProfile optimizationProfile = this.predictedPVProfile.merge(this.predictedPVProfile, 86400).cloneWithOffset(now);

        PvNonControllableIPP ipp = new PvNonControllableIPP(this.getUUID(),
                this.getTimeDriver().getCurrentTime(), optimizationProfile, this.compressionType, this.compressionValue);

        this.getOCRegistry().publish(InterdependentProblemPart.class, this, ipp);
    }

    private void runPvProfilePredictor(ElectricPowerOCDetails powerDetails) {

        if (this.getTimeDriver().getCurrentTimeEvents().contains(TimeSubscribeEnum.DAY)) {
            //hooray a brand new day...let's make a new prediction

            if (this.lastDayProfile.getEndingTimeOfProfile() != 0) {
                while (this.lastDayProfiles.size() >= this.usedDaysForPrediction)
                    this.lastDayProfiles.remove(0);
                this.lastDayProfile.setEndingTimeOfProfile(86400);
                this.lastDayProfiles.add(this.lastDayProfile);
            }
            this.predictedPVProfile = new SparseLoadProfile();

            for (SparseLoadProfile sp : this.lastDayProfiles) {
                this.predictedPVProfile = this.predictedPVProfile.merge(sp, 0);
            }

            this.predictedPVProfile.multiplyLoadsWithFactor(1.0 / (!this.lastDayProfiles.isEmpty() ? this.lastDayProfiles.size() : 1));

            this.predictedPVProfile = this.predictedPVProfile.getProfileWithoutDuplicateValues();

            this.predictedPVProfile.setEndingTimeOfProfile(86400);
            //create a new profile for the prediction
            this.lastDayProfile = new SparseLoadProfile();
        } else {
            if (this.timeRangeCounter >= profileResolutionInSec) {
                this.lastDayProfile.setLoad(Commodity.ACTIVEPOWER, this.timeFromMidnight, powerDetails.getActivePower());
                this.lastDayProfile.setLoad(Commodity.REACTIVEPOWER, this.timeFromMidnight, powerDetails.getReactivePower());
                //TODO: change to 1 in when next backwards-compatibility breaking update gets released
                this.timeRangeCounter = 0;
            } else {
                ++this.timeRangeCounter;
            }
        }
    }


    @Override
    public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
        return null;
    }
}
