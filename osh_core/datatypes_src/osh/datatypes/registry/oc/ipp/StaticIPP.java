package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a problem-part for a a fully static device.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public abstract class StaticIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends NonControllableIPP<PhenotypeType, PredictionType> {

    private final Schedule schedule;
    private final String description;


    /**
     * Constructs this static problem-part with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param deviceType type of device represented by this problem-part
     * @param schedule the final schedule of the underlying device
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     * @param description a descprition of this problem-part
     */
    public StaticIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            Schedule schedule,
            LoadProfileCompressionTypes compressionType,
            int compressionValue,
            String description) {

        super(
                deviceId,
                timestamp,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                false, //does not react to input states
                true, //is static
                deviceType,
                EnumSet.noneOf(Commodity.class),
                compressionType,
                compressionValue);

        this.schedule = schedule;
        this.description = description;
    }

    /**
     * Limited copy-constructor that constructs a copy of the given static problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the static problem-part to copy
     */
    public StaticIPP(StaticIPP<PhenotypeType, PredictionType> other) {
        super(other);
        this.schedule = other.schedule;
        this.description = other.description;
    }

    @Override
    public final void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {
        super.initializeInterdependentCalculation(interdependentStartingTime, stepSize, createLoadProfile, keepPrediction);
        // do nothing
    }

    @Override
    public final void calculateNextStep() {
        // do nothing
    }

    @Override
    public final Schedule getFinalInterdependentSchedule() {
        return this.schedule;
    }

    @Override
    public final void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
    }

    @Override
    public final String problemToString() {
        return "[" + this.getReferenceTime() + "] " + this.description;
    }
}
