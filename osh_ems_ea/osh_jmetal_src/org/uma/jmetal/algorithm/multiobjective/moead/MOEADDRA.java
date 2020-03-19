package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the MOEA/D-DRA algorithm described in :
 * Q. Zhang,  W. Liu,  and H Li, The Performance of a New Version of
 * MOEA/D on CEC09 Unconstrained MOP Test Instances, Working Report CES-491,
 * School of CS & EE, University of Essex, 02/2009
 *
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MOEADDRA extends AbstractMOEAD<DoubleSolution> {
    protected final DifferentialEvolutionCrossover differentialEvolutionCrossover;

    protected final DoubleSolution[] savedValues;
    protected final double[] utility;
    protected final int[] frequency;

    final JMetalRandom randomGenerator;

    public MOEADDRA(Problem<DoubleSolution> problem, int populationSize, int resultPopulationSize,
                    MutationOperator<DoubleSolution> mutation, CrossoverOperator<DoubleSolution> crossover, FunctionType functionType,
                    String dataDirectory, double neighborhoodSelectionProbability,
                    int maximumNumberOfReplacedSolutions, int neighborSize, IEALogger eaLogger) {
        super(problem, populationSize, resultPopulationSize, crossover, mutation, functionType,
                dataDirectory, neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions,
                neighborSize, eaLogger);

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

        int generation = 0;
        this.initProgress();
        do {
            int[] permutation = new int[this.populationSize];
            MOEADUtils.randomPermutation(permutation, this.populationSize);

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

                this.idealPoint.update(child.getObjectives());
                this.updateNeighborhood(child, subProblemId, neighborType);
            }

            generation++;
            if (generation % 30 == 0) {
                this.utilityFunction();
            }
            this.updateProgress();

        } while (!this.isStoppingConditionReached());

    }

    protected void initializePopulation() {
        for (int i = 0; i < this.populationSize; i++) {
            DoubleSolution newSolution = this.problem.createSolution();

            this.problem.evaluate(newSolution);
            this.population.add(newSolution);
            this.savedValues[i] = (DoubleSolution) newSolution.copy();
        }
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
                this.utility[n] = Math.min(uti, 1.0);
            }
            this.savedValues[n] = (DoubleSolution) this.population.get(n).copy();
        }
    }

    public List<Integer> tourSelection(int depth) {
        List<Integer> selected = new ArrayList<>();
        List<Integer> candidate = new ArrayList<>();

        for (int k = 0; k < this.problem.getNumberOfObjectives(); k++) {
            // WARNING! HERE YOU HAVE TO USE THE WEIGHT PROVIDED BY QINGFU Et AL (NOT SORTED!!!!)
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

    @Override
    public String getName() {
        return "MOEADDRA";
    }

    @Override
    public String getDescription() {
        return "Multi-Objective Evolutionary Algorithm based on Decomposition. Version with Dynamic Resource Allocation";
    }
}
