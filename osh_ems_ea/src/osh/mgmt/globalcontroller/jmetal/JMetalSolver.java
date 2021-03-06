package osh.mgmt.globalcontroller.jmetal;

import org.uma.jmetal.solution.Solution;
import osh.core.OSHRandom;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.Comparator;
import java.util.EnumMap;

/**
 * @author Till Schuberth, Ingo Mauser
 */
public class JMetalSolver extends Optimizer {

    protected final int STEP_SIZE;
    protected final OSHRandom randomGenerator;
    protected final IGlobalLogger logger;
    protected final boolean showDebugMessages;
    protected final Comparator<Solution<?>> fitnessComparator = (o1, o2) -> {
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
            OSHRandom randomGenerator,
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
            IFitness fitnessFunction, IEALogger eaLogger) throws Exception {

        return this.getSolutionAndFitness(
                problemParts,
                ocESC,
                priceSignals,
                powerLimitSignals,
                ignoreLoadProfileBefore,
                fitnessFunction,
                eaLogger);
    }

    public SolutionWithFitness getSolutionAndFitness (
            InterdependentProblemPart<?, ?>[] problemParts,
            OCEnergySimulationCore ocESC,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            IFitness fitnessFunction, IEALogger eaLogger) throws Exception {
        throw new UnsupportedOperationException("not implemented here");
    }

}

