package org.uma.jmetal.algorithm.multiobjective.mochc;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
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
 */
@SuppressWarnings("serial")
public class MOCHC extends AbstractEvolutionaryAlgorithm<BinarySolution, List<BinarySolution>> {
    private final BinaryProblem problem;

    private final int convergenceValue;
    private final double preservedPopulation;
    private final double initialConvergenceCount;
    private final CrossoverOperator<BinarySolution> crossover;
    private final MutationOperator<BinarySolution> cataclysmicMutation;
    private final SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection;
    private final SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection;
    private final Comparator<BinarySolution> comparator;
    private final SolutionListEvaluator<BinarySolution> evaluator;
    private int maxPopulationSize;
    private int evaluations;
    private int minimumDistance;
    private int size;
    private int lastOffspringPopulationSize;

    /**
     * Constructor
     */
    public MOCHC(BinaryProblem problem, int populationSize, int convergenceValue,
                 double preservedPopulation, double initialConvergenceCount,
                 CrossoverOperator<BinarySolution> crossoverOperator,
                 MutationOperator<BinarySolution> cataclysmicMutation,
                 SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection, SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection,
                 SolutionListEvaluator<BinarySolution> evaluator) {
        super();
        this.problem = problem;
        this.maxPopulationSize = populationSize;
        this.convergenceValue = convergenceValue;
        this.preservedPopulation = preservedPopulation;
        this.initialConvergenceCount = initialConvergenceCount;
        this.crossover = crossoverOperator;
        this.cataclysmicMutation = cataclysmicMutation;
        this.newGenerationSelection = newGenerationSelection;
        this.parentSelection = parentSelection;
        this.evaluator = evaluator;

        for (int i = 0; i < problem.getNumberOfVariables(); i++) {
            this.size += problem.getNumberOfBits(i);
        }
        this.minimumDistance = (int) Math.floor(this.initialConvergenceCount * this.size);

        this.comparator = new CrowdingDistanceComparator<>();
    }

    public int getMaxPopulationSize() {
        return this.maxPopulationSize;
    }

    public void setMaxPopulationSize(int maxPopulationSize) {
        this.maxPopulationSize = maxPopulationSize;
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.maxPopulationSize;
    }

    @Override
    protected void updateProgress() {
        this.evaluations += this.lastOffspringPopulationSize;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.population)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<BinarySolution> createInitialPopulation() {
        List<BinarySolution> population = new ArrayList<>(this.maxPopulationSize);
        for (int i = 0; i < this.maxPopulationSize; i++) {
            BinarySolution newIndividual = this.problem.createSolution();
            population.add(newIndividual);
        }
        return population;
    }

    @Override
    protected List<BinarySolution> evaluatePopulation(List<BinarySolution> population) {
        return this.evaluator.evaluate(population, this.problem);
    }

    @Override
    protected List<BinarySolution> selection(List<BinarySolution> population) {
        List<BinarySolution> matingPopulation = new ArrayList<>(population.size());
        for (int i = 0; i < population.size(); i++) {
            BinarySolution solution = this.parentSelection.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    @Override
    protected List<BinarySolution> reproduction(List<BinarySolution> matingPopulation) {
        List<BinarySolution> offspringPopulation = new ArrayList<>();

        for (int i = 0; i < matingPopulation.size(); i += 2) {
            List<BinarySolution> parents = new ArrayList<>(2);
            parents.add(matingPopulation.get(i));
            parents.add(matingPopulation.get(i + 1));

            if (this.hammingDistance(parents.get(0), parents.get(1)) >= this.minimumDistance) {
                List<BinarySolution> offspring = this.crossover.execute(parents);
                offspringPopulation.add(offspring.get(0));
                offspringPopulation.add(offspring.get(1));
            }
        }

        this.lastOffspringPopulationSize = offspringPopulation.size();
        return offspringPopulation;
    }

    @Override
    protected List<BinarySolution> replacement(List<BinarySolution> population,
                                               List<BinarySolution> offspringPopulation) {
        List<BinarySolution> union = new ArrayList<>();
        union.addAll(population);
        union.addAll(offspringPopulation);

        List<BinarySolution> newPopulation = this.newGenerationSelection.execute(union);

        if (SolutionListUtils.solutionListsAreEquals(population, newPopulation)) {
            this.minimumDistance--;
        }

        if (this.minimumDistance <= -this.convergenceValue) {
            // minimumDistance = (int) (1.0 / size * (1 - 1.0 / size) * size);
            this.minimumDistance = (int) (0.35 * (1 - 0.35) * this.size);

            int preserve = (int) Math.floor(this.preservedPopulation * population.size());
            newPopulation = new ArrayList<>(this.maxPopulationSize);
            population.sort(this.comparator);
            for (int i = 0; i < preserve; i++) {
                newPopulation.add((BinarySolution) population.get(i).copy());
            }
            for (int i = preserve; i < this.maxPopulationSize; i++) {
                BinarySolution solution = (BinarySolution) population.get(i).copy();
                this.cataclysmicMutation.execute(solution);

                newPopulation.add(solution);
            }
        }

        return newPopulation;
    }

    @Override
    public List<BinarySolution> getResult() {
        NonDominatedSolutionListArchive<BinarySolution> archive = new NonDominatedSolutionListArchive<>();
        for (BinarySolution solution : this.getPopulation()) {
            archive.add(solution);
        }

        return archive.getSolutionList();
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

    @Override
    public String getName() {
        return "MOCHC";
    }

    @Override
    public String getDescription() {
        return "Multiobjective CHC algorithm";
    }
}
