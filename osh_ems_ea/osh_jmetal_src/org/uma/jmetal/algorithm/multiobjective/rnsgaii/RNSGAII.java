package org.uma.jmetal.algorithm.multiobjective.rnsgaii;

import org.uma.jmetal.algorithm.InteractiveAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RankingAndPreferenceSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.measure.Measurable;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;
import org.uma.jmetal.util.measure.impl.DurationMeasure;
import org.uma.jmetal.util.measure.impl.SimpleMeasureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class RNSGAII<S extends Solution<?>> extends NSGAII<S> implements
        InteractiveAlgorithm<S, List<S>>, Measurable {

    private final double epsilon;
    protected SimpleMeasureManager measureManager;
    protected BasicMeasure<List<S>> solutionListMeasure;
    protected CountingMeasure evaluations;
    protected DurationMeasure durationMeasure;
    private List<Double> interestPoint;

    /**
     * Constructor
     */
    public RNSGAII(Problem<S> problem, int populationSize,
                   int matingPoolSize, int offspringPopulationSize,
                   CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                   SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator,
                   List<Double> interestPoint, double epsilon) {
        super(problem, populationSize, matingPoolSize, offspringPopulationSize, crossoverOperator,
                mutationOperator, selectionOperator, new DominanceComparator<>(), evaluator);
        this.interestPoint = interestPoint;
        this.epsilon = epsilon;

        this.measureManager = new SimpleMeasureManager();
        this.measureManager.setPushMeasure("currentPopulation", this.solutionListMeasure);
        this.measureManager.setPushMeasure("currentEvaluation", this.evaluations);

        this.initMeasures();
    }

    @Override
    public void updatePointOfInterest(List<Double> newReferencePoints) {
        this.interestPoint = newReferencePoints;
    }

    @Override
    protected void initProgress() {
        this.evaluations.reset(this.getMaxPopulationSize());
    }

    @Override
    protected void updateProgress() {
        this.evaluations.increment(this.getMaxPopulationSize());
        this.solutionListMeasure.push(this.getPopulation());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations.get().intValue(), this.getPopulation())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        this.durationMeasure.reset();
        this.durationMeasure.start();
        super.run();
        this.durationMeasure.stop();
    }

    /* Measures code */
    private void initMeasures() {
        this.durationMeasure = new DurationMeasure();
        this.evaluations = new CountingMeasure(0);
        this.solutionListMeasure = new BasicMeasure<>();

        this.measureManager = new SimpleMeasureManager();
        this.measureManager.setPullMeasure("currentExecutionTime", this.durationMeasure);
        this.measureManager.setPullMeasure("currentEvaluation", this.evaluations);

        this.measureManager.setPushMeasure("currentPopulation", this.solutionListMeasure);
        this.measureManager.setPushMeasure("currentEvaluation", this.evaluations);
    }

    @Override
    public MeasureManager getMeasureManager() {
        return this.measureManager;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);

        RankingAndPreferenceSelection<S> rankingAndCrowdingSelection;
        rankingAndCrowdingSelection = new RankingAndPreferenceSelection<>(this.getMaxPopulationSize(), this.interestPoint, this.epsilon);

        return rankingAndCrowdingSelection.execute(jointPopulation);
    }

    @Override
    public String getName() {
        return "RNSGAII";
    }

    @Override
    public String getDescription() {
        return "Reference Point Based Nondominated Sorting Genetic Algorithm version II";
    }
}
