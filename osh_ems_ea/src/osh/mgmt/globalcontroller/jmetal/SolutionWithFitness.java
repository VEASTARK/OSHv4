package osh.mgmt.globalcontroller.jmetal;


import org.uma.jmetal.solution.Solution;

/**
 * @author Ingo Mauser
 */
public class SolutionWithFitness {

    final Solution<?> solution;
    final double[] fitness;

    public SolutionWithFitness(Solution<?> solution, double[] fitness) {
        this.solution = solution;
        this.fitness = fitness;
    }

    public Solution<?> getSolution() {
        return this.solution;
    }

    public double[] getFitness() {
        return this.fitness;
    }
}
