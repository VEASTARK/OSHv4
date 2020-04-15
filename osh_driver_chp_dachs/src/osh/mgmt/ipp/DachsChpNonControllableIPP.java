package osh.mgmt.ipp;

import osh.configuration.oc.EAObjectives;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.chp.model.GenericChpModel;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a problem-part for a non-controllable dachs combinded-heating-plant (chp).
 *
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Julian Feder, Sebastian Kramer
 */
public class DachsChpNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    /**
     * slot length in [s]
     */
    private final GenericChpModel masterModel;
    private GenericChpModel actualModel;
    private final double fixedCostPerStart;
    private final boolean initialChpState;
    private final int minRuntime;


    // ### interdependent stuff ###
    // temperature control
    private final double hotWaterStorageMinTemp;
    private final double hotWaterStorageMaxTemp;
    private boolean interdependentLastState;
    /**
     * from hot water tank IPP
     */
    private double currentWaterTemperature;


    /**
     * Constructs this non-controllable chp-ipp with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param minRuntime the minimum time the chp needs to stay on
     * @param chpModel a model of the chp
     * @param initialChpState the initial operating state of the chp
     * @param hotWaterStorageMinTemp the minimum hot-water temperature needed to kept
     * @param hotWaterStorageMaxTemp the maximum hot-water temperature allowed
     * @param currentWaterTemperature the current hot-water temperature
     * @param fixedCostPerStart the additional cervisia costs per start of the chp
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public DachsChpNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
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
                timestamp,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
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

    /**
     * Limited copy-constructor that constructs a copy of the given non-controllable chp-ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the non-controllable chp-ipp to copy
     */
    public DachsChpNonControllableIPP(DachsChpNonControllableIPP other) {
        super(other);

        this.initialChpState = other.initialChpState;
        this.minRuntime = other.minRuntime;

        this.masterModel = other.masterModel;

        this.hotWaterStorageMinTemp = other.hotWaterStorageMinTemp;
        this.hotWaterStorageMaxTemp = other.hotWaterStorageMaxTemp;
        this.currentWaterTemperature = other.currentWaterTemperature;
        this.fixedCostPerStart = other.fixedCostPerStart;
    }

    @Override
    public void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(interdependentStartingTime, stepSize, createLoadProfile, keepPrediction);

        this.interdependentLastState = this.initialChpState;

        this.actualModel = this.masterModel.clone();
    }

    @Override
    public void calculateNextStep() {

        boolean chpNewState = this.interdependentLastState;

        // update water temperature

        this.currentWaterTemperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

        // CHP control (forced on/off)

        if (this.interdependentLastState) {
            if (this.currentWaterTemperature > this.hotWaterStorageMaxTemp) {
                chpNewState = false;
            }
        } else {
            if (this.currentWaterTemperature < this.hotWaterStorageMinTemp) {
                chpNewState = true;
                this.addInterdependentCervisia(EAObjectives.MONEY, this.fixedCostPerStart);
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

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] DachsChpNonControllableIPP";
    }

    @Override
    public DachsChpNonControllableIPP getClone() {
        return new DachsChpNonControllableIPP(this);
    }
}