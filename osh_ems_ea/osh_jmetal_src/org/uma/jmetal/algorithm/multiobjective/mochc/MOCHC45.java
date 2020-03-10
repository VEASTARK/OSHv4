package org.uma.jmetal.algorithm.multiobjective.mochc;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class executes the MOCHC algorithm described in:
 * A.J. Nebro, E. Alba, G. Molina, F. Chicano, F. Luna, J.J. Durillo
 * "Optimal antenna placement using a new multi-objective chc algorithm".
 * GECCO '07: Proceedings of the 9th annual conference on Genetic and
 * evolutionary computation. London, England. July 2007.
 * <p>
 * Implementation of MOCHC following the scheme used in jMetal4.5 and former versions, i.e, without
 * implementing the {@link AbstractGeneticAlgorithm} interface.
 */
@SuppressWarnings("serial")
public class MOCHC45 implements Algorithm<List<BinarySolution>> {
    private final BinaryProblem problem;
    private final int populationSize;
    private final int maxEvaluations;
    private final int convergenceValue;
    private final double preservedPopulation;
    private final double initialConvergenceCount;
    private final CrossoverOperator<BinarySolution> crossover;
    private final MutationOperator<BinarySolution> cataclysmicMutation;
    private final SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection;
    private final SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection;
    private List<BinarySolution> population;
    private int evaluations;
    private int minimumDistance;
    private int size;
    private Comparator<BinarySolution> comparator;

    private final List<StoppingRule> stoppingRules = new ArrayList<>();

    /**
     * Constructor
     */
    public MOCHC45(BinaryProblem problem, int populationSize, int maxEvaluations, int convergenceValue,
                   double preservedPopulation, double initialConvergenceCount,
                   CrossoverOperator<BinarySolution> crossoverOperator,
                   MutationOperator<BinarySolution> cataclysmicMutation,
                   SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection,
                   SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection,
                   SolutionListEvaluator<BinarySolution> evaluator) {
        super();
        this.problem = problem;
        this.populationSize = populationSize;
        this.maxEvaluations = maxEvaluations;
        this.convergenceValue = convergenceValue;
        this.preservedPopulation = preservedPopulation;
        this.initialConvergenceCount = initialConvergenceCount;
        this.crossover = crossoverOperator;
        this.cataclysmicMutation = cataclysmicMutation;
        this.newGenerationSelection = newGenerationSelection;
        this.parentSelection = parentSelection;
    }

    @Override
    public String getName() {
        return "MOCHC45";
    }

    @Override
    public String getDescription() {
        return "Multiobjective CHC algorithm";
    }

    @Override
    public void run() {
        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            this.size += this.problem.getNumberOfBits(i);
        }
        this.minimumDistance = (int) Math.floor(this.initialConvergenceCount * this.size);

        this.comparator = new CrowdingDistanceComparator<>();

        this.evaluations = 0;
        this.population = new ArrayList<>();
        for (int i = 0; i < this.populationSize; i++) {
            BinarySolution newIndividual = this.problem.createSolution();
            this.problem.evaluate(newIndividual);
            this.population.add(newIndividual);
            this.evaluations++;
        }

        while (!this.isStoppingConditionReached()) {
            List<BinarySolution> offspringPopulation = new ArrayList<>(this.populationSize);
            for (int i = 0; i < this.population.size() / 2; i++) {
                List<BinarySolution> parents = new ArrayList<>(2);
                parents.add(this.parentSelection.execute(this.population));
                parents.add(this.parentSelection.execute(this.population));

                if (this.hammingDistance(parents.get(0), parents.get(1)) >= this.minimumDistance) {
                    List<BinarySolution> offspring = this.crossover.execute(parents);
                    this.problem.evaluate(offspring.get(0));
                    this.problem.evaluate(offspring.get(1));
                    offspringPopulation.add(offspring.get(0));
                    offspringPopulation.add(offspring.get(1));

                    this.evaluations += 2;
                }
            }

            List<BinarySolution> union = new ArrayList<>();
            union.addAll(this.population);
            union.addAll(offspringPopulation);

            List<BinarySolution> newPopulation = this.newGenerationSelection.execute(union);

            if (SolutionListUtils.solutionListsAreEquals(this.population, newPopulation)) {
                this.minimumDistance--;
            }

            if (this.minimumDistance <= -this.convergenceValue) {
                this.minimumDistance = (int) (1.0 / this.size * (1 - 1.0 / this.size) * this.size);

                int preserve = (int) Math.floor(this.preservedPopulation * this.population.size());
                newPopulation = new ArrayList<>(this.populationSize);
                this.population.sort(this.comparator);
                for (int i = 0; i < preserve; i++) {
                    newPopulation.add((BinarySolution) this.population.get(i).copy());
                }
                for (int i = preserve; i < this.populationSize; i++) {
                    BinarySolution solution = (BinarySolution) this.population.get(i).copy();
                    this.cataclysmicMutation.execute(solution);
                    this.problem.evaluate(solution);
                    //problem.evaluateConstraints(solution);
                    newPopulation.add(solution);
                    this.evaluations++;
                }
            }

            this.population = newPopulation;
        }
    }

    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.stoppingRules) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.population)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<BinarySolution> getResult() {
        NonDominatedSolutionListArchive<BinarySolution> archive = new NonDominatedSolutionListArchive<>();
        for (BinarySolution solution : this.population) {
            archive.add(solution);
        }

        return archive.getSolutionList();
    }

    @Override
    public List<StoppingRule> getStoppingRules() {
        return this.stoppingRules;
    }

    /**
     * Calculate the hamming distance between two solutions
     *
     * @param solutionOne A <code>Solution</code>
     * @param solutionTwo A <code>Solution</code>
     * @return the hamming distance between solutions
     */

    private int hammingDistance(BinarySolution solutionOne, BinarySolution solutionTwo) {
        int distance = 0;
        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            distance += this.hammingDistance(solutionOne.getVariableValue(i), solutionTwo.getVariableValue(i));
        }

        return distance;
    }

    private int hammingDistance(BinarySet bitSet1, BinarySet bitSet2) {
        if (bitSet1.getBinarySetLength() != bitSet2.getBinarySetLength()) {
            throw new JMetalException("The bitsets have different length: "
                    + bitSet1.getBinarySetLength() + ", " + bitSet2.getBinarySetLength());
        }
        int distance = 0;
        int i = 0;
        while (i < bitSet1.getBinarySetLength()) {
            if (bitSet1.get(i) != bitSet2.get(i)) {
                distance++;
            }
            i++;
        }

        return distance;
    }

}
