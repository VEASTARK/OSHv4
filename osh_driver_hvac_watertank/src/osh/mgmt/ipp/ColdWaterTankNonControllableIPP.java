package osh.mgmt.ipp;

import osh.configuration.oc.EAObjectives;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.TemperaturePrediction;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.thermal.FactorisedBasicWaterTank;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.TreeMap;
import java.util.UUID;


/**
 * Represents a problem-part for a cold-water tank.
 *
 * @author Florian Allerding, Ingo Mauser, Till Schuberth
 */
public class ColdWaterTankNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private final FactorisedBasicWaterTank masterWaterTank;
    private FactorisedBasicWaterTank actualWaterTank;
    private Double firstTemperature;
    //6ct per kWh ThermalPower (= gas price per kWh)
    private final double punishmentFactorPerWsPowerLost;

    private TreeMap<Long, Double> temperatureStates;

    /**
     * Constructs this cold-water tank ipp with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param initialTemperature the intial temperature of the watertank
     * @param tankCapacity the capacity of the watertank
     * @param tankDiameter the diameter of the watertank
     * @param standingHeatLossFactor the heat-loss factor as a multiple of the standard assumed heat-loss of the
     *                               simple water tank
     * @param ambientTemperature the ambient temperature around the watertank
     * @param punishmentFactorPerWsLost the punishment factor per ws lost over the optimization loop
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public ColdWaterTankNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            double initialTemperature,
            double tankCapacity,
            double tankDiameter,
            double standingHeatLossFactor,
            double ambientTemperature,
            double punishmentFactorPerWsLost,
            LoadProfileCompressionTypes compressionType,
            int compressionValue)  {
        super(
                deviceId,
                timestamp,
                toBeScheduled, //does not cause scheduling
                false, //does not need ancillary meter state as Input State
                true, // reacts to input states
                false, //is not static
                DeviceTypes.COLDWATERSTORAGE,
                EnumSet.of(Commodity.COLDWATERPOWER),
                compressionType,
                compressionValue);

        this.masterWaterTank = new FactorisedBasicWaterTank(tankCapacity, tankDiameter, initialTemperature,
                ambientTemperature, standingHeatLossFactor);
        this.punishmentFactorPerWsPowerLost = punishmentFactorPerWsLost;

        this.setAllInputCommodities(EnumSet.of(Commodity.COLDWATERPOWER));
    }

    /**
     * Limited copy-constructor that constructs a copy of the given cold-water tank ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the cold-water tank ipp to copy
     */
    public ColdWaterTankNonControllableIPP(ColdWaterTankNonControllableIPP other) {
        super(other);
        this.masterWaterTank = new FactorisedBasicWaterTank(other.masterWaterTank);
        this.actualWaterTank = null;
        this.temperatureStates = null;

        this.punishmentFactorPerWsPowerLost = other.punishmentFactorPerWsPowerLost;
        this.firstTemperature = other.firstTemperature;
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
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

        if (keepPrediction)
            this.temperatureStates = new TreeMap<>();
        else
            this.temperatureStates = null;

        this.actualWaterTank = new FactorisedBasicWaterTank(this.masterWaterTank);

        this.actualWaterTank.reduceByStandingHeatLoss(this.getInterdependentTime() - this.getTimestamp().toEpochSecond());
        this.firstTemperature = this.actualWaterTank.getCurrentWaterTemperature();
    }

    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            // update tank according to interdependentInputStates
            double coldWaterPower = this.interdependentInputStates.getPower(Commodity.COLDWATERPOWER);
            this.actualWaterTank.addPowerOverTime(coldWaterPower, this.getStepSize(), null, null);

            // update interdependentOutputStates
            this.internalInterdependentOutputStates.setTemperature(Commodity.COLDWATERPOWER, this.actualWaterTank.getCurrentWaterTemperature());
            this.setOutputStates(this.internalInterdependentOutputStates);
        }

        // reduce by standing loss
        this.actualWaterTank.reduceByStandingHeatLoss(this.getStepSize());
        this.incrementInterdependentTime();
    }

    @Override
    public TemperaturePrediction transformToFinalInterdependentPrediction() {
        return new TemperaturePrediction(this.temperatureStates);
    }

    @Override
    public void finalizeInterdependentCervisia() {
        super.finalizeInterdependentCervisia();

        //punish for losing power (having a higher temperature then at the start)
        double tempDifference = this.firstTemperature - this.actualWaterTank.getCurrentWaterTemperature();

        double cervisia =
                this.actualWaterTank.calculateEnergyDrawOff(
                        this.firstTemperature,
                        this.actualWaterTank.getCurrentWaterTemperature())
                        * this.punishmentFactorPerWsPowerLost;

        this.addInterdependentCervisia(EAObjectives.MONEY, cervisia);
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
    }

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] ColdWaterTank current temperature = " + (this.actualWaterTank != null ? this.actualWaterTank.getCurrentWaterTemperature() : null);
    }

    @Override
    public ColdWaterTankNonControllableIPP getClone() {
        return new ColdWaterTankNonControllableIPP(this);
    }
}
