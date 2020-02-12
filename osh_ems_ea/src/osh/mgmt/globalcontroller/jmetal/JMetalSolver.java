package osh.mgmt.globalcontroller.jmetal;

import jmetal.core.Solution;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.utils.string.ParameterConstants;

import java.util.Comparator;
import java.util.EnumMap;

/**
 * @author Till Schuberth, Ingo Mauser
 */
public class JMetalSolver extends Optimizer {

    protected final int STEP_SIZE;
    protected final OSHRandomGenerator randomGenerator;
    protected final IGlobalLogger logger;
    protected final boolean showDebugMessages;
    protected final Comparator<Solution> fitnessComparator = (o1, o2) -> {
        double v1 = o1.getObjective(0), v2 = o2.getObjective(0);
        return Double.compare(v1, v2);
    };


    /**
     * CONSTRUCTOR
     *
     * @param globalLogger
     * @param randomGenerator
     * @param showDebugMessages
     */
    public JMetalSolver(
            IGlobalLogger globalLogger,
            OSHRandomGenerator randomGenerator,
            boolean showDebugMessages,
            int STEP_SIZE) {
        this.logger = globalLogger;
        this.randomGenerator = randomGenerator;
        this.showDebugMessages = showDebugMessages;
        this.STEP_SIZE = STEP_SIZE;
    }


    public SolutionWithFitness getSolution(
            InterdependentProblemPart<?, ?>[] problemParts,
            OCEnergySimulationCore ocESC,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            IFitness fitnessFunction) throws Exception {

        return this.getSolutionAndFitness(
                problemParts,
                ocESC,
                priceSignals,
                powerLimitSignals,
                ignoreLoadProfileBefore,
                fitnessFunction);
    }

    public SolutionWithFitness getSolutionAndFitness (
            InterdependentProblemPart<?, ?>[] problemParts,
            OCEnergySimulationCore ocESC,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            IFitness fitnessFunction) throws Exception {
        throw new UnsupportedOperationException("not implemented here");
    }

}

