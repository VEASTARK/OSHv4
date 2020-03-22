package osh.mgmt.globalcontroller.jmetal.esc;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.OSHLegacyGenerationalGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRuleFactory;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.CrossoverFactory;
import org.uma.jmetal.operator.impl.crossover.CrossoverType;
import org.uma.jmetal.operator.impl.mutation.MutationFactory;
import org.uma.jmetal.operator.impl.mutation.MutationType;
import org.uma.jmetal.operator.impl.selection.SelectionFactory;
import org.uma.jmetal.operator.impl.selection.SelectionType;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedStealingSolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.point.PointSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.impl.OSHPseudoRandom;
import osh.core.OSHRandom;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.GAParameters;
import osh.mgmt.globalcontroller.jmetal.JMetalSolver;
import osh.mgmt.globalcontroller.jmetal.SolutionWithFitness;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;
import osh.utils.costs.OptimizationCostFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * New JMetalEnergySolverGA
 *
 * @author Ingo Mauser, Till Schuberth, Sebastian Kramer
 */
public class JMetalEnergySolverGA extends JMetalSolver {

    private final GAParameters gaparameters;
    private final long timestamp;
    private final String gaLogPath;

    /**
     * CONSTRUCTOR
     *
     * @param globalLogger
     * @param randomGenerator
     * @param showDebugMessages
     * @param parameters
     */
    public JMetalEnergySolverGA(
            IGlobalLogger globalLogger,
            OSHRandom randomGenerator,
            boolean showDebugMessages,
            GAParameters parameters,
            long timestamp,
            int STEP_SIZE,
            String logDir) {
        super(globalLogger, randomGenerator, showDebugMessages, STEP_SIZE);

        this.gaparameters = parameters.clone();
        this.timestamp = timestamp;
        File f = new File(logDir);
        if (!f.exists())
            f.mkdirs();

        this.gaLogPath = logDir + "/gaLog.txt";
    }


    @SuppressWarnings("unchecked")
    @Override
    public SolutionWithFitness getSolutionAndFitness(
            InterdependentProblemPart<?, ?>[] problemParts,
            OCEnergySimulationCore ocESC,
            long ignoreLoadProfileBefore,
            OptimizationCostFunction costFunction,
            IEALogger eaLogger) throws FileNotFoundException {


        // DECLARATION
        SolutionDistributor distributor = new SolutionDistributor();
        boolean useMultithreading = true;
        Algorithm<BinarySolution> algorithm;
        MutationOperator<BinarySolution> mutation;            // Mutation operator
        CrossoverOperator<BinarySolution> crossover;
        SelectionOperator<List<BinarySolution>, BinarySolution> selection;
//		HashMap parameters;			// Operator parameters

        distributor.gatherVariableInformation(problemParts);

        //abort if there is nothing to optimize
        if (distributor.getVariableInformation(VariableEncoding.BINARY).needsNoVariables()) {
            PointSolution emptySolution = new PointSolution(1);
            emptySolution.setObjective(0, 0.0);
            return new SolutionWithFitness(emptySolution, 0.0);
        }

        // calculate ignoreLoadProfileAfter (Optimization Horizon)
        long ignoreLoadProfileAfter = ignoreLoadProfileBefore;
        for (InterdependentProblemPart<?, ?> ex : problemParts) {
            if (ex instanceof ControllableIPP<?, ?>) {
                ignoreLoadProfileAfter = Math.max(ex.getOptimizationHorizon(), ignoreLoadProfileAfter);
            }
        }

        // INITIALIZATION
        EMProblemEvaluator evaluator = new EMProblemEvaluator(
                problemParts,
                ocESC,
                distributor,
                ignoreLoadProfileBefore,
                ignoreLoadProfileAfter,
                costFunction,
                eaLogger,
                this.STEP_SIZE);

        Problem<BinarySolution> binaryProblem = new BinaryEnergyManagementProblem(
                evaluator,
                distributor);

        JMetalRandom.getInstance().setRandomGenerator(new OSHPseudoRandom(this.randomGenerator));

        PrintWriter pw = new PrintWriter(new FileOutputStream(
                new File(this.gaLogPath),
                true));


        /* Mutation and Crossover for Real codification */
        mutation = MutationFactory.getMutationOperator(
                MutationType.fromName(this.gaparameters.getMutationOperator()),
                this.gaparameters.getMutationParameters());


        //crossover
        crossover = CrossoverFactory.getCrossoverOperator(
                CrossoverType.fromName(this.gaparameters.getCrossoverOperator()),
                this.gaparameters.getCrossoverParameters());


        //selection
        selection = SelectionFactory.getSelectionOperator(
                SelectionType.fromName(this.gaparameters.getSelectionOperator()),
                this.gaparameters.getSelectionParameters());

        SolutionListEvaluator<BinarySolution> algorithmEvaluator;

        if (useMultithreading) {
            algorithmEvaluator = new MultithreadedStealingSolutionListEvaluator<>();
            evaluator.initializeMultithreading();
        } else {
            algorithmEvaluator = new SequentialSolutionListEvaluator<>();
        }

        eaLogger.attachWriter(pw);
        eaLogger.setTimestamp(ignoreLoadProfileBefore);

        algorithm = new OSHLegacyGenerationalGeneticAlgorithm<>(binaryProblem, this.gaparameters.getPopSize(), crossover,
                mutation, selection, algorithmEvaluator, eaLogger);

        //add stopping rules
        for (String ruleName : this.gaparameters.getStoppingRules().keySet()) {
            StoppingRule sr = StoppingRuleFactory.getStoppingRule(ruleName, this.gaparameters.getStoppingRules().get(ruleName));
            algorithm.addStoppingRule(sr);
        }

        /* Execute the Algorithm */
        algorithm.run();
        BinarySolution solution = algorithm.getResult();

        eaLogger.logEnd(solution);

        if (true) { // debug
            this.logger.logDebug("Final Fitness: " + solution.getObjective(0)); //
        }

        eaLogger.detachWriter();

        //better be sure
        evaluator.finalizeGrids();

        return new SolutionWithFitness(solution, solution.getObjective(0));
    }

}

