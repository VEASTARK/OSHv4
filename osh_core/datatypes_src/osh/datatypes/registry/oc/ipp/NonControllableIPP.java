package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Sebastian Kramer, Ingo Mauser
 */
public abstract class NonControllableIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends InterdependentProblemPart<PhenotypeType, PredictionType> {

    private static final long serialVersionUID = -6744029462291912653L;


    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected NonControllableIPP() {
        super();
    }

    /**
     * CONSTRUCTOR
     */
    public NonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            boolean isCompletelyStatic,
            long referenceTime,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                referenceTime,
                toBeScheduled,
                needsAncillaryMeterState,
                reactsToInputStates,
                isCompletelyStatic,
                referenceTime,
                deviceType,
                allOutputCommodities,
                compressionType,
                compressionValue,
                null,
                null);
    }

    @Override
    public final PhenotypeType transformToPhenotype(DecodedSolutionWrapper solution) {
        return null;
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
    public long getOptimizationHorizon() {
        return 0;
    }
}
