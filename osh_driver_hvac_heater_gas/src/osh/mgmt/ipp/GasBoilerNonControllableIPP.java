package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.gasboiler.GasBoilerModel;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a problem-part for a non-controllable simple gas-boiler.
 *
 * @author Sebastian Kramer, Ingo Mauser
 */
public class GasBoilerNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private final double MIN_TEMPERATURE;
    private final double MAX_TEMPERATURE;

    private final GasBoilerModel masterModel;
    private GasBoilerModel actualModel;

    /**
     * Constructs this non-controllable gas-boiler-ipp with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param minTemperature the minimum hot-water temperature needed to kept
     * @param maxTemperature the maximum hot-water temperature allowed
     * @param initialState the initial operating state of the gas-boiler
     * @param maxHotWaterPower the maximum heating power of the gas-boiler
     * @param maxGasPower the maximum gas consumption of the gas-boiler
     * @param typicalActivePowerOn the typical active power consumption when on
     * @param typicalActivePowerOff the typical active power consumption when off
     * @param typicalReactivePowerOn the typical reactive power consumption when on
     * @param typicalReactivePowerOff the typical reactive power consumption when off
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public GasBoilerNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            double minTemperature,
            double maxTemperature,
            boolean initialState,
            int maxHotWaterPower,
            int maxGasPower,
            int typicalActivePowerOn,
            int typicalActivePowerOff,
            int typicalReactivePowerOn,
            int typicalReactivePowerOff,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                timestamp,
                false, //does not cause a scheduling
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
                DeviceTypes.GASHEATING,
                EnumSet.of(Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.NATURALGASPOWER),
                compressionType,
                compressionValue);

        this.MIN_TEMPERATURE = minTemperature;
        this.MAX_TEMPERATURE = maxTemperature;
        this.masterModel = new GasBoilerModel(maxHotWaterPower, maxGasPower, typicalActivePowerOn,
                typicalActivePowerOff, typicalReactivePowerOn, typicalReactivePowerOff, initialState);

        this.setAllInputCommodities(EnumSet.of(Commodity.HEATINGHOTWATERPOWER));
    }

    /**
     * Limited copy-constructor that constructs a copy of the given non-controllable gas-boiler-ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the non-controllable gas-boiler-ipp to copy
     */
    public GasBoilerNonControllableIPP(GasBoilerNonControllableIPP other) {
        super(other);
        this.MIN_TEMPERATURE = other.MIN_TEMPERATURE;
        this.MAX_TEMPERATURE = other.MAX_TEMPERATURE;
        this.masterModel = other.masterModel;
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(interdependentStartingTime, stepSize, createLoadProfile, keepPrediction);

        this.actualModel = new GasBoilerModel(this.masterModel);
    }


    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            // get temperature of tank
            double currentTemperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

            // LOGIC
            if (this.actualModel.isOn() && currentTemperature > this.MAX_TEMPERATURE) {
                this.actualModel.switchOff();
            } else if (!this.actualModel.isOn() && currentTemperature < this.MIN_TEMPERATURE) {
                this.actualModel.switchOn();
            }

            double activePower = 0.0 + this.actualModel.getActivePower();
            double reactivePower = 0.0 + this.actualModel.getReactivePower();
            double thermalPower = 0.0 + -this.actualModel.getHotWaterPower();
            double gasPower = 0.0 + this.actualModel.getGasPower();

            if (this.getLoadProfile() != null) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), (int) activePower);
                this.getLoadProfile().setLoad(Commodity.REACTIVEPOWER, this.getInterdependentTime(), (int) reactivePower);
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), (int) thermalPower);
                this.getLoadProfile().setLoad(Commodity.NATURALGASPOWER, this.getInterdependentTime(), (int) gasPower);
            }

            // update interdependentOutputStates
            boolean hasValues = false;

            if (activePower != 0) {
                this.internalInterdependentOutputStates.setPower(Commodity.ACTIVEPOWER, activePower);
                hasValues = true;
            } else {
                this.internalInterdependentOutputStates.resetCommodity(Commodity.ACTIVEPOWER);
            }

            if (thermalPower != 0) {
                this.internalInterdependentOutputStates.setPower(Commodity.HEATINGHOTWATERPOWER, thermalPower);
                hasValues = true;
            } else {
                this.internalInterdependentOutputStates.resetCommodity(Commodity.HEATINGHOTWATERPOWER);
            }

            if (gasPower != 0) {
                this.internalInterdependentOutputStates.setPower(Commodity.NATURALGASPOWER, gasPower);
                hasValues = true;
            } else {
                this.internalInterdependentOutputStates.resetCommodity(Commodity.NATURALGASPOWER);
            }

            if (hasValues) {
                this.setOutputStates(this.internalInterdependentOutputStates);
            } else {
                this.setOutputStates(null);
            }
        }

        this.incrementInterdependentTime();
    }


    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.getLoadProfile() != null) {
            if (this.getLoadProfile().getEndingTimeOfProfile() > 0) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), 0);
                this.getLoadProfile().setLoad(Commodity.REACTIVEPOWER, this.getInterdependentTime(), 0);
                this.getLoadProfile().setLoad(Commodity.NATURALGASPOWER, this.getInterdependentTime(), 0);
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), 0);
            }
            return new Schedule(this.getLoadProfile().getCompressedProfile(this.compressionType,
                    this.compressionValue, this.compressionValue), this.getInterdependentCervisia(), this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] GasHeatingNonControllableIPP ON=" + this.masterModel.isOn() + " MIN=" + this.MIN_TEMPERATURE + " MAX=" + this.MAX_TEMPERATURE;
    }

    @Override
    public GasBoilerNonControllableIPP getClone() {
        return new GasBoilerNonControllableIPP(this);
    }

}
