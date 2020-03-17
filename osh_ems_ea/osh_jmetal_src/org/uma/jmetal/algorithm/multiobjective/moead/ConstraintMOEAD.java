package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.impl.ViolationThresholdComparator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.List;

/**
 * This class implements a constrained version of the MOEAD algorithm based on the one presented in
 * the paper: "An adaptive constraint handling approach embedded MOEA/D". DOI: 10.1109/CEC.2012.6252868
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ConstraintMOEAD extends AbstractMOEAD<DoubleSolution> {

    private final DifferentialEvolutionCrossover differentialEvolutionCrossover;
    private final ViolationThresholdComparator<DoubleSolution> violationThresholdComparator;

    public ConstraintMOEAD(Problem<DoubleSolution> problem,
                           int populationSize,
                           int resultPopulationSize,
                           MutationOperator<DoubleSolution> mutation,
                           CrossoverOperator<DoubleSolution> crossover,
                           FunctionType functionType,
                           String dataDirectory,
                           double neighborhoodSelectionProbability,
                           int maximumNumberOfReplacedSolutions,
                           int neighborSize,
                           IEALogger eaLogger) {
        super(problem, populationSize, resultPopulationSize, crossover, mutation, functionType,
                dataDirectory, neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions,
                neighborSize, eaLogger);

        this.differentialEvolutionCrossover = (DifferentialEvolutionCrossover) this.crossoverOperator;
        this.violationThresholdComparator = new ViolationThresholdComparator<>();
    }

    @Override
    public void run() {
        this.initializeUniformWeight();
        this.initializeNeighborhood();
        this.initializePopulation();
        this.idealPoint.update(this.population);

        this.violationThresholdComparator.updateThreshold(this.population);

        this.initProgress();

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

                this.idealPoint.update(child.getObjectives());
                this.updateNeighborhood(child, subProblemId, neighborType);
            }

            this.violationThresholdComparator.updateThreshold(this.population);
            this.updateProgress();

        } while (!this.isStoppingConditionReached());
    }

    public void initializePopulation() {
        for (int i = 0; i < this.populationSize; i++) {
            DoubleSolution newSolution = this.problem.createSolution();

            this.problem.evaluate(newSolution);
            this.population.add(newSolution);
        }
    }

    @Override
    protected void updateNeighborhood(DoubleSolution individual, int subproblemId, NeighborType neighborType) {
        int size;
        int time;

        time = 0;

        if (neighborType == NeighborType.NEIGHBOR) {
            size = this.neighborhood[subproblemId].length;
        } else {
            size = this.population.size();
        }
        int[] perm = new int[size];

        MOEADUtils.randomPermutation(perm, size);

        for (int i = 0; i < size; i++) {
            int k;
            if (neighborType == NeighborType.NEIGHBOR) {
                k = this.neighborhood[subproblemId][perm[i]];
            } else {
                k = perm[i];
            }
            double f1, f2;

            f1 = this.fitnessFunction(this.population.get(k), this.lambda[k]);
            f2 = this.fitnessFunction(individual, this.lambda[k]);

            if (this.violationThresholdComparator.needToCompare(this.population.get(k), individual)) {
                int flag = this.violationThresholdComparator.compare(this.population.get(k), individual);
                if (flag > 0) {
                    this.population.set(k, (DoubleSolution) individual.copy());
                } else if (flag == 0) {
                    if (f2 < f1) {
                        this.population.set(k, (DoubleSolution) individual.copy());
                        time++;
                    }
                }
            } else {
                if (f2 < f1) {
                    this.population.set(k, (DoubleSolution) individual.copy());
                    time++;
                }
            }

            if (time >= this.maximumNumberOfReplacedSolutions) {
                return;
            }
        }
    }

    @Override
    public String getName() {
        return "cMOEAD";
    }

    @Override
    public String getDescription() {
        return "Multi-Objective Evolutionary Algorithm based on Decomposition with constraints support";
    }
}
