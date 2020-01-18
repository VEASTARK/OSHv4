package osh.mgmt.globalcontroller.jmetal.esc;

import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.encodings.variable.Binary;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.OSH_gGAMultiThread;
import jmetal.metaheuristics.stoppingRule.StoppingRule;
import jmetal.metaheuristics.stoppingRule.StoppingRuleFactory;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.GAParameters;
import osh.mgmt.globalcontroller.jmetal.IFitness;
import osh.mgmt.globalcontroller.jmetal.JMetalSolver;
import osh.mgmt.globalcontroller.jmetal.SolutionWithFitness;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
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
            OSHRandomGenerator randomGenerator,
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


    @Override
    public SolutionWithFitness getSolutionAndFitness(
            List<InterdependentProblemPart<?, ?>> problemParts,
            OCEnergySimulationCore ocESC,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            IFitness fitnessFunction) throws Exception {


        //TODO: find something to get a fast fitness
//		// return if all DOF=0
        int numberOfBits = 0;
        for (InterdependentProblemPart<?, ?> i : problemParts) {
            numberOfBits += i.getBitCount();
        }

        // DECLARATION
        EnergyManagementProblem problem;            // The problem to solve
        OSH_gGAMultiThread algorithm;        // The algorithm to use
        Operator mutation;            // Mutation operator
        Operator crossover;
        Operator selection;
//		HashMap parameters;			// Operator parameters

        // calculate ignoreLoadProfileAfter (Optimization Horizon)
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

        PrintWriter pw = new PrintWriter(new FileOutputStream(
                new File(this.gaLogPath),
                true));
        /* initialize algorithm */
        //IMA:
        {
            algorithm = new OSH_gGAMultiThread(problem, true, this.timestamp, pw);
            problem.initMultithreading();
        }

// 		{ 			
//			algorithm = new OSH_gGASingleThread(problem, true, timestamp, pw);
//		}

        // IMA: no longer possible as there is probably a battery storage of smart heater
        // SEKR: batterystorage or smartheater are either smart (and so will have a bitcount) or stupid and will not need optimization
        // SHORT CUT IFF NOTHING HAS TO BE OPTIMIZED
        if (numberOfBits == 0) {
            Solution solution = new Solution(problem);
            problem.evaluate(solution);

            List<BitSet> emptySolution = new ArrayList<>();
            problemParts.forEach(p -> emptySolution.add(new BitSet()));

            SolutionWithFitness result = new SolutionWithFitness(new BitSet(), emptySolution, solution.getFitness());

            //better be sure
            problem.finalizeGrids();

            return result;
        }

        /* Algorithm parameters */
        algorithm.setInputParameter("maxEvaluations", this.gaparameters.getNumEvaluations());
        algorithm.setInputParameter("populationSize", this.gaparameters.getPopSize());

        /* Mutation and Crossover for Real codification */
        mutation = MutationFactory.getMutationOperator(
                this.gaparameters.getMutationOperator(),
                this.gaparameters.getMutationParameters(),
                this.randomGenerator);


        //crossover
        crossover = CrossoverFactory.getCrossoverOperator(
                this.gaparameters.getCrossoverOperator(),
                this.gaparameters.getCrossoverParameters(),
                this.randomGenerator);


        //selection
        selection = SelectionFactory.getSelectionOperator(
                this.gaparameters.getSelectionOperator(),
                this.gaparameters.getSelectionParameters(),
                this.randomGenerator);


        //add the operators
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        //add stopping rules
        for (String ruleName : this.gaparameters.getStoppingRules().keySet()) {
            StoppingRule sr = StoppingRuleFactory.getStoppingRule(ruleName, this.gaparameters.getStoppingRules().get(ruleName));
            algorithm.addStoppingRule(sr);
        }

        /* Execute the Algorithm */
        SolutionSet population = algorithm.execute();

        Binary s = (Binary) population.best(this.fitnessComparator).getDecisionVariables()[0];

        ArrayList<BitSet> resultBitSet = new ArrayList<>();
        for (InterdependentProblemPart<?, ?> part : problemParts) {
            int bitPos = this.bitPositions[part.getId()][0];
            int bitPosEnd = this.bitPositions[part.getId()][1];
            resultBitSet.add(s.bits_.get(bitPos, bitPosEnd));
            if ((part.getBitCount() == 0 && (bitPosEnd - bitPos) != 0)
                    || (part.getBitCount() > 0 && (bitPosEnd - bitPos) != part.getBitCount())) {
                throw new IllegalArgumentException("bit-count mismatch");
            }
        }

        pw.flush();
        pw.close();

        if (true) { // debug
            for (int i = 0; i < population.size(); i++) {
                Solution solution = population.get(i);
                this.logger.logDebug("Final Fitness: " + solution.getObjective(0)); //
//				if (solution.getObjective(0) == 0) {
//					System.out.println();
//				}
            }
        }

        double returnFitness = population.best(this.fitnessComparator).getObjective(0);

        //better be sure
        problem.finalizeGrids();

        return new SolutionWithFitness(s.bits_, resultBitSet, returnFitness);
    }
}

