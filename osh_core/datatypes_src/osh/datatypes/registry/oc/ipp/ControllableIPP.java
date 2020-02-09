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
 * @author Sebastian Kramer, Ingo Mauser
 */
public abstract class ControllableIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends InterdependentProblemPart<PhenotypeType, PredictionType> {

    private static final long serialVersionUID = 7350946372573454658L;

    /**
     * absolute time in unix time [s]
     */
    private long optimizationHorizon;


    /**
     * CONSTRUCTOR for serialization usage only, do not use
     */
    @Deprecated
    protected ControllableIPP() {
        super();
    }

    /**
     * CONSTRUCTOR
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

    public ControllableIPP(ControllableIPP<PhenotypeType, PredictionType> other) {
        super(other);
        this.optimizationHorizon = other.optimizationHorizon;
    }

    @Override
    public long getOptimizationHorizon() {
        return this.optimizationHorizon;
    }

    public void setOptimizationHorizon(long optimizationHorizon) {
        this.optimizationHorizon = optimizationHorizon;
    }

    public abstract String solutionToString();

    @Override
    public PredictionType transformToFinalInterdependentPrediction() {
        return null;
    }

}
