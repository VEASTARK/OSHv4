package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.gasboiler.GasBoilerModel;

import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Sebastian Kramer, Ingo Mauser
 */
public class GasBoilerNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = 1001003082323078089L;

    private final double MIN_TEMPERATURE;
    private final double MAX_TEMPERATURE;
    private final int MAX_HOT_WATER_POWER;
    private final int MAX_GAS_POWER;
    private final boolean INITIAL_STATE;

    private int typicalActivePowerOn;
    private int typicalActivePowerOff;
    private int typicalReactivePowerOn;
    private int typicalReactivePowerOff;

    private GasBoilerModel model;

    /**
     * CONSTRUCTOR
     */
    public GasBoilerNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            long now,
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
                logger,
                false, //does not cause a scheduling
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
                now,
                DeviceTypes.GASHEATING,
                EnumSet.of(Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.NATURALGASPOWER),
                compressionType,
                compressionValue);

        this.INITIAL_STATE = initialState;
        this.MIN_TEMPERATURE = minTemperature;
        this.MAX_TEMPERATURE = maxTemperature;
        this.MAX_HOT_WATER_POWER = maxHotWaterPower;
        this.MAX_GAS_POWER = maxGasPower;
        this.typicalActivePowerOn = typicalActivePowerOn;
        this.typicalActivePowerOff = typicalActivePowerOff;
        this.typicalReactivePowerOn = typicalReactivePowerOn;
        this.typicalReactivePowerOff = typicalReactivePowerOff;
        this.model = new GasBoilerModel(this.MAX_HOT_WATER_POWER, this.MAX_GAS_POWER, typicalActivePowerOn, typicalActivePowerOff,
                typicalReactivePowerOn, typicalReactivePowerOff, this.INITIAL_STATE);

        this.setAllInputCommodities(EnumSet.of(Commodity.HEATINGHOTWATERPOWER));
    }

    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected GasBoilerNonControllableIPP() {
        super();
        this.MIN_TEMPERATURE = 0;
        this.MAX_TEMPERATURE = 0;
        this.MAX_HOT_WATER_POWER = 0;
        this.MAX_GAS_POWER = 0;
        this.INITIAL_STATE = false;
    }


    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        this.model = new GasBoilerModel(this.MAX_HOT_WATER_POWER, this.MAX_GAS_POWER, this.typicalActivePowerOn, this.typicalActivePowerOff,
                this.typicalReactivePowerOn, this.typicalReactivePowerOff, this.INITIAL_STATE);
    }


    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            // get temperature of tank
            double currentTemperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

            // LOGIC
            if (this.model.isOn() && currentTemperature > this.MAX_TEMPERATURE) {
                this.model.switchOff();
            } else if (!this.model.isOn() && currentTemperature < this.MIN_TEMPERATURE) {
                this.model.switchOn();
            }

            double activePower = 0.0 + this.model.getActivePower();
            double reactivePower = 0.0 + this.model.getReactivePower();
            double thermalPower = 0.0 + -this.model.getHotWaterPower();
            double gasPower = 0.0 + this.model.getGasPower();

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
        } else {
            this.getGlobalLogger().logDebug("interdependentInputStates == null");
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
        return "[" + this.getReferenceTime() + "] GasHeatingNonControllableIPP ON=" + this.model.isOn() + " MIN=" + this.MIN_TEMPERATURE + " MAX=" + this.MAX_TEMPERATURE;
    }

}
