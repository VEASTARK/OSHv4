package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;

import java.time.ZonedDateTime;
import java.util.BitSet;
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
            ZonedDateTime timeStamp,
            DeviceTypes deviceType,
            Commodity[] allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                timeStamp,
                0,
                toBeScheduled,
                needsAncillaryMeterState,
                reactsToInputStates,
                isCompletelyStatic,
                timeStamp.toEpochSecond(),
                deviceType,
                allOutputCommodities,
                compressionType,
                compressionValue);
    }

    public NonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            boolean isCompletelyStatic,
            ZonedDateTime timeStamp,
            DeviceTypes deviceType,
            Commodity[] allOutputCommodities,
            Commodity[] allInputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                timeStamp,
                0,
                toBeScheduled,
                needsAncillaryMeterState,
                reactsToInputStates,
                isCompletelyStatic,
                timeStamp.toEpochSecond(),
                deviceType,
                allOutputCommodities,
                allInputCommodities,
                compressionType,
                compressionValue);
    }


    @Override
    public final void setBitCount(int bitCount) {
        if (bitCount != 0) throw new IllegalArgumentException("bit-count != 0");
    }

    @Override
    public final PhenotypeType transformToPhenotype(BitSet solution) {
        return null;
    }

    @Override
    public final PhenotypeType transformToFinalInterdependentPhenotype(BitSet solution) {
        return null;
    }

    @Override
    public PredictionType transformToFinalInterdependentPrediction(BitSet solution) {
        return null;
    }

    @Override
    public long getOptimizationHorizon() {
        return 0;
    }
}
