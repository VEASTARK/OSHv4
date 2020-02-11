package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;

import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a problem-part for a non-controllable device.
 *
 * @author Sebastian Kramer, Ingo Mauser
 */
public abstract class NonControllableIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends InterdependentProblemPart<PhenotypeType, PredictionType> {

    /**
     * Constructs this non-controllable problem-part with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param needsAncillaryMeterState if this problem-part needs the virtual ancillary meter for it's calculation
     * @param reactsToInputStates if this problem-part  reacts to any input information inside the optimization loop
     * @param isCompletelyStatic if this problem part is completely static and will not provide any input/output
     * @param deviceType type of device represented by this problem-part
     * @param allOutputCommodities set of all energy types this problem-part produces or consumes.
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public NonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            boolean isCompletelyStatic,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                timestamp,
                toBeScheduled,
                needsAncillaryMeterState,
                reactsToInputStates,
                isCompletelyStatic,
                deviceType,
                allOutputCommodities,
                compressionType,
                compressionValue,
                null,
                null);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given non-controllable problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the non-controllable problem-part to copy
     */
    public NonControllableIPP(NonControllableIPP<PhenotypeType, PredictionType> other) {
        super(other);
    }

    @Override
    public final PhenotypeType transformToFinalInterdependentPhenotype() {
        return null;
    }

    @Override
    public PredictionType transformToFinalInterdependentPrediction() {
        return null;
    }

    @Override
    public final void setSolution(BitSet solution) {
        //should not be called but to be safe we ensure that nothing is happening if it is
    }

    @Override
    public final void setSolution(double[] solution) {
        //should not be called but to be safe we ensure that nothing is happening if it is
    }

    @Override
    protected final void interpretNewSolution() {
        //should not be called but to be safe we ensure that nothing is happening if it is
    }

    @Override
    public long getOptimizationHorizon() {
        return 0;
    }
}
