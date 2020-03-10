package org.uma.jmetal.algorithm.multiobjective.mombi;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing variants of the MOMBI algorithm
 *
 * @param <S>
 * @author Juan J. Durillo
 * Modified by Antonio J. Nebro
 */
@SuppressWarnings("serial")
public abstract class AbstractMOMBI<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final SolutionListEvaluator<S> evaluator;
    protected final List<Double> referencePoint;
    protected final List<Double> nadirPoint;
    protected int iterations;

    /**
     * Constructor
     *
     * @param problem       Problem to be solved
     * @param crossover     Crossover operator
     * @param mutation      Mutation operator
     * @param selection     Selection operator
     * @param evaluator     Evaluator object for evaluating solution lists
     */
    public AbstractMOMBI(Problem<S> problem, CrossoverOperator<S> crossover, MutationOperator<S> mutation,
                         SelectionOperator<List<S>, S> selection,
                         SolutionListEvaluator<S> evaluator) {
        super(problem);

        this.crossoverOperator = crossover;
        this.mutationOperator = mutation;
        this.selectionOperator = selection;

        this.evaluator = evaluator;

        this.nadirPoint = new ArrayList<>(this.getProblem().getNumberOfObjectives());
        this.initializeNadirPoint(this.getProblem().getNumberOfObjectives());
        this.referencePoint = new ArrayList<>(this.getProblem().getNumberOfObjectives());
        this.initializeReferencePoint(this.getProblem().getNumberOfObjectives());
    }

    @Override
    protected void initProgress() {
        this.iterations = 1;
    }

    @Override
    protected void updateProgress() {
        this.iterations += 1;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, this.iterations, -1, this.getPopulation())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        return this.evaluator.evaluate(population, this.getProblem());
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(population.size());
        for (int i = 0; i < this.getMaxPopulationSize(); i++) {
            S solution = this.selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offspringPopulation = new ArrayList<>(this.getMaxPopulationSize());
        for (int i = 0; i < this.getMaxPopulationSize(); i += 2) {
            List<S> parents = new ArrayList<>(2);
            int parent1Index = JMetalRandom.getInstance().nextInt(0, this.getMaxPopulationSize() - 1);
            int parent2Index = JMetalRandom.getInstance().nextInt(0, this.getMaxPopulationSize() - 1);
            while (parent1Index == parent2Index)
                parent2Index = JMetalRandom.getInstance().nextInt(0, this.getMaxPopulationSize() - 1);
            parents.add(population.get(parent1Index));
            parents.add(population.get(parent2Index));

            List<S> offspring = this.crossoverOperator.execute(parents);

            this.mutationOperator.execute(offspring.get(0));
            this.mutationOperator.execute(offspring.get(1));

            offspringPopulation.add(offspring.get(0));
            offspringPopulation.add(offspring.get(1));
        }
        return offspringPopulation;
    }

    @Override
    public List<S> getResult() {
        this.setPopulation(this.evaluator.evaluate(this.getPopulation(), this.getProblem()));

        return this.getPopulation();
    }

    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;

        this.setPopulation(this.createInitialPopulation());
        this.evaluatePopulation(this.getPopulation());
        this.initProgress();
        //specific GA needed computations
        this.specificMOEAComputations();
        while (!this.isStoppingConditionReached()) {
            matingPopulation = this.selection(this.getPopulation());
            offspringPopulation = this.reproduction(matingPopulation);
            offspringPopulation = this.evaluatePopulation(offspringPopulation);
            this.setPopulation(this.replacement(this.getPopulation(), offspringPopulation));
            this.updateProgress();
            // specific GA needed computations
            this.specificMOEAComputations();
        }
    }

    public abstract void specificMOEAComputations();

    public List<Double> getReferencePoint() {
        return this.referencePoint;
    }

    public List<Double> getNadirPoint() {
        return this.nadirPoint;
    }

    private void initializeReferencePoint(int size) {
        for (int i = 0; i < size; i++)
            this.referencePoint.add(Double.POSITIVE_INFINITY);
    }

    private void initializeNadirPoint(int size) {
        for (int i = 0; i < size; i++)
            this.nadirPoint.add(Double.NEGATIVE_INFINITY);
    }

    protected void updateReferencePoint(S s) {
        for (int i = 0; i < s.getNumberOfObjectives(); i++)
            this.referencePoint.set(i, Math.min(this.referencePoint.get(i), s.getObjective(i)));
    }

    protected void updateNadirPoint(S s) {
        for (int i = 0; i < s.getNumberOfObjectives(); i++)
            this.nadirPoint.set(i, Math.max(this.nadirPoint.get(i), s.getObjective(i)));
    }

    public void updateReferencePoint(List<S> population) {
        for (S solution : population)
            this.updateReferencePoint(solution);
    }

    public void updateNadirPoint(List<S> population) {
        for (S solution : population)
            this.updateNadirPoint(solution);
    }


    protected boolean populationIsNotFull(List<S> population) {
        return population.size() < this.getMaxPopulationSize();
    }

    protected void setReferencePointValue(Double value, int index) {
        if ((index < 0) || (index >= this.referencePoint.size())) {
            throw new IndexOutOfBoundsException();
        }

        this.referencePoint.set(index, value);
    }
}
