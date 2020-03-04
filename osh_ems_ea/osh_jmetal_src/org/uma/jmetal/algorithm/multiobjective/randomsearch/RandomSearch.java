package org.uma.jmetal.algorithm.multiobjective.randomsearch;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;

import java.util.List;

/**
 * This class implements a simple random search algorithm.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class RandomSearch<S extends Solution<?>> implements Algorithm<List<S>> {
    final NonDominatedSolutionListArchive<S> nonDominatedArchive;
    private final Problem<S> problem;
    private final int maxEvaluations;

    /**
     * Constructor
     */
    public RandomSearch(Problem<S> problem, int maxEvaluations) {
        this.problem = problem;
        this.maxEvaluations = maxEvaluations;
        this.nonDominatedArchive = new NonDominatedSolutionListArchive<>();
    }

    /* Getter */
    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    @Override
    public void run() {
        S newSolution;
        for (int i = 0; i < this.maxEvaluations; i++) {
            newSolution = this.problem.createSolution();
            this.problem.evaluate(newSolution);
            this.nonDominatedArchive.add(newSolution);
        }
    }

    @Override
    public List<S> getResult() {
        return this.nonDominatedArchive.getSolutionList();
    }

    @Override
    public String getName() {
        return "RS";
    }

    @Override
    public String getDescription() {
        return "Multi-objective random search algorithm";
    }
} 
