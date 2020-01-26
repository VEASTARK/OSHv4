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
import osh.driver.chp.model.GenericChpModel;

import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Julian Feder, Sebastian Kramer
 */
@SuppressWarnings("unused")
public class DachsChpNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    /**
     * slot length in [s]
     */
    public final static long TIME_PER_SLOT = 5 * 60; // 5 minutes
    /**
     *
     */
    private static final long serialVersionUID = -1505828917459842280L;
    GenericChpModel masterModel;
    GenericChpModel actualModel;
    private double fixedCostPerStart;
    private boolean initialChpState;
    private int minRuntime;


    // ### interdependent stuff ###
    // temperature control
    private double hotWaterStorageMinTemp;
    private double hotWaterStorageMaxTemp;
    private boolean interdependentLastState;
    /**
     * from hot water tank IPP
     */
    private double currentWaterTemperature;


    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected DachsChpNonControllableIPP() {
        super();
    }

    /**
     * CONSTRUCTOR
     */
    public DachsChpNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            long now,
            boolean toBeScheduled,
            int minRuntime,
            GenericChpModel chpModel,
            boolean initialChpState,
            double hotWaterStorageMinTemp,
            double hotWaterStorageMaxTemp,
            double currentWaterTemperature,
            double fixedCostPerStart,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                logger,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
                now,
                DeviceTypes.CHPPLANT,
                EnumSet.of(Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.NATURALGASPOWER),
                compressionType,
                compressionValue);

        this.initialChpState = initialChpState;
        this.minRuntime = minRuntime;

        this.masterModel = chpModel;

        this.hotWaterStorageMinTemp = hotWaterStorageMinTemp;
        this.hotWaterStorageMaxTemp = hotWaterStorageMaxTemp;
        this.currentWaterTemperature = currentWaterTemperature;
        this.fixedCostPerStart = fixedCostPerStart;

        this.setAllInputCommodities(EnumSet.of(Commodity.HEATINGHOTWATERPOWER));
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        this.interdependentLastState = this.initialChpState;

        this.actualModel = this.masterModel.clone();
    }

    @Override
    public void calculateNextStep() {

        boolean chpNewState = this.interdependentLastState;

        // update water temperature

        if (this.interdependentInputStates == null) {
            this.getGlobalLogger().logDebug("No interdependentInputStates available.");
        }

        this.currentWaterTemperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

        // CHP control (forced on/off)

        if (this.interdependentLastState) {
            if (this.currentWaterTemperature > this.hotWaterStorageMaxTemp) {
                chpNewState = false;
            }
        } else {
            if (this.currentWaterTemperature < this.hotWaterStorageMinTemp) {
                chpNewState = true;
                this.addInterdependentCervisia(this.fixedCostPerStart);
            }
        }

        // ### set power profiles and cervizia

        if (chpNewState != this.interdependentLastState) {
            this.actualModel.setRunning(chpNewState, this.getInterdependentTime());
        }


        this.actualModel.calcPowerAvg(this.getInterdependentTime(), this.getInterdependentTime() + this.getStepSize());
        int activePower = this.actualModel.getAvgActualActivePower();
        int reactivePower = this.actualModel.getAvgActualReactivePower();
        int thermalPower = this.actualModel.getAvgActualThermalPower();
        int gasPower = this.actualModel.getAvgActualGasPower();


        // set power
        if (this.getLoadProfile() != null) {
            this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), activePower);
            this.getLoadProfile().setLoad(Commodity.REACTIVEPOWER, this.getInterdependentTime(), reactivePower);
            this.getLoadProfile().setLoad(Commodity.NATURALGASPOWER, this.getInterdependentTime(), gasPower);
            this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), thermalPower);
        }

        boolean hasValues = false;

        if (activePower != 0) {
            this.internalInterdependentOutputStates.setPower(Commodity.ACTIVEPOWER, activePower);
            hasValues = true;
        } else {
            this.internalInterdependentOutputStates.resetCommodity(Commodity.ACTIVEPOWER);
        }

        if (reactivePower != 0) {
            this.internalInterdependentOutputStates.setPower(Commodity.REACTIVEPOWER, reactivePower);
            hasValues = true;
        } else {
            this.internalInterdependentOutputStates.resetCommodity(Commodity.REACTIVEPOWER);
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

        this.interdependentLastState = chpNewState;
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

            return new Schedule(this.getLoadProfile().getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue),
                    this.getInterdependentCervisia(), this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] DachsChpNonControllableIPP";
    }
}