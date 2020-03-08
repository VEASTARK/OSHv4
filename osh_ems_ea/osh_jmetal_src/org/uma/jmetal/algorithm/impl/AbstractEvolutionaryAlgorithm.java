package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.problem.Problem;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing an evolutionary algorithm
 *
 * @param <S> Solution
 * @param <R> Result
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractEvolutionaryAlgorithm<S, R> implements Algorithm<R> {
    protected List<S> population;
    protected Problem<S> problem;

    private final List<StoppingRule> stoppingRules = new ArrayList<>();

    public List<S> getPopulation() {
        return this.population;
    }

    public void setPopulation(List<S> population) {
        this.population = population;
    }

    public Problem<S> getProblem() {
        return this.problem;
    }

    public void setProblem(Problem<S> problem) {
        this.problem = problem;
    }

    protected abstract void initProgress();

    protected abstract void updateProgress();

    protected abstract boolean isStoppingConditionReached();

    protected abstract List<S> createInitialPopulation();

    protected abstract List<S> evaluatePopulation(List<S> population);

    protected abstract List<S> selection(List<S> population);

    protected abstract List<S> reproduction(List<S> population);

    protected abstract List<S> replacement(List<S> population, List<S> offspringPopulation);

    @Override
    public abstract R getResult();

    @Override
    public List<StoppingRule> getStoppingRules() {
        return this.stoppingRules;
    }

    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;

        this.population = this.createInitialPopulation();
        this.population = this.evaluatePopulation(this.population);
        this.initProgress();
        while (!this.isStoppingConditionReached()) {
            matingPopulation = this.selection(this.population);
            offspringPopulation = this.reproduction(matingPopulation);
            offspringPopulation = this.evaluatePopulation(offspringPopulation);
            this.population = this.replacement(this.population, offspringPopulation);
            this.updateProgress();
        }
    }
}
