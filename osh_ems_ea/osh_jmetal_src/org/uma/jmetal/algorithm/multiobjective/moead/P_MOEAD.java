package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class implementing a crude parallelized version of the the MOEA/D-DE algorithm.
 *
 * @author Antonio J. Nebro
 * @author Sebastian Kramer
 * @version 1.0
 */
@SuppressWarnings("serial")
public class P_MOEAD extends AbstractMOEAD<DoubleSolution> {
    protected DifferentialEvolutionCrossover differentialEvolutionCrossover;

    private SolutionListEvaluator<DoubleSolution> evaluator;

    public P_MOEAD(Problem<DoubleSolution> problem,
                   int populationSize,
                   int resultPopulationSize,
                   MutationOperator<DoubleSolution> mutation,
                   CrossoverOperator<DoubleSolution> crossover,
                   FunctionType functionType,
                   String dataDirectory,
                   double neighborhoodSelectionProbability,
                   int maximumNumberOfReplacedSolutions,
                   int neighborSize,
                   SolutionListEvaluator<DoubleSolution> evaluator) {
        super(problem, populationSize, resultPopulationSize, crossover, mutation, functionType,
                dataDirectory, neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions,
                neighborSize);

        this.differentialEvolutionCrossover = (DifferentialEvolutionCrossover) this.crossoverOperator;
        this.evaluator = evaluator;
    }

    @Override
    public void run() {
        this.initializePopulation();
        this.initializeUniformWeight();
        this.initializeNeighborhood();
        this.idealPoint.update(this.population);

        this.initProgress();
        do {
            int[] permutation = new int[this.populationSize];
            MOEADUtils.randomPermutation(permutation, this.populationSize);

            DoubleSolution[] childGeneration = new DoubleSolution[this.populationSize];
            NeighborType[] neighborTypes = new NeighborType[this.populationSize];

            for (int i = 0; i < this.populationSize; i++) {
                int subProblemId = permutation[i];

                NeighborType neighborType = this.chooseNeighborType();
                List<DoubleSolution> parents = this.parentSelection(subProblemId, neighborType);

                this.differentialEvolutionCrossover.setCurrentSolution(this.population.get(subProblemId));
                List<DoubleSolution> children = this.differentialEvolutionCrossover.execute(parents);

                DoubleSolution child = children.get(0);
                this.mutationOperator.execute(child);

                childGeneration[subProblemId] = child;
                neighborTypes[subProblemId] = neighborType;
            }

            this.evaluator.evaluate(Arrays.asList(childGeneration), this.problem);
            for (int i = 0; i < childGeneration.length; i++) {
                this.idealPoint.update(childGeneration[i].getObjectives());
                this.updateNeighborhood(childGeneration[i], i, neighborTypes[i]);
            }

            this.updateProgress();

        } while (!this.isStoppingConditionReached());

    }

    protected void evaluatePopulation(DoubleSolution[] population, NeighborType[] neighborTypes) {
        this.evaluator.evaluate(Arrays.asList(population), this.problem);

        for (int i = 0; i < population.length; i++) {
            this.idealPoint.update(population[i].getObjectives());
            this.updateNeighborhood(population[i], i, neighborTypes[i]);
        }
    }

    protected void initializePopulation() {
        this.population = new ArrayList<>(this.populationSize);
        for (int i = 0; i < this.populationSize; i++) {
            this.population.add(this.problem.createSolution());
        }
        this.evaluator.evaluate(this.population, this.problem);
    }

    @Override
    public String getName() {
        return "P_MOEAD";
    }

    @Override
    public String getDescription() {
        return "parallelized Multi-Objective Evolutionary Algorithm based on Decomposition";
    }
}
