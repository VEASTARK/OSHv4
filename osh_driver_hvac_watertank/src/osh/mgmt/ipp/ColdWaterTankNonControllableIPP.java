package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.thermal.SimpleColdWaterTank;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;


/**
 * Represents a problem-part for a cold-water tank.
 *
 * @author Florian Allerding, Ingo Mauser, Till Schuberth
 */
public class ColdWaterTankNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private final double initialTemperature;
    private static final double tankCapacity = 3000.0;
    private static final double tankDiameter = 1.0;
    private static final double ambientTemperature = 20.0;

    private SimpleColdWaterTank waterTank;

    /**
     * Constructs this cold-water tank ipp with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param initialTemperature the intial temperature of the watertank
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public ColdWaterTankNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            double initialTemperature,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                timestamp,
                false, //does not cause scheduling
                false, //does not need ancillary meter state as Input State
                true, // reacts to input states
                false, //is not static
                DeviceTypes.COLDWATERSTORAGE,
                EnumSet.of(Commodity.COLDWATERPOWER),
                compressionType,
                compressionValue);

        this.initialTemperature = initialTemperature;
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
        this.initialTemperature = other.initialTemperature;
        this.waterTank = null;
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

        this.waterTank = new SimpleColdWaterTank(
                tankCapacity,
                tankDiameter,
                this.initialTemperature,
                ambientTemperature);

        this.waterTank.reduceByStandingHeatLoss(this.getInterdependentTime() - this.getTimestamp().toEpochSecond());
    }

    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            // update tank according to interdependentInputStates
            double coldWaterPower = this.interdependentInputStates.getPower(Commodity.COLDWATERPOWER);
            if (coldWaterPower != 0) {
                this.waterTank.addPowerOverTime(coldWaterPower, this.getStepSize(), null, null);
            }

            // update interdependentOutputStates
            this.internalInterdependentOutputStates.setTemperature(Commodity.COLDWATERPOWER, this.waterTank.getCurrentWaterTemperature());
            this.setOutputStates(this.internalInterdependentOutputStates);
        }

        // reduce by standing loss
        this.waterTank.reduceByStandingHeatLoss(this.getStepSize());
        this.incrementInterdependentTime();
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        return new Schedule(new SparseLoadProfile(), 0, this.getDeviceType().toString());
    }

    @Override
    public String problemToString() {
        return "FIXME FIRST !!!! [" + this.getReferenceTime() + "] ColdWaterTank current temperature = " + (this.waterTank != null ? this.waterTank.getCurrentWaterTemperature() : null);
    }

    @Override
    public ColdWaterTankNonControllableIPP getClone() {
        return new ColdWaterTankNonControllableIPP(this);
    }
}
