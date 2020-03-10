package org.uma.jmetal.algorithm.singleobjective.evolutionstrategy;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionStrategy;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class implementing a (mu + lambda) Evolution Strategy (lambda must be divisible by mu)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NonElitistEvolutionStrategy<S extends Solution<?>> extends AbstractEvolutionStrategy<S, S> {
    private final int mu;
    private final int lambda;
    private final MutationOperator<S> mutation;
    private final Comparator<S> comparator;
    private int evaluations;

    /**
     * Constructor
     */
    public NonElitistEvolutionStrategy(Problem<S> problem, int mu, int lambda, MutationOperator<S> mutation) {
        super(problem);
        this.mu = mu;
        this.lambda = lambda;
        this.mutation = mutation;

        this.comparator = new ObjectiveComparator<>(0);
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.mu;
    }

    @Override
    protected void updateProgress() {
        this.evaluations += this.lambda;
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
        List<S> population = new ArrayList<>(this.mu);
        for (int i = 0; i < this.mu; i++) {
            S newIndividual = this.getProblem().createSolution();
            population.add(newIndividual);
        }

        return population;
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
        return population;
        //    List<Solution> matingPopulation = new ArrayList<>(mu) ;
        //    for (Solution solution: population) {
        //      matingPopulation.add(solution.copy()) ;
        //    }
        //    return matingPopulation ;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offspringPopulation = new ArrayList<>(this.lambda);
        for (int i = 0; i < this.mu; i++) {
            for (int j = 0; j < this.lambda / this.mu; j++) {
                S offspring = (S) population.get(i).copy();
                this.mutation.execute(offspring);
                offspringPopulation.add(offspring);
            }
        }

        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population,
                                  List<S> offspringPopulation) {
        offspringPopulation.sort(this.comparator);

        List<S> newPopulation = new ArrayList<>(this.mu);
        for (int i = 0; i < this.mu; i++) {
            newPopulation.add(offspringPopulation.get(i));
        }
        return newPopulation;
    }

    @Override
    public S getResult() {
        return this.getPopulation().get(0);
    }

    @Override
    public String getName() {
        return "NonElitistEA";
    }

    @Override
    public String getDescription() {
        return "Non Elitist Evolution Strategy Algorithm, i.e, (mu , lambda) EA";
    }
}
