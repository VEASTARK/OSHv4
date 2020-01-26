package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.thermal.SimpleHotWaterTank;
import osh.mgmt.ipp.watertank.HotWaterTankPrediction;

import java.util.EnumSet;
import java.util.TreeMap;
import java.util.UUID;


/**
 * @author Florian Allerding, Ingo Mauser, Till Schuberth
 */
public class HotWaterTankNonControllableIPP
        extends NonControllableIPP<ISolution, HotWaterTankPrediction> {

    private static final long serialVersionUID = -7474764554202830275L;
    private final double initialTemperature;
    private final double tankCapacity;
    private final double tankDiameter;
    private final double ambientTemperature;
    private SimpleHotWaterTank waterTank;
    private Double firstTemperature;
    //6ct per kWh ThermalPower (= gas price per kWh)
    private double punishmentFactorPerWsPowerLost;

    private TreeMap<Long, Double> temperatureStates;


    /**
     * CONSTRUCTOR
     */
    public HotWaterTankNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            long now,
            double initialTemperature,
            double tankCapacity,
            double tankDiameter,
            double ambientTemperature,
            double punishmentFactorPerWsLost,
            boolean causeScheduling,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                logger,
                causeScheduling, //does not cause scheduling
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
                now,
                DeviceTypes.HOTWATERSTORAGE,
                EnumSet.of(Commodity.HEATINGHOTWATERPOWER,
                        Commodity.DOMESTICHOTWATERPOWER),
                compressionType,
                compressionValue);

        this.initialTemperature = initialTemperature;
        this.tankCapacity = tankCapacity;
        this.tankDiameter = tankDiameter;
        this.ambientTemperature = ambientTemperature;
        this.punishmentFactorPerWsPowerLost = punishmentFactorPerWsLost;

        this.setAllInputCommodities(EnumSet.of(Commodity.HEATINGHOTWATERPOWER, Commodity.DOMESTICHOTWATERPOWER));
    }


    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected HotWaterTankNonControllableIPP() {
        super();
        this.initialTemperature = 0;
        this.tankCapacity = 0;
        this.tankDiameter = 0;
        this.ambientTemperature = 0;
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        // get new temperature of tank
        //  better not...new IPP instead
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        if (keepPrediction)
            this.temperatureStates = new TreeMap<>();
        else
            this.temperatureStates = null;

        this.waterTank = new SimpleHotWaterTank(
                this.tankCapacity,
                this.tankDiameter,
                this.initialTemperature,
                this.ambientTemperature);

        this.waterTank.reduceByStandingHeatLoss(this.getInterdependentTime() - this.getTimestamp());
        this.firstTemperature = this.waterTank.getCurrentWaterTemperature();
    }

    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            // update tank according to interdependentInputStates
            double hotWaterPower = 0;

            hotWaterPower -= this.interdependentInputStates.getPower(Commodity.HEATINGHOTWATERPOWER);
            hotWaterPower -= this.interdependentInputStates.getPower(Commodity.DOMESTICHOTWATERPOWER);

            this.waterTank.addPowerOverTime(hotWaterPower, this.getStepSize(), null, null);
        }

        double currentTemp = this.waterTank.getCurrentWaterTemperature();

        if (this.temperatureStates != null)
            this.temperatureStates.put(this.getInterdependentTime(), currentTemp);

        // update interdependentOutputStates
        this.internalInterdependentOutputStates.setTemperature(Commodity.HEATINGHOTWATERPOWER, currentTemp);
        this.internalInterdependentOutputStates.setTemperature(Commodity.DOMESTICHOTWATERPOWER, currentTemp);
        this.setOutputStates(this.internalInterdependentOutputStates);


        // reduce be standing loss
        this.waterTank.reduceByStandingHeatLoss(this.getStepSize());

        this.incrementInterdependentTime();
    }

    @Override
    public HotWaterTankPrediction transformToFinalInterdependentPrediction() {
        return new HotWaterTankPrediction(this.temperatureStates);
    }


    @Override
    public Schedule getFinalInterdependentSchedule() {
        //punish for losing power (having a lower temperature then at the start)
        double cervisia;
        double tempDifference = this.firstTemperature - this.waterTank.getCurrentWaterTemperature();

        cervisia =
                -this.waterTank.calculateEnergyDrawOff(
                        this.firstTemperature,
                        this.waterTank.getCurrentWaterTemperature())
                        * this.punishmentFactorPerWsPowerLost;

        //if tank has higher temperature than at the beginning only give half of the cervizia
        if (tempDifference < 0) {
            cervisia /= 2.0;
        }

        this.addInterdependentCervisia(cervisia);

        return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] HotWaterTankIPP initialTemperature=" + ((int) (this.initialTemperature * 10)) / 10.0;
    }
}