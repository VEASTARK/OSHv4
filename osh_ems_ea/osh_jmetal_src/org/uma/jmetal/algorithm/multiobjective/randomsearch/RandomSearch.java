package org.uma.jmetal.algorithm.multiobjective.randomsearch;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
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

    private final List<StoppingRule> stoppingRules = new ArrayList<>();
    private IEALogger eaLogger;

    /**
     * Constructor
     */
    public RandomSearch(Problem<S> problem, int maxEvaluations, IEALogger eaLogger) {
        this.problem = problem;
        this.maxEvaluations = maxEvaluations;
        this.nonDominatedArchive = new NonDominatedSolutionListArchive<>();

        this.eaLogger = eaLogger;
        this.eaLogger.logStart(this);
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
        this.eaLogger.logPopulation(this.nonDominatedArchive.getSolutionList(), 1);
    }

    @Override
    public List<S> getResult() {
        return this.nonDominatedArchive.getSolutionList();
    }

    @Override
    public List<StoppingRule> getStoppingRules() {
        return this.stoppingRules;
    }

    @Override
    public void setEALogger(IEALogger eaLogger) {
        this.eaLogger = eaLogger;
    }

    @Override
    public IEALogger getEALogger() {
        return this.eaLogger;
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
