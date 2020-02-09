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
 * IPP for devices without any interaction (e.g., appliance is off)
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public abstract class StaticIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends NonControllableIPP<PhenotypeType, PredictionType> {

    private static final long serialVersionUID = -8858902765784429939L;

    private Schedule schedule;
    private String description;


    /**
     * CONSTRUCTOR for serialization usage only, do not use
     */
    @Deprecated
    protected StaticIPP() {
        super();
    }

    /**
     * CONSTRUCTOR
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
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                false, //does not react to input states
                true, //is static
                timestamp,
                deviceType,
                EnumSet.noneOf(Commodity.class),
                compressionType,
                compressionValue);

        this.schedule = schedule;
        this.description = description;
    }

    public StaticIPP(StaticIPP<PhenotypeType, PredictionType> other) {
        super(other);
        this.schedule = other.schedule;
        this.description = other.description;
    }


    @Override
    public final void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {
        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);
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

    // ### to string ###

    @Override
    public final String problemToString() {
        return "[" + this.getReferenceTime() + "] " + this.description;
    }

}
