package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.TemperaturePrediction;
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
 * Represents a problem-part for a hot-water tank.
 *
 * @author Florian Allerding, Ingo Mauser, Till Schuberth
 */
public class HotWaterTankNonControllableIPP
        extends NonControllableIPP<ISolution, TemperaturePrediction> {

    private final FactorisedBasicWaterTank masterWaterTank;
    private FactorisedBasicWaterTank actualWaterTank;
    private Double firstTemperature;
    //6ct per kWh ThermalPower (= gas price per kWh)
    private final double punishmentFactorPerWsPowerLost;

    private TreeMap<Long, Double> temperatureStates;

    /**
     * Constructs this hot-water tank ipp with the given information.
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
    public HotWaterTankNonControllableIPP(
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
            int compressionValue) {

        super(
                deviceId,
                timestamp,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
                DeviceTypes.HOTWATERSTORAGE,
                EnumSet.of(Commodity.HEATINGHOTWATERPOWER,
                        Commodity.DOMESTICHOTWATERPOWER),
                compressionType,
                compressionValue);

        this.masterWaterTank = new FactorisedBasicWaterTank(tankCapacity, tankDiameter, initialTemperature,
                ambientTemperature, standingHeatLossFactor);
        this.punishmentFactorPerWsPowerLost = punishmentFactorPerWsLost;

        this.setAllInputCommodities(EnumSet.of(Commodity.HEATINGHOTWATERPOWER, Commodity.DOMESTICHOTWATERPOWER));
    }

    /**
     * Limited copy-constructor that constructs a copy of the given hot-water tank ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the hot-water tank ipp to copy
     */
    public HotWaterTankNonControllableIPP(HotWaterTankNonControllableIPP other) {
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
            double hotWaterPower = 0;

            hotWaterPower -= this.interdependentInputStates.getPower(Commodity.HEATINGHOTWATERPOWER);
            hotWaterPower -= this.interdependentInputStates.getPower(Commodity.DOMESTICHOTWATERPOWER);

            this.actualWaterTank.addPowerOverTime(hotWaterPower, this.getStepSize(), null, null);
        }

        double currentTemp = this.actualWaterTank.getCurrentWaterTemperature();

        if (this.temperatureStates != null)
            this.temperatureStates.put(this.getInterdependentTime(), currentTemp);

        // update interdependentOutputStates
        this.internalInterdependentOutputStates.setTemperature(Commodity.HEATINGHOTWATERPOWER, currentTemp);
        this.internalInterdependentOutputStates.setTemperature(Commodity.DOMESTICHOTWATERPOWER, currentTemp);
        this.setOutputStates(this.internalInterdependentOutputStates);


        // reduce be standing loss
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

        //punish for losing power (having a lower temperature then at the start)
        double tempDifference = this.firstTemperature - this.actualWaterTank.getCurrentWaterTemperature();

        double cervisia =
                -this.actualWaterTank.calculateEnergyDrawOff(
                        this.firstTemperature,
                        this.actualWaterTank.getCurrentWaterTemperature())
                        * this.punishmentFactorPerWsPowerLost;

        //if tank has higher temperature than at the beginning only give half of the cervisia
        if (tempDifference < 0) {
            cervisia /= 2.0;
        }

        this.addInterdependentCervisia(cervisia);
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
    }

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] HotWaterTankIPP initialTemperature=" + ((int) (this.masterWaterTank.getCurrentWaterTemperature() * 10)) / 10.0;
    }

    @Override
    public HotWaterTankNonControllableIPP getClone() {
        return new HotWaterTankNonControllableIPP(this);
    }
}