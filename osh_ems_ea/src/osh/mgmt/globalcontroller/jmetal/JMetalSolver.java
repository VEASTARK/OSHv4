package osh.mgmt.globalcontroller.jmetal;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.encodings.variable.Binary;
import jmetal.metaheuristics.singleObjective.evolutionStrategy.ElitistES;
import jmetal.operators.mutation.MutationFactory;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.esc.EnergyManagementProblem;
import osh.utils.string.ParameterConstants;

import java.util.*;

/**
 * @author Till Schuberth, Ingo Mauser
 */
public class JMetalSolver extends Optimizer {

    protected final int STEP_SIZE;
    protected final OSHRandomGenerator randomGenerator;
    protected final IGlobalLogger logger;
    protected final boolean showDebugMessages;
    protected int[][] bitPositions;
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
            List<InterdependentProblemPart<?, ?>> problemParts,
            OCEnergySimulationCore ocESC,
            int[][] bitPositions,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            IFitness fitnessFunction) throws Exception {

        this.bitPositions = bitPositions;

        return this.getSolutionAndFitness(
                problemParts,
                ocESC,
                priceSignals,
                powerLimitSignals,
                ignoreLoadProfileBefore,
                fitnessFunction);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SolutionWithFitness getSolutionAndFitness(
            List<InterdependentProblemPart<?, ?>> problemParts,
            OCEnergySimulationCore ocESC,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            IFitness fitnessFunction) throws Exception {

        int mu = 1; // Requirement: lambda must be divisible by mu
        int lambda = 10; // Population size

        int evaluations = 40 * lambda; // Generations = evaluations / lambda

        int numberOfBits = 0;
        for (InterdependentProblemPart<?, ?> i : problemParts) {
            numberOfBits += i.getBitCount();
        }

        // DECLARATION
        EnergyManagementProblem problem;            // The problem to solve
        Algorithm algorithm;        // The algorithm to use
        Operator mutation;            // Mutation operator

        HashMap parameters;            // Operator parameters

        // calculate ignoreLoadProfileAfter (Optimizaion Horizon)
        long ignoreLoadProfileAfter = ignoreLoadProfileBefore;
        for (InterdependentProblemPart<?, ?> ex : problemParts) {
            if (ex instanceof ControllableIPP<?, ?>) {
                ignoreLoadProfileAfter = Math.max(ex.getOptimizationHorizon(), ignoreLoadProfileAfter);
            }
        }

        // INITIALIZATION
        problem = new EnergyManagementProblem(
                problemParts,
                ocESC,
                this.bitPositions,
                priceSignals,
                powerLimitSignals,
                ignoreLoadProfileBefore,
                ignoreLoadProfileAfter,
                this.randomGenerator,
                this.logger,
                fitnessFunction,
                this.STEP_SIZE);

        // SHORT CUT IFF NOTHING HAS TO BE OPTIMIZED
        if (numberOfBits == 0) {
            Solution solution = new Solution(problem);
            problem.evaluate(solution);

            SolutionWithFitness result = new SolutionWithFitness(new BitSet(), Collections.emptyList(), solution.getFitness());

            //better be sure
            problem.finalizeGrids();

            return result;
        }

        algorithm = new ElitistES(problem, mu, lambda, this.showDebugMessages);
        //algorithm = new NonElitistES(problem, mu, lambda);

        /* Algorithm parameters */
        algorithm.setInputParameter(ParameterConstants.EA.maxEvaluations, evaluations);

        /* Mutation and Crossover for Real codification */
        parameters = new HashMap();
        parameters.put(ParameterConstants.EA.probability, 1.0 / 30);
        mutation = MutationFactory.getMutationOperator(
                "BitFlipMutation",
                parameters,
                this.randomGenerator);

        algorithm.addOperator(ParameterConstants.EA.mutation, mutation);

        /* Execute the Algorithm */
        SolutionSet population = algorithm.execute();

        Binary s = (Binary) population.best(this.fitnessComparator).getDecisionVariables()[0];

        int bitPos = 0;
        ArrayList<BitSet> resultBitSet = new ArrayList<>();
        for (InterdependentProblemPart<?, ?> part : problemParts) {
            resultBitSet.add(s.bits_.get(bitPos, bitPos + part.getBitCount()));
            bitPos += part.getBitCount();
        }
        if (bitPos < s.bits_.length()) {
            throw new NullPointerException("Conflict: Solution has more bits then needed for IPP");
        }

        double returnFitness = population.best(this.fitnessComparator).getObjective(0);

        SolutionWithFitness result = new SolutionWithFitness(s.bits_, resultBitSet, returnFitness);

        //better be sure
        problem.finalizeGrids();

        return result;
    }

}

