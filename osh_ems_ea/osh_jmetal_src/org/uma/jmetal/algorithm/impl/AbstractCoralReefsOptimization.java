package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract class representing a Coral Reefs Optimization Algorithm
 * <p>
 * Reference: S. Salcedo-Sanz, J. Del Ser, S. Gil-López, I. Landa-Torres and J.
 * A. Portilla-Figueras, "The coral reefs optimization algorithm: an efficient
 * meta-heuristic for solving hard optimization problems," 15th Applied
 * Stochastic Models and Data Analysis International Conference, Mataró, Spain,
 * June, 2013.
 *
 * @author Inacio Medeiros <inaciogmedeiros@gmail.com>
 */
@SuppressWarnings("serial")
public abstract class AbstractCoralReefsOptimization<S, R>
        implements Algorithm<R> {

    protected final SelectionOperator<List<S>, S> selectionOperator;
    protected final CrossoverOperator<S> crossoverOperator;
    protected final MutationOperator<S> mutationOperator;
    protected final Comparator<S> comparator;
    private final int N;
    private final int M; // Grid sizes
    private final double rho; // Percentage of occupied reef
    private final double Fbs;
    private final double Fbr; // Percentage of broadcast spawners and brooders
    private final double Fa;
    private final double Fd; // Percentage of budders and depredated corals
    private final double Pd; // Probability of depredation
    private final int attemptsToSettle;
    protected List<S> population;
    protected List<Coordinate> coordinates;

    private final List<StoppingRule> stoppingRules = new ArrayList<>();

    /**
     * Constructor
     *
     * @param comparator        Object for comparing two solutions
     * @param selectionOperator Selection Operator
     * @param crossoverOperator Crossover Operator
     * @param mutationOperator  Mutation Operator
     * @param n                 width of Coral Reef Grid
     * @param m                 height of Coral Reef Grid
     * @param rho               Percentage of occupied reef
     * @param fbs               Percentage of broadcast spawners
     * @param fa                Percentage of budders
     * @param pd                Probability of depredation
     * @param attemptsToSettle  number of attempts a larvae has to try to settle reef
     */
    public AbstractCoralReefsOptimization(Comparator<S> comparator,
                                          SelectionOperator<List<S>, S> selectionOperator,
                                          CrossoverOperator<S> crossoverOperator,
                                          MutationOperator<S> mutationOperator, int n, int m, double rho,
                                          double fbs, double fa, double pd, int attemptsToSettle) {
        this.comparator = comparator;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.N = n;
        this.M = m;
        this.rho = rho;
        this.Fbs = fbs;
        this.Fbr = 1 - fbs;
        this.Fa = fa;
        this.Fd = fa;
        this.Pd = pd;
        this.attemptsToSettle = attemptsToSettle;
    }

    public List<S> getPopulation() {
        return this.population;
    }

    public void setPopulation(List<S> population) {
        this.population = population;
    }

    public int getPopulationSize() {
        return this.population.size();
    }

    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public int getN() {
        return this.N;
    }

    public int getM() {
        return this.M;
    }

    public double getRho() {
        return this.rho;
    }

    public double getFbs() {
        return this.Fbs;
    }

    public double getFbr() {
        return this.Fbr;
    }

    public double getFa() {
        return this.Fa;
    }

    public double getFd() {
        return this.Fd;
    }

    public double getPd() {
        return this.Pd;
    }

    public int getAttemptsToSettle() {
        return this.attemptsToSettle;
    }

    protected abstract void initProgress();

    protected abstract void updateProgress(int evaluations);

    protected abstract boolean isStoppingConditionReached();

    protected abstract List<S> createInitialPopulation();

    protected abstract List<Coordinate> generateCoordinates();

    protected abstract List<S> evaluatePopulation(List<S> population);

    protected abstract List<S> selectBroadcastSpawners(List<S> population);

    protected abstract List<S> sexualReproduction(List<S> broadcastSpawners);

    protected abstract List<S> asexualReproduction(List<S> brooders);

    protected abstract List<S> larvaeSettlementPhase(List<S> larvae,
                                                     List<S> population, List<Coordinate> coordinates);

    protected abstract List<S> depredation(List<S> population,
                                           List<Coordinate> coordinates);

    @Override
    public void run() {
        List<S> broadcastSpawners;
        List<S> brooders;
        List<S> larvae;
        List<S> budders;
        int reproductions = 0;

        this.population = this.createInitialPopulation();
        this.population = this.evaluatePopulation(this.population);

        this.coordinates = this.generateCoordinates();

        this.initProgress();
        while (!this.isStoppingConditionReached()) {
            broadcastSpawners = this.selectBroadcastSpawners(this.population);

            brooders = new ArrayList<>((int) (this.Fbr * this.population.size()));

            for (S coral : this.population) {
                if (!broadcastSpawners.contains(coral)) {
                    brooders.add(coral);
                }
            }

            larvae = this.sexualReproduction(broadcastSpawners);
            larvae = this.evaluatePopulation(larvae);
            reproductions += larvae.size();

            this.population = this.larvaeSettlementPhase(larvae, this.population, this.coordinates);

            larvae = this.asexualReproduction(brooders);
            larvae = this.evaluatePopulation(larvae);
            reproductions += larvae.size();

            this.population = this.larvaeSettlementPhase(larvae, this.population, this.coordinates);

            this.population.sort(this.comparator);

            budders = new ArrayList<>(this.population.subList(0, (int) this.Fa * this.population.size()));

            this.population = this.larvaeSettlementPhase(budders, this.population, this.coordinates);

            this.population.sort(this.comparator);

            this.population = this.depredation(this.population, this.coordinates);

            this.updateProgress(reproductions);
            reproductions = 0;
        }

    }

    @Override
    public abstract R getResult();

    @Override
    public List<StoppingRule> getStoppingRules() {
        return this.stoppingRules;
    }

    /**
     * Represents a Coordinate in Coral Reef Grid
     *
     * @author inacio-medeiros
     */
    public static class Coordinate implements Comparable<Coordinate> {
        private int x, y;

        /**
         * Constructor
         *
         * @param x Coordinate's x-position
         * @param y Coordinate's y-position
         */
        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Retrieves Coordinate's x-position
         *
         * @return Coordinate's x-position
         */
        public int getX() {
            return this.x;
        }

        /**
         * Sets Coordinate's x-position to a new value
         *
         * @param x new value for Coordinate's x-position
         */
        public void setX(int x) {
            this.x = x;
        }

        /**
         * Retrieves Coordinate's y-position
         *
         * @return Coordinate's y-position
         */
        public int getY() {
            return this.y;
        }

        /**
         * Sets Coordinate's y-position to a new value
         *
         * @param y new value for Coordinate's y-position
         */
        public void setY(int y) {
            this.y = y;
        }

        @Override
        public int compareTo(Coordinate arg0) {
            int diffX = Math.abs(arg0.x - this.x);
            int diffY = Math.abs(arg0.y - this.y);
            double result = Math.sqrt((diffX * diffX) + (diffY * diffY));

            return Integer.parseInt(Double.toString(result));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;
            Coordinate other = (Coordinate) obj;

            if (this.x != other.x)
                return false;
            return this.y == other.y;
        }

    }

}
