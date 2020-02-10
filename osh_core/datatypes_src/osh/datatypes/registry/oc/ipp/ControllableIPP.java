package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.AbstractVariableTranslator;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.BinaryVariableTranslator;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.RealVariableTranslator;

import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a problem-part for a controllable device.
 *
 * @author Sebastian Kramer, Ingo Mauser
 */
public abstract class ControllableIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends InterdependentProblemPart<PhenotypeType, PredictionType> {

    /**
     * absolute time in unix time [s]
     */
    private long optimizationHorizon;

    /**
     * Constructs this controllable problem-part with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param needsAncillaryMeterState if this problem-part needs the virtual ancillary meter for it's calculation
     * @param reactsToInputStates if this problem-part  reacts to any input information inside the optimization loop
     * @param optimizationHorizon the optimization horizon
     * @param deviceType type of device represented by this problem-part
     * @param allOutputCommodities set of all energy types this problem-part produces or consumes.
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public ControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            long optimizationHorizon,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId, timestamp, toBeScheduled, needsAncillaryMeterState,
                reactsToInputStates, false, deviceType, allOutputCommodities, compressionType,
                compressionValue, new BinaryVariableTranslator(), new RealVariableTranslator());
        this.optimizationHorizon = optimizationHorizon;
    }

    /**
     * Constructs this controllable problem-part with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param needsAncillaryMeterState if this problem-part needs the virtual ancillary meter for it's calculation
     * @param reactsToInputStates if this problem-part  reacts to any input information inside the optimization loop
     * @param optimizationHorizon
     * @param deviceType type of device represented by this problem-part
     * @param allOutputCommodities set of all energy types this problem-part produces or consumes.
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     * @param binaryTranslator variable translator and information collector for a binary encoding
     * @param realTranslator variable translator and information collector for a real encoding
     */
    public ControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            long optimizationHorizon,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            EnumSet<Commodity> allInputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue,
            AbstractVariableTranslator<BitSet> binaryTranslator,
            AbstractVariableTranslator<double[]> realTranslator) {
        super(deviceId, timestamp, toBeScheduled, needsAncillaryMeterState,
                reactsToInputStates, false, deviceType, allOutputCommodities,
                compressionType, compressionValue, binaryTranslator, realTranslator);
        this.optimizationHorizon = optimizationHorizon;
        this.setAllInputCommodities(allInputCommodities);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given controllable problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the controllable problem-part to copy
     */
    public ControllableIPP(ControllableIPP<PhenotypeType, PredictionType> other) {
        super(other);
        this.optimizationHorizon = other.optimizationHorizon;
    }

    @Override
    public long getOptimizationHorizon() {
        return this.optimizationHorizon;
    }

    /**
     * Sets the optimization horizon to the given one.
     *
     * @param optimizationHorizon the new optimization horizon
     */
    public void setOptimizationHorizon(long optimizationHorizon) {
        this.optimizationHorizon = optimizationHorizon;
    }

    @Override
    public PredictionType transformToFinalInterdependentPrediction() {
        return null;
    }
}
