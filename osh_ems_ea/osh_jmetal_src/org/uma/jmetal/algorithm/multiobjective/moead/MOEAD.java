package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the MOEA/D-DE algorithm described in :
 * Hui Li; Qingfu Zhang, "Multiobjective Optimization Problems With Complicated Pareto Sets,
 * MOEA/D and NSGA-II," Evolutionary Computation, IEEE Transactions on , vol.13, no.2, pp.284,302,
 * April 2009. doi: 10.1109/TEVC.2008.925798
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MOEAD extends AbstractMOEAD<DoubleSolution> {
    protected final DifferentialEvolutionCrossover differentialEvolutionCrossover;

    public MOEAD(Problem<DoubleSolution> problem,
                 int populationSize,
                 int resultPopulationSize,
                 int maxEvaluations,
                 MutationOperator<DoubleSolution> mutation,
                 CrossoverOperator<DoubleSolution> crossover,
                 FunctionType functionType,
                 String dataDirectory,
                 double neighborhoodSelectionProbability,
                 int maximumNumberOfReplacedSolutions,
                 int neighborSize) {
        super(problem, populationSize, resultPopulationSize, maxEvaluations, crossover, mutation, functionType,
                dataDirectory, neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions,
                neighborSize);

        this.differentialEvolutionCrossover = (DifferentialEvolutionCrossover) this.crossoverOperator;
    }

    @Override
    public void run() {
        this.initializePopulation();
        this.initializeUniformWeight();
        this.initializeNeighborhood();
        this.idealPoint.update(this.population);

        this.evaluations = this.populationSize;
        do {
            int[] permutation = new int[this.populationSize];
            MOEADUtils.randomPermutation(permutation, this.populationSize);

            for (int i = 0; i < this.populationSize; i++) {
                int subProblemId = permutation[i];

                NeighborType neighborType = this.chooseNeighborType();
                List<DoubleSolution> parents = this.parentSelection(subProblemId, neighborType);

                this.differentialEvolutionCrossover.setCurrentSolution(this.population.get(subProblemId));
                List<DoubleSolution> children = this.differentialEvolutionCrossover.execute(parents);

                DoubleSolution child = children.get(0);
                this.mutationOperator.execute(child);
                this.problem.evaluate(child);

                this.evaluations++;

                this.idealPoint.update(child.getObjectives());
                this.updateNeighborhood(child, subProblemId, neighborType);
            }
        } while (this.evaluations < this.maxEvaluations);

    }

    protected void initializePopulation() {
        this.population = new ArrayList<>(this.populationSize);
        for (int i = 0; i < this.populationSize; i++) {
            DoubleSolution newSolution = this.problem.createSolution();

            this.problem.evaluate(newSolution);
            this.population.add(newSolution);
        }
    }

    @Override
    public String getName() {
        return "MOEAD";
    }

    @Override
    public String getDescription() {
        return "Multi-Objective Evolutionary Algorithm based on Decomposition";
    }
}
