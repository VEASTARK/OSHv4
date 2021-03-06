package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.ihe.SmartHeaterModel;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;


/**
 * Represents a problem-part for a non-controllable self-adjusting smart-heater.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class SmartHeaterNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private final SmartHeaterModel masterModel;
    private SmartHeaterModel actualModel;


    /**
     * Constructs this non-controllable problem-part with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param temperatureSetting the target temperature for the heater
     * @param initialState the initial operating state of the heater
     * @param timestampOfLastChangePerSubElement if this problem-part  reacts to any input information inside the optimization loop
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public SmartHeaterNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            int temperatureSetting,
            int initialState,
            long[] timestampOfLastChangePerSubElement,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                timestamp,
                false, //does not cause scheduling
                true, //needs ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
                DeviceTypes.INSERTHEATINGELEMENT,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);

        this.masterModel = new SmartHeaterModel(
                temperatureSetting,
                initialState,
                timestampOfLastChangePerSubElement);

        this.setAllInputCommodities(EnumSet.of(Commodity.HEATINGHOTWATERPOWER));
    }

    public SmartHeaterNonControllableIPP(SmartHeaterNonControllableIPP other) {
        super(other);
        this.masterModel = new SmartHeaterModel(other.masterModel);
    }


    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        // get new temperature of tank
        //  better not...new IPP instead
    }

    @Override
    public void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(interdependentStartingTime, stepSize, createLoadProfile, keepPrediction);

        this.actualModel = new SmartHeaterModel(this.masterModel);
    }

    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            double availablePower = 0;

            if (this.ancillaryMeterState != null) {

                // #1
//				double chpFeedIn = 0;
//				double pvFeedIn = 0;
//				if (ancillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null) {
//					chpFeedIn = Math.abs(AncillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPower());
//				}
//				if (ancillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null) {
//					pvFeedIn = Math.abs(AncillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPower());
//				}
//				availablePower = (int) (chpFeedIn + pvFeedIn);

                // #2
                availablePower = (int) this.ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);

//					if (availablePower > 1500) {
//						@SuppressWarnings("unused")
//						int xxx = 0;
//					}
            }

            // get temperature of tank
            double temperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

            // update state
            this.actualModel.updateAvailablePower(this.getInterdependentTime(), availablePower, temperature);

            // update interdependentOutputStates
            this.internalInterdependentOutputStates.setPower(
                    Commodity.ACTIVEPOWER, this.actualModel.getPower());
            this.internalInterdependentOutputStates.setPower(
                    Commodity.HEATINGHOTWATERPOWER, -this.actualModel.getPower());
            this.setOutputStates(this.internalInterdependentOutputStates);

            if (this.getLoadProfile() != null) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), (int) this.actualModel.getPower());
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), (int) -this.actualModel.getPower());
            }
        }

        this.incrementInterdependentTime();
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.getLoadProfile() != null) {
            if (this.getLoadProfile().getEndingTimeOfProfile() > 0) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), 0);
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), 0);
            }
            return new Schedule(this.getLoadProfile().getCompressedProfile(this.compressionType,
                    this.compressionValue, this.compressionValue), this.getInterdependentCervisia(), this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] SmartHeaterNonControllableIPP setTemperature=" + this.masterModel.getSetTemperature() + " " +
                "currentState=" + this.masterModel.getCurrentState();
    }

    @Override
    public SmartHeaterNonControllableIPP getClone() {
        return new SmartHeaterNonControllableIPP(this);
    }
}