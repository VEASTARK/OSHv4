package org.uma.jmetal.algorithm.multiobjective.smsemoa;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SMSEMOA<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final int maxEvaluations;
    protected final double offset;
    protected final Comparator<S> dominanceComparator;
    private final Hypervolume<S> hypervolume;
    protected int evaluations;

    /**
     * Constructor
     */
    public SMSEMOA(Problem<S> problem, int maxEvaluations, int populationSize, double offset,
                   CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                   SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator, Hypervolume<S> hypervolumeImplementation) {
        super(problem);
        this.maxEvaluations = maxEvaluations;
        this.setMaxPopulationSize(populationSize);

        this.offset = offset;

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.dominanceComparator = dominanceComparator;
        this.hypervolume = hypervolumeImplementation;
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.getMaxPopulationSize();
    }

    @Override
    protected void updateProgress() {
        this.evaluations++;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return this.evaluations >= this.maxEvaluations;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        for (S solution : population) {
            this.getProblem().evaluate(solution);
        }
        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            S solution = this.selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offspringPopulation = new ArrayList<>(1);

        List<S> parents = new ArrayList<>(2);
        parents.add(population.get(0));
        parents.add(population.get(1));

        List<S> offspring = this.crossoverOperator.execute(parents);

        this.mutationOperator.execute(offspring.get(0));

        offspringPopulation.add(offspring.get(0));
        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);

        Ranking<S> ranking = this.computeRanking(jointPopulation);
        List<S> lastSubfront = ranking.getSubfront(ranking.getNumberOfSubfronts() - 1);

        lastSubfront = this.hypervolume.computeHypervolumeContribution(lastSubfront, jointPopulation);

        List<S> resultPopulation = new ArrayList<>();
        for (int i = 0; i < ranking.getNumberOfSubfronts() - 1; i++) {
            resultPopulation.addAll(ranking.getSubfront(i));
        }

        for (int i = 0; i < lastSubfront.size() - 1; i++) {
            resultPopulation.add(lastSubfront.get(i));
        }

        return resultPopulation;
    }

    @Override
    public List<S> getResult() {
        return this.getPopulation();
    }

    protected Ranking<S> computeRanking(List<S> solutionList) {
        Ranking<S> ranking = new DominanceRanking<>(this.dominanceComparator);
        ranking.computeRanking(solutionList);

        return ranking;
    }

    @Override
    public String getName() {
        return "SMSEMOA";
    }

    @Override
    public String getDescription() {
        return "S metric selection EMOA";
    }
}