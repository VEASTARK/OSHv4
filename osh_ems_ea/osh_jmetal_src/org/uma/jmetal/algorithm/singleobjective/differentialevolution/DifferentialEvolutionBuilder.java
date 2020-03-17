package org.uma.jmetal.algorithm.singleobjective.differentialevolution;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

/**
 * DifferentialEvolutionBuilder class
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class DifferentialEvolutionBuilder {
    private final DoubleProblem problem;
    private final IEALogger eaLogger;
    private int populationSize;
    private int maxEvaluations;
    private DifferentialEvolutionCrossover crossoverOperator;
    private DifferentialEvolutionSelection selectionOperator;
    private SolutionListEvaluator<DoubleSolution> evaluator;

    public DifferentialEvolutionBuilder(DoubleProblem problem, IEALogger eaLogger) {
        this.problem = problem;
        this.eaLogger = eaLogger;
        this.populationSize = 100;
        this.maxEvaluations = 25000;
        this.crossoverOperator = new DifferentialEvolutionCrossover(0.5, 0.5, "rand/1/bin");
        this.selectionOperator = new DifferentialEvolutionSelection();
        this.evaluator = new SequentialSolutionListEvaluator<>();
    }

    public DifferentialEvolutionBuilder setCrossover(DifferentialEvolutionCrossover crossover) {
        this.crossoverOperator = crossover;

        return this;
    }

    public DifferentialEvolutionBuilder setSelection(DifferentialEvolutionSelection selection) {
        this.selectionOperator = selection;

        return this;
    }

    public DifferentialEvolution build() {
        DifferentialEvolution de = new DifferentialEvolution(this.problem, this.populationSize, this.crossoverOperator,
                this.selectionOperator, this.evaluator, this.eaLogger);

        de.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));

        return de;
    }

    /* Getters */
    public DoubleProblem getProblem() {
        return this.problem;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public DifferentialEvolutionBuilder setPopulationSize(int populationSize) {
        if (populationSize < 0) {
            throw new JMetalException("Population size is negative: " + populationSize);
        }

        this.populationSize = populationSize;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public DifferentialEvolutionBuilder setMaxEvaluations(int maxEvaluations) {
        if (maxEvaluations < 0) {
            throw new JMetalException("MaxEvaluations is negative: " + maxEvaluations);
        }

        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public DifferentialEvolutionCrossover getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public DifferentialEvolutionSelection getSelectionOperator() {
        return this.selectionOperator;
    }

    public SolutionListEvaluator<DoubleSolution> getSolutionListEvaluator() {
        return this.evaluator;
    }

    public DifferentialEvolutionBuilder setSolutionListEvaluator(SolutionListEvaluator<DoubleSolution> evaluator) {
        this.evaluator = evaluator;

        return this;
    }
}

