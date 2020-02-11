package osh.mgmt.globalcontroller.jmetal;

import jmetal.core.Solution;

/**
 * @author Ingo Mauser
 */
public class SolutionWithFitness {

    final Solution solution;
    final double fitness;

    public SolutionWithFitness(Solution solution, double fitness) {
        this.solution = solution;
        this.fitness = fitness;
    }

    public Solution getSolution() {
        return this.solution;
    }

    public double getFitness() {
        return this.fitness;
    }
}
