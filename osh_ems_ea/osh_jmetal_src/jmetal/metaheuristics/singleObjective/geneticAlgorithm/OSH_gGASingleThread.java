//  gGA.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.metaheuristics.singleObjective.geneticAlgorithm;

import jmetal.core.*;
import jmetal.encodings.variable.Binary;
import jmetal.metaheuristics.stoppingRule.StoppingRule;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;
import osh.configuration.system.DeviceTypes;
import osh.mgmt.globalcontroller.jmetal.esc.EnergyManagementProblem;
import osh.simulation.DatabaseLoggerThread;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ForkJoinTask;

/**
 * Class implementing a generational genetic algorithm
 */
@SuppressWarnings("unused")
public class OSH_gGASingleThread extends Algorithm {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static boolean log;
    private static double generationsUsed;
    private static double[] fitnessChange;
    private static double[] fitnessSpread;
    private static double[] homogeneity;
    private static int optimizationCounter;
    private static double[][] cervisia;
    private final List<StoppingRule> stoppingRules = new ArrayList<>();
    private final boolean showDebugMessages;
    private final long timestamp;
    private final PrintWriter pw;

    /**
     * Constructor
     * Create a new GGA instance.
     *
     * @param problem Problem to solve.
     */
    public OSH_gGASingleThread(Problem problem, boolean showDebugMessages, long timestamp, PrintWriter pw) {
        super(problem);
        this.showDebugMessages = showDebugMessages;
        this.timestamp = timestamp;
        this.pw = pw;
        pw.println("[" + timestamp + "] : [===    New Optimization    ===]");
    } // GGA

    public static void shutdown() {

        if (log) {
            generationsUsed /= optimizationCounter;
            fitnessChange = Arrays.stream(fitnessChange).map(d -> d / optimizationCounter).toArray();
            fitnessSpread = Arrays.stream(fitnessSpread).map(d -> d / optimizationCounter).toArray();
            homogeneity = Arrays.stream(homogeneity).map(d -> d / optimizationCounter).toArray();
            double[] cervisiaResults = new double[5];
            for (int i = 0; i < cervisiaResults.length; i++) {
                if (cervisia[i][1] != 0) {
                    cervisiaResults[i] = cervisia[i][0] / cervisia[i][1];
                }
            }

            DatabaseLoggerThread.enqueueGA(generationsUsed, fitnessChange, fitnessSpread, homogeneity, optimizationCounter, cervisiaResults);
        }
    }

    public static void initLogging() {
        OSH_gGASingleThread.log = true;
        OSH_gGASingleThread.generationsUsed = 0;
        OSH_gGASingleThread.fitnessChange = new double[20];
        OSH_gGASingleThread.fitnessSpread = new double[20];
        OSH_gGASingleThread.homogeneity = new double[20];

        OSH_gGASingleThread.optimizationCounter = 0;
        cervisia = new double[5][2];
    }

    public static void logCervisia(DeviceTypes type, double cervisia) {
        if (type == DeviceTypes.CHPPLANT) {
            OSH_gGASingleThread.cervisia[0][0] += cervisia;
            OSH_gGASingleThread.cervisia[0][1]++;
        } else if (type == DeviceTypes.HOTWATERSTORAGE) {
            OSH_gGASingleThread.cervisia[1][0] += cervisia;
            OSH_gGASingleThread.cervisia[1][1]++;
        } else if (type == DeviceTypes.DISHWASHER) {
            OSH_gGASingleThread.cervisia[2][0] += cervisia;
            OSH_gGASingleThread.cervisia[2][1]++;
        } else if (type == DeviceTypes.DRYER) {
            OSH_gGASingleThread.cervisia[3][0] += cervisia;
            OSH_gGASingleThread.cervisia[3][1]++;
        } else if (type == DeviceTypes.WASHINGMACHINE) {
            OSH_gGASingleThread.cervisia[4][0] += cervisia;
            OSH_gGASingleThread.cervisia[4][1]++;
        }
    }

