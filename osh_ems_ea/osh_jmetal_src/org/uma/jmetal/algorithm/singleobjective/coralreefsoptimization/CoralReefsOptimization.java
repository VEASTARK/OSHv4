package org.uma.jmetal.algorithm.singleobjective.coralreefsoptimization;

import org.uma.jmetal.algorithm.impl.AbstractCoralReefsOptimization;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Inacio Medeiros <inaciogmedeiros@gmail.com>
 */
public class CoralReefsOptimization<S extends Solution<?>>
        extends AbstractCoralReefsOptimization<S, List<S>> {

    private static final long serialVersionUID = 3013223456538143239L;
    private final Problem<S> problem;
    private final JMetalRandom random;
    private int evaluations;

    public CoralReefsOptimization(Problem<S> problem, Comparator<S> comparator,
                                  SelectionOperator<List<S>, S> selectionOperator,
                                  CrossoverOperator<S> crossoverOperator,
                                  MutationOperator<S> mutationOperator, int n, int m, double rho,
                                  double fbs, double fa, double pd, int attemptsToSettle, IEALogger eaLogger) {

        super(comparator, selectionOperator, crossoverOperator,
                mutationOperator, n, m, rho, fbs, fa, pd, attemptsToSettle, eaLogger);

        this.problem = problem;
        this.random = JMetalRandom.getInstance();
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.population.size();
        this.getEALogger().logPopulation(this.population, 1);
    }

    @Override
    protected void updateProgress(int reproductions) {
        this.evaluations += reproductions;
        this.getEALogger().logPopulation(this.population, this.evaluations / this.getPopulationSize());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.population)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<S> createInitialPopulation() {
        List<S> population = new ArrayList<>(this.getN() * this.getM());

        int quantity = (int) (this.getRho() * this.getN() * this.getM());

        for (int i = 0; i < quantity; i++) {
            S newIndividual = this.problem.createSolution();
            population.add(newIndividual);
        }
        return population;
    }

    @Override
    protected List<Coordinate> generateCoordinates() {
        int popSize = this.getPopulationSize();
        ArrayList<Coordinate> coordinates = new ArrayList<>(popSize);

        for (int i = 0; i < popSize; i++) {
            coordinates.add(new Coordinate(this.random.nextInt(0, this.getN() - 1),
                    this.random.nextInt(0, this.getM() - 1)));
        }

        return coordinates;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        for (S s : population) {
            this.problem.evaluate(s);
        }
        return population;
    }

    @Override
    protected List<S> selectBroadcastSpawners(List<S> population) {
        int quantity = (int) (this.getFbs() * population.size());

        if ((quantity % 2) == 1) {
            quantity--;
        }

        List<S> spawners = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; i++) {
            S solution = this.selectionOperator.execute(population);
            spawners.add(solution);
        }

        return spawners;
    }

    @Override
    protected List<S> sexualReproduction(List<S> broadcastSpawners) {
        List<S> parents = new ArrayList<>(2);
        List<S> larvae = new ArrayList<>(broadcastSpawners.size() / 2);

        while (!broadcastSpawners.isEmpty()) {
            parents.add(this.selectionOperator.execute(broadcastSpawners));
            parents.add(this.selectionOperator.execute(broadcastSpawners));

            broadcastSpawners.remove(parents.get(0));
            broadcastSpawners.remove(parents.get(1));

            larvae.add(this.crossoverOperator.execute(parents).get(0));

            parents.clear();

        }

        return larvae;
    }

    @Override
    protected List<S> asexualReproduction(List<S> brooders) {
        int sz = brooders.size();

        List<S> larvae = new ArrayList<>(sz);

        for (S brooder : brooders) {
            larvae.add(this.mutationOperator.execute(brooder));
        }

        return larvae;
    }

    @Override
    protected List<S> larvaeSettlementPhase(List<S> larvae, List<S> population,
                                            List<Coordinate> coordinates) {

        int attempts = this.getAttemptsToSettle();
        int index;

        for (S larva : larvae) {

            for (int attempt = 0; attempt < attempts; attempt++) {
                Coordinate C = new Coordinate(this.random.nextInt(0, this.getN() - 1),
                        this.random.nextInt(0, this.getM() - 1));

                if (!coordinates.contains(C)) {
                    population.add(larva);
                    coordinates.add(C);
                    break;
                }

                index = coordinates.indexOf(C);

                if (this.comparator.compare(larva, population.get(index)) < 0) {
                    population.add(index, larva);
                    population.remove(index + 1);
                    break;
                }

            }

        }

        return population;
    }

    @Override
    protected List<S> depredation(List<S> population,
                                  List<Coordinate> coordinates) {
        int popSize = population.size();
        int quantity = (int) (this.getFd() * popSize);

        quantity = popSize - quantity;

        double coin;
        for (int i = popSize - 1; i > quantity; i--) {
            coin = this.random.nextDouble();

            if (coin < this.getPd()) {
                population.remove(population.size() - 1);
                coordinates.remove(population.size() - 1);
            }

        }

        return population;
    }

    @Override
    public List<S> getResult() {
        this.getPopulation().sort(this.comparator);
        return this.getPopulation();
    }

    @Override
    public String getName() {
        return "CRO";
    }

    @Override
    public String getDescription() {
        return "Coral Reefs Optimization";
    }

}
