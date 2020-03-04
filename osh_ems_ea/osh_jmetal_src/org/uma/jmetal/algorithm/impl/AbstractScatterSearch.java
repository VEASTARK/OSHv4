package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.algorithm.Algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing a scatter search algorithm
 *
 * @param <S> Solution
 * @param <R> Result
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractScatterSearch<S, R> implements Algorithm<R> {
    private List<S> population;
    private int populationSize;

    public List<S> getPopulation() {
        return this.population;
    }

    public void setPopulation(List<S> population) {
        this.population = population;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public abstract boolean isStoppingConditionReached();

    public abstract boolean restartConditionIsFulfilled(List<S> solutionList);

    public abstract void restart();

    public abstract S diversificationGeneration();

    public abstract S improvement(S solution);

    public abstract void referenceSetUpdate();

    public abstract void referenceSetUpdate(S solution);

    public abstract List<List<S>> subsetGeneration();

    public abstract List<S> solutionCombination(List<List<S>> population);

    @Override
    public abstract R getResult();

    @Override
    public void run() {
        this.initializationPhase();
        this.referenceSetUpdate();
        while (!this.isStoppingConditionReached()) {
            List<List<S>> subset = this.subsetGeneration();
            List<S> combinedSolutions = this.solutionCombination(subset);
            if (this.restartConditionIsFulfilled(combinedSolutions)) {
                this.restart();
                this.referenceSetUpdate();
            } else {
                for (S solution : combinedSolutions) {
                    S improvedSolution = this.improvement(solution);
                    this.referenceSetUpdate(improvedSolution);
                }
            }
        }
    }

    /**
     * Initialization phase of the scatter search: the population is filled with diverse solutions that
     * have been improved.
     *
     * @return The population
     */
    public void initializationPhase() {
        this.population = new ArrayList<>(this.populationSize);
        while (this.population.size() < this.populationSize) {
            S newSolution = this.diversificationGeneration();
            S improvedSolution = this.improvement(newSolution);
            this.population.add(improvedSolution);
        }
    }
}
