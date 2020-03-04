package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class implementing the MOEA/D-STM algorithm described in : K. Li, Q. Zhang, S. Kwong, M. Li and
 * R. Wang, "Stable Matching-Based Selection in Evolutionary Multiobjective Optimization", IEEE
 * Transactions on Evolutionary Computation, 18(6): 909-923, 2014. DOI: 10.1109/TEVC.2013.2293776
 *
 * @author Ke Li
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MOEADSTM extends AbstractMOEAD<DoubleSolution> {

    protected final DifferentialEvolutionCrossover differentialEvolutionCrossover;

    protected final DoubleSolution[] savedValues;
    protected final double[] utility;
    protected final int[] frequency;

    final JMetalRandom randomGenerator;

    public MOEADSTM(Problem<DoubleSolution> problem, int populationSize, int resultPopulationSize,
                    int maxEvaluations,
                    MutationOperator<DoubleSolution> mutation, CrossoverOperator<DoubleSolution> crossover,
                    FunctionType functionType, String dataDirectory, double neighborhoodSelectionProbability,
                    int maximumNumberOfReplacedSolutions, int neighborSize) {
        super(problem, populationSize, resultPopulationSize, maxEvaluations, crossover, mutation,
                functionType,
                dataDirectory, neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions,
                neighborSize);

        this.differentialEvolutionCrossover = (DifferentialEvolutionCrossover) this.crossoverOperator;

        this.savedValues = new DoubleSolution[populationSize];
        this.utility = new double[populationSize];
        this.frequency = new int[populationSize];
        for (int i = 0; i < this.utility.length; i++) {
            this.utility[i] = 1.0;
            this.frequency[i] = 0;
        }

        this.randomGenerator = JMetalRandom.getInstance();
    }

    @Override
    public void run() {
        this.initializePopulation();
        this.initializeUniformWeight();
        this.initializeNeighborhood();
        this.idealPoint.update(this.population);
        this.nadirPoint.update(this.population);

        int generation = 0;
        this.evaluations = this.populationSize;
        do {
            int[] permutation = new int[this.populationSize];
            MOEADUtils.randomPermutation(permutation, this.populationSize);
            this.offspringPopulation.clear();

            for (int i = 0; i < this.populationSize; i++) {
                int subProblemId = permutation[i];
                this.frequency[subProblemId]++;

                NeighborType neighborType = this.chooseNeighborType();
                List<DoubleSolution> parents = this.parentSelection(subProblemId, neighborType);

                this.differentialEvolutionCrossover.setCurrentSolution(this.population.get(subProblemId));
                List<DoubleSolution> children = this.differentialEvolutionCrossover.execute(parents);

                DoubleSolution child = children.get(0);
                this.mutationOperator.execute(child);
                this.problem.evaluate(child);

                this.evaluations++;

                this.idealPoint.update(this.population);
                this.nadirPoint.update(this.population);
                this.updateNeighborhood(child, subProblemId, neighborType);

                this.offspringPopulation.add(child);
            }

            // Combine the parent and the current offspring populations
            this.jointPopulation.clear();
            this.jointPopulation.addAll(this.population);
            this.jointPopulation.addAll(this.offspringPopulation);

            // selection process
            this.stmSelection();

            generation++;
            if (generation % 30 == 0) {
                this.utilityFunction();
            }

        } while (this.evaluations < this.maxEvaluations);

    }

    protected void initializePopulation() {
        this.population = new ArrayList<>(this.populationSize);
        this.offspringPopulation = new ArrayList<>(this.populationSize);
        this.jointPopulation = new ArrayList<>(this.populationSize);

        for (int i = 0; i < this.populationSize; i++) {
            DoubleSolution newSolution = this.problem.createSolution();

            this.problem.evaluate(newSolution);
            this.population.add(newSolution);
            this.savedValues[i] = (DoubleSolution) newSolution.copy();
        }
    }

    @Override
    public List<DoubleSolution> getResult() {
        return this.population;
    }

    public void utilityFunction() throws JMetalException {
        double f1, f2, uti, delta;
        for (int n = 0; n < this.populationSize; n++) {
            f1 = this.fitnessFunction(this.population.get(n), this.lambda[n]);
            f2 = this.fitnessFunction(this.savedValues[n], this.lambda[n]);
            delta = f2 - f1;
            if (delta > 0.001) {
                this.utility[n] = 1.0;
            } else {
                uti = (0.95 + (0.05 * delta / 0.001)) * this.utility[n];
                this.utility[n] = uti < 1.0 ? uti : 1.0;
            }
            this.savedValues[n] = (DoubleSolution) this.population.get(n).copy();
        }
    }

    public List<Integer> tourSelection(int depth) {
        List<Integer> selected = new ArrayList<>();
        List<Integer> candidate = new ArrayList<>();

        for (int k = 0; k < this.problem.getNumberOfObjectives(); k++) {
            // WARNING! HERE YOU HAVE TO USE THE WEIGHT PROVIDED BY QINGFU Et AL
            // (NOT SORTED!!!!)
            selected.add(k);
        }

        for (int n = this.problem.getNumberOfObjectives(); n < this.populationSize; n++) {
            // set of unselected weights
            candidate.add(n);
        }

        while (selected.size() < (int) (this.populationSize / 5.0)) {
            int best_idd = (int) (this.randomGenerator.nextDouble() * candidate.size());
            int i2;
            int best_sub = candidate.get(best_idd);
            int s2;
            for (int i = 1; i < depth; i++) {
                i2 = (int) (this.randomGenerator.nextDouble() * candidate.size());
                s2 = candidate.get(i2);
                if (this.utility[s2] > this.utility[best_sub]) {
                    best_idd = i2;
                    best_sub = s2;
                }
            }
            selected.add(best_sub);
            candidate.remove(best_idd);
        }
        return selected;
    }

    /**
     * Select the next parent population, based on the stable matching criteria
     */
    public void stmSelection() {

        int[] idx = new int[this.populationSize];
        double[] nicheCount = new double[this.populationSize];

        int[][] solPref = new int[this.jointPopulation.size()][];
        double[][] solMatrix = new double[this.jointPopulation.size()][];
        double[][] distMatrix = new double[this.jointPopulation.size()][];
        double[][] fitnessMatrix = new double[this.jointPopulation.size()][];

        for (int i = 0; i < this.jointPopulation.size(); i++) {
            solPref[i] = new int[this.populationSize];
            solMatrix[i] = new double[this.populationSize];
            distMatrix[i] = new double[this.populationSize];
            fitnessMatrix[i] = new double[this.populationSize];
        }
        int[][] subpPref = new int[this.populationSize][];
        double[][] subpMatrix = new double[this.populationSize][];
        for (int i = 0; i < this.populationSize; i++) {
            subpPref[i] = new int[this.jointPopulation.size()];
            subpMatrix[i] = new double[this.jointPopulation.size()];
        }

        // Calculate the preference values of solution matrix
        for (int i = 0; i < this.jointPopulation.size(); i++) {
            int minIndex = 0;
            for (int j = 0; j < this.populationSize; j++) {
                fitnessMatrix[i][j] = this.fitnessFunction(this.jointPopulation.get(i), this.lambda[j]);
                distMatrix[i][j] = this.calculateDistance2(this.jointPopulation.get(i), this.lambda[j]);
                if (distMatrix[i][j] < distMatrix[i][minIndex]) {
                    minIndex = j;
                }
            }
            nicheCount[minIndex] += 1;
        }

        // calculate the preference values of subproblem matrix and solution matrix
        for (int i = 0; i < this.jointPopulation.size(); i++) {
            for (int j = 0; j < this.populationSize; j++) {
                subpMatrix[j][i] = this.fitnessFunction(this.jointPopulation.get(i), this.lambda[j]);
                solMatrix[i][j] = distMatrix[i][j] + nicheCount[j];
            }
        }

        // sort the preference value matrix to get the preference rank matrix
        for (int i = 0; i < this.populationSize; i++) {
            for (int j = 0; j < this.jointPopulation.size(); j++) {
                subpPref[i][j] = j;
            }
            MOEADUtils.quickSort(subpMatrix[i], subpPref[i], 0, this.jointPopulation.size() - 1);
        }
        for (int i = 0; i < this.jointPopulation.size(); i++) {
            for (int j = 0; j < this.populationSize; j++) {
                solPref[i][j] = j;
            }
            MOEADUtils.quickSort(solMatrix[i], solPref[i], 0, this.populationSize - 1);
        }

        idx = this.stableMatching(subpPref, solPref, this.populationSize, this.jointPopulation.size());

        this.population.clear();
        for (int i = 0; i < this.populationSize; i++) {
            this.population.add(i, this.jointPopulation.get(idx[i]));
        }
    }

    /**
     * Return the stable matching between 'subproblems' and 'solutions' ('subproblems' propose first).
     * It is worth noting that the number of solutions is larger than that of the subproblems.
     */
    public int[] stableMatching(int[][] manPref, int[][] womanPref, int menSize, int womenSize) {

        // Indicates the mating status
        int[] statusMan = new int[menSize];
        int[] statusWoman = new int[womenSize];

        final int NOT_ENGAGED = -1;
        for (int i = 0; i < womenSize; i++) {
            statusWoman[i] = NOT_ENGAGED;
        }

        // List of men that are not currently engaged.
        LinkedList<Integer> freeMen = new LinkedList<>();
        for (int i = 0; i < menSize; i++) {
            freeMen.add(i);
        }

        // next[i] is the next woman to whom i has not yet proposed.
        int[] next = new int[womenSize];

        while (!freeMen.isEmpty()) {
            int m = freeMen.remove();
            int w = manPref[m][next[m]];
            next[m]++;
            if (statusWoman[w] == NOT_ENGAGED) {
                statusMan[m] = w;
                statusWoman[w] = m;
            } else {
                int m1 = statusWoman[w];
                if (this.prefers(m, m1, womanPref[w], menSize)) {
                    statusMan[m] = w;
                    statusWoman[w] = m;
                    freeMen.add(m1);
                } else {
                    freeMen.add(m);
                }
            }
        }

        return statusMan;
    }

    /**
     * Returns true in case that a given woman prefers x to y.
     */
    public boolean prefers(int x, int y, int[] womanPref, int size) {

        for (int i = 0; i < size; i++) {
            int pref = womanPref[i];
            if (pref == x) {
                return true;
            }
            if (pref == y) {
                return false;
            }
        }
        // this should never happen.
        System.out.println("Error in womanPref list!");
        return false;
    }

    /**
     * Calculate the perpendicular distance between the solution and reference line
     */
    public double calculateDistance(DoubleSolution individual, double[] lambda) {
        double scale;
        double distance;

        double[] vecInd = new double[this.problem.getNumberOfObjectives()];
        double[] vecProj = new double[this.problem.getNumberOfObjectives()];

        // vecInd has been normalized to the range [0,1]
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            vecInd[i] = (individual.getObjective(i) - this.idealPoint.getValue(i)) /
                    (this.nadirPoint.getValue(i) - this.idealPoint.getValue(i));
        }

        scale = this.innerproduct(vecInd, lambda) / this.innerproduct(lambda, lambda);
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            vecProj[i] = vecInd[i] - scale * lambda[i];
        }

        distance = this.norm_vector(vecProj);

        return distance;
    }

    /**
     * Calculate the perpendicular distance between the solution and reference line
     */
    public double calculateDistance2(DoubleSolution individual, double[] lambda) {

        double distance;
        double distanceSum = 0.0;

        double[] vecInd = new double[this.problem.getNumberOfObjectives()];
        double[] normalizedObj = new double[this.problem.getNumberOfObjectives()];

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            distanceSum += individual.getObjective(i);
        }
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            normalizedObj[i] = individual.getObjective(i) / distanceSum;
        }
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            vecInd[i] = normalizedObj[i] - lambda[i];
        }

        distance = this.norm_vector(vecInd);

        return distance;
    }

    /**
     * Calculate the norm of the vector
     */
    public double norm_vector(double[] z) {
        double sum = 0;

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            sum += z[i] * z[i];
        }

        return Math.sqrt(sum);
    }

    /**
     * Calculate the dot product of two vectors
     */
    public double innerproduct(double[] vec1, double[] vec2) {
        double sum = 0;

        for (int i = 0; i < vec1.length; i++) {
            sum += vec1[i] * vec2[i];
        }

        return sum;
    }

    @Override
    public String getName() {
        return "MOEADSTM";
    }

    @Override
    public String getDescription() {
        return "Multi-Objective Evolutionary Algorithm based on Decomposition. Version with Stable Matching Model";
    }
}
