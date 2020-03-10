package org.uma.jmetal.algorithm.singleobjective.coralreefsoptimization;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;

import java.util.Comparator;
import java.util.List;

/**
 * @author Inacio Medeiros <inaciogmedeiros@gmail.com>
 */
public class CoralReefsOptimizationBuilder<S extends Solution<?>> implements
        AlgorithmBuilder<CoralReefsOptimization<S>> {

    /**
     * CoralReefsOptimizationBuilder class
     */
    private final Problem<S> problem;

    private final SelectionOperator<List<S>, S> selectionOperator;
    private final CrossoverOperator<S> crossoverOperator;
    private final MutationOperator<S> mutationOperator;
    private Comparator<S> comparator;

    private int maxEvaluations;
    private int N, M; // Grid sizes
    private double rho; // Percentage of occupied reef
    private double Fbs, Fbr; // Percentage of broadcast spawners and brooders
    private double Fa, Fd; // Percentage of budders and depredated corals
    private double Pd; // Probability of depredation
    private int attemptsToSettle;

    /**
     * CoralReefsOptimizationBuilder constructor
     */
    public CoralReefsOptimizationBuilder(Problem<S> problem,
                                         SelectionOperator<List<S>, S> selectionOperator,
                                         CrossoverOperator<S> crossoverOperator,
                                         MutationOperator<S> mutationOperator) {
        this.problem = problem;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
    }

    @Override
    public CoralReefsOptimization<S> build() {
        CoralReefsOptimization<S> algorithm;

        algorithm = new CoralReefsOptimization<>(this.problem,
                this.comparator, this.selectionOperator, this.crossoverOperator,
                this.mutationOperator, this.N, this.M, this.rho, this.Fbs, this.Fa, this.Pd, this.attemptsToSettle);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.M * this.N, this.maxEvaluations));

        return algorithm;
    }

    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public CoralReefsOptimizationBuilder<S> setMaxEvaluations(int maxEvaluations) {
        if (maxEvaluations < 0) {
            throw new JMetalException("maxEvaluations is negative: "
                    + maxEvaluations);
        }
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return this.selectionOperator;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    public Comparator<S> getComparator() {
        return this.comparator;
    }

    public CoralReefsOptimizationBuilder<S> setComparator(
            Comparator<S> comparator) {
        if (comparator == null) {
            throw new JMetalException("Comparator is null!");
        }

        this.comparator = comparator;

        return this;
    }

    public int getN() {
        return this.N;
    }

    public CoralReefsOptimizationBuilder<S> setN(int n) {
        if (n < 0) {
            throw new JMetalException("N is negative: " + n);
        }

        this.N = n;
        return this;
    }

    public int getM() {
        return this.M;
    }

    public CoralReefsOptimizationBuilder<S> setM(int m) {
        if (m < 0) {
            throw new JMetalException("M is negative: " + m);
        }

        this.M = m;
        return this;
    }

    public double getRho() {
        return this.rho;
    }

    public CoralReefsOptimizationBuilder<S> setRho(double rho) {
        if (rho < 0) {
            throw new JMetalException("Rho is negative: " + rho);
        }

        this.rho = rho;
        return this;
    }

    public double getFbs() {
        return this.Fbs;
    }

    public CoralReefsOptimizationBuilder<S> setFbs(double fbs) {
        if (fbs < 0) {
            throw new JMetalException("Fbs is negative: " + fbs);
        }

        this.Fbs = fbs;
        return this;
    }

    public double getFbr() {
        return this.Fbr;
    }

    public CoralReefsOptimizationBuilder<S> setFbr(double fbr) {
        if (fbr < 0) {
            throw new JMetalException("Fbr is negative: " + fbr);
        }

        this.Fbr = fbr;
        return this;
    }

    public double getFa() {
        return this.Fa;
    }

    public CoralReefsOptimizationBuilder<S> setFa(double fa) {
        if (fa < 0) {
            throw new JMetalException("Fa is negative: " + fa);
        }

        this.Fa = fa;
        return this;
    }

    public double getFd() {
        return this.Fd;
    }

    public CoralReefsOptimizationBuilder<S> setFd(double fd) {
        if (fd < 0) {
            throw new JMetalException("Fd is negative: " + fd);
        }

        this.Fd = fd;
        return this;
    }

    public double getPd() {
        return this.Pd;
    }

    public CoralReefsOptimizationBuilder<S> setPd(double pd) {
        if (pd < 0) {
            throw new JMetalException("Pd is negative: " + pd);
        }

        this.Pd = pd;
        return this;
    }

    public int getAttemptsToSettle() {
        return this.attemptsToSettle;
    }

    public CoralReefsOptimizationBuilder<S> setAttemptsToSettle(
            int attemptsToSettle) {
        if (attemptsToSettle < 0) {
            throw new JMetalException("attemptsToSettle is negative: "
                    + attemptsToSettle);
        }

        this.attemptsToSettle = attemptsToSettle;
        return this;
    }

}