    /**
     * Execute the GGA algorithm
     *
     * @throws JMException
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public SolutionSet execute() throws JMException {
        int generation = 0;
        int populationSize;
        int maxEvaluations;
        int evaluations;

        SolutionSet population;
        SolutionSet offspringPopulation;

        Operator mutationOperator;
        Operator crossoverOperator;
        Operator selectionOperator;

        Comparator<Object> comparator;
        comparator = new ObjectiveComparator(0); // Single objective comparator

        // Read the params
        populationSize = (Integer) this.getInputParameter("populationSize");
        maxEvaluations = (Integer) this.getInputParameter("maxEvaluations");

        // Initialize the variables
        population = new SolutionSet(populationSize);
        offspringPopulation = new SolutionSet(populationSize);

        evaluations = 0;

        // Read the operators
        mutationOperator = this.operators_.get("mutation");
        crossoverOperator = this.operators_.get("crossover");
        selectionOperator = this.operators_.get("selection");

        // Create the initial population
        Solution newIndividual;
        for (int i = 0; i < populationSize; i++) {
            newIndividual = new Solution(this.problem_);
            evaluations++;
            population.add(newIndividual);
        } //for

        this.evaluatePopulation(population);

        // Sort population
        population.sort(comparator);
        double bestFirstFitness = 0;
        if (log) {
            fitnessChange[0] += 1;
            bestFirstFitness = population.get(0).getFitness();
            double[] values = this.getHomogeneityValues(population);
            homogeneity[0] += values[0];
            fitnessSpread[0] += values[2];
        }


        boolean stoppingRulesReached = false;
        do {
            if ((evaluations % (populationSize * 10)) == 0) {
                String msg = "[" + evaluations + "] Fitness: "
                        + population.get(0).getObjective(0);
                this.pw.println(msg);
                if (this.showDebugMessages) {
                    System.out.println(msg);

                }
            } //

            if ((evaluations % (populationSize * 10)) == 0) {
                String msg = this.printHomogeneity(population, evaluations);
                this.pw.println(msg);
                if (this.showDebugMessages) {
                    System.out.println(msg);
                }
            } //

            // Copy the best two individuals to the offspring population
            offspringPopulation.add(new Solution(population.get(0)));
            offspringPopulation.add(new Solution(population.get(1)));

            evaluations += 2; // To get back right number of Evaluations

            // Reproductive cycle
            LinkedList<Evaluator> evaluators = new LinkedList<>();
            for (int i = 0; i < ((populationSize / 2) - 1); i++) {
                // Selection
                Solution[] parents = new Solution[2];

                Object selected = selectionOperator.execute(population);

                if (selected instanceof Solution[])
                    parents = (Solution[]) selected;
                else if (selected instanceof Solution) {
                    parents[0] = (Solution) selected;
                    parents[1] = (Solution) selectionOperator.execute(population);
                } else {
                    throw new JMException("Selection operator does not return a Solution object");
                }

                // <Workaround for 0 bits>
                int totalNumberOfBits = 0;
                for (int v = 0; v < parents[0].getDecisionVariables().length; v++) {
                    totalNumberOfBits += ((Binary) parents[0]
                            .getDecisionVariables()[v]).getNumberOfBits();
                }

                Solution[] offspring;
                if (totalNumberOfBits == 0) {
                    offspring = new Solution[2];
                    offspring[0] = parents[0];
                    offspring[1] = parents[1];
                } else {
                    offspring = (Solution[]) crossoverOperator.execute(parents);
                }

                // Mutation
                offspring[0] = (Solution) mutationOperator.execute(offspring[0]);
                offspring[1] = (Solution) mutationOperator.execute(offspring[1]);

                // Evaluation of the new individual (do it asynchronous)
                //      problem_.evaluate(offspring[0]);
                //      problem_.evaluate(offspring[1]);
                for (int j = 0; j < 2; j++) {
                    Evaluator eval = new Evaluator(this.problem_, offspring[j], i * 2 + j);
                    evaluators.add(eval);
                }
            }

            try {

                for (Evaluator eval : evaluators) {

                    eval.exec();
                    offspringPopulation.add(eval.counter, eval.result);
                    evaluations++;
                } // for

            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            // The offspring population becomes the new current population
            population.clear();
            for (int i = 0; i < populationSize; i++) {
                population.add(offspringPopulation.get(i));
            }
            offspringPopulation.clear();
            population.sort(comparator);
            generation++;

            if (log && generation < 20) {
                fitnessChange[generation] += population.get(0).getFitness() / bestFirstFitness;
                double[] values = this.getHomogeneityValues(population);
                homogeneity[generation] += values[0];
                fitnessSpread[generation] += values[2];
            }

            for (StoppingRule sr : this.stoppingRules) {
                if (sr.checkIfStop(this.problem_, generation, population)) {
                    stoppingRulesReached = true;
                    this.pw.println(sr.getMsg());
                    System.out.println(sr.getMsg());
                    break;
                }
            }

        } while (!stoppingRulesReached); // stopping rules will decide when to break out of the cycle

        // Return a population with the best individual
        SolutionSet resultPopulation = new SolutionSet(1);
        resultPopulation.add(population.get(0));

        ((EnergyManagementProblem) this.problem_).evaluateFinalTime(population.get(0), log);

        if (log) {
            generationsUsed += generation;
            optimizationCounter++;
        }

        String msg = "["
                + (evaluations + "] Final Fitness: " + population.get(0)
                .getObjective(0));
        this.pw.println(msg);
        if (this.showDebugMessages) {
            System.out.println(msg);
        }
        return resultPopulation;
    } // execute

    public void addStoppingRule(StoppingRule sr) {
        this.stoppingRules.add(sr);
    }

    private void evaluatePopulation(SolutionSet population) {

        LinkedList<Evaluator> evaluators = new LinkedList<>();

        for (int j = 0; j < population.size(); j++) {
            Evaluator eval = new Evaluator(this.problem_, population.get(j), j);
            evaluators.add(eval);
        }

        for (Evaluator eval : evaluators) {

            eval.exec();
        } // for
    }

    private String printHomogeneity(SolutionSet population, int evaluations) {
        double[] values = this.getHomogeneityValues(population);
        return "[" + evaluations + "] Homogeneity mean: " + values[0] + ", max: " + values[1] + ", deltaFitnessSpread: " + values[2];
    }

    private double[] getHomogeneityValues(SolutionSet population) {
        BitSet best = ((Binary) population.get(0).getDecisionVariables()[0]).bits_;
        double bestFitness = population.get(0).getFitness();
        int numberOfBits = this.problem_.getNumberOfBits();
        double max = Double.MIN_VALUE;
        double homogeneitySum = 0.0;
        double deltaFitnessSum = 0.0;

        for (int i = 0; i < population.size(); i++) {
            BitSet act = ((Binary) population.get(i).getDecisionVariables()[0]).bits_;
            BitSet diff = ((BitSet) act.clone());
            diff.xor(best);
            double diffBits = 0.0;
            for (int j = diff.nextSetBit(0); j >= 0; j = diff.nextSetBit(j + 1)) {
                diffBits++;
            }
            double homogeneity = diffBits / numberOfBits;
            if (max < homogeneity)
                max = homogeneity;
            homogeneitySum += homogeneity;
            deltaFitnessSum += (population.get(i).getFitness() / bestFitness) - 1;

        }
        homogeneitySum /= population.size();
        deltaFitnessSum /= population.size();

        return new double[]{homogeneitySum, max, deltaFitnessSum};
    }

    private static class Evaluator extends ForkJoinTask<Solution> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final Problem problem;
        private final Solution solution;
        private Solution result;
        private JMException exception;
        private final int counter;

        public Evaluator(Problem problem, Solution solution, int counter) {
            this.problem = problem;
            this.solution = solution;
            this.counter = counter;
        }

        @Override
        public Solution getRawResult() {
            return this.result; // return null as long as the thread is running
        }

        @Override
        protected void setRawResult(Solution value) {
            this.result = this.solution;
        }

        @Override
        protected boolean exec() {
            try {
                this.problem.evaluate(this.solution);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
            }

            this.result = this.solution;
            return true;
        }

    }

} // gGA