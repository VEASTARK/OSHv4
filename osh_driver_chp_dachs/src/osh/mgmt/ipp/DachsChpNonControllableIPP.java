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

import java.util.BitSet;
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
    /**
     * used for iteration in interdependent calculation (virtual time in the future)
     */
    private long interdependentTime;
    /**
     * running times of chiller
     */
    private double interdependentCervisia;
    private boolean interdependentLastState;
    /**
     * from hot water tank IPP
     */
    private double currentWaterTemperature;
    private SparseLoadProfile lp;


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
                new Commodity[]{Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.NATURALGASPOWER
                },
                new Commodity[]{Commodity.HEATINGHOTWATERPOWER},
                compressionType,
                compressionValue);

        this.initialChpState = initialChpState;
        this.minRuntime = minRuntime;

        this.masterModel = chpModel;

        this.hotWaterStorageMinTemp = hotWaterStorageMinTemp;
        this.hotWaterStorageMaxTemp = hotWaterStorageMaxTemp;
        this.currentWaterTemperature = currentWaterTemperature;
        this.fixedCostPerStart = fixedCostPerStart;
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            BitSet solution,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {
        this.stepSize = stepSize;

        // used for iteration in interdependent calculation
        this.setOutputStates(null);
        this.interdependentInputStates = null;

        // initialize variables
        if (createLoadProfile)
            this.lp = new SparseLoadProfile();
        else
            this.lp = null;
        this.interdependentCervisia = 0.0;

        if (this.getReferenceTime() != maxReferenceTime)
            this.setReferenceTime(maxReferenceTime);
        this.interdependentTime = this.getReferenceTime();

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
                this.interdependentCervisia += this.fixedCostPerStart;
            }
        }

        // ### set power profiles and cervizia

        if (chpNewState != this.interdependentLastState) {
            this.actualModel.setRunning(chpNewState, this.interdependentTime);
        }


        this.actualModel.calcPowerAvg(this.interdependentTime, this.interdependentTime + this.stepSize);
        int activePower = this.actualModel.getAvgActualActivePower();
        int reactivePower = this.actualModel.getAvgActualReactivePower();
        int thermalPower = this.actualModel.getAvgActualThermalPower();
        int gasPower = this.actualModel.getAvgActualGasPower();


        // set power
        if (this.lp != null) {
            this.lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, activePower);
            this.lp.setLoad(Commodity.REACTIVEPOWER, this.interdependentTime, reactivePower);
            this.lp.setLoad(Commodity.NATURALGASPOWER, this.interdependentTime, gasPower);
            this.lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, thermalPower);
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
        this.interdependentTime += this.stepSize;
    }


    @Override
    public Schedule getFinalInterdependentSchedule() {

        if (this.lp != null) {
            if (this.lp.getEndingTimeOfProfile() > 0) {
                this.lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, 0);
                this.lp.setLoad(Commodity.REACTIVEPOWER, this.interdependentTime, 0);
                this.lp.setLoad(Commodity.NATURALGASPOWER, this.interdependentTime, 0);
                this.lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, 0);
            }

            return new Schedule(this.lp.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue),
                    this.interdependentCervisia, this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
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