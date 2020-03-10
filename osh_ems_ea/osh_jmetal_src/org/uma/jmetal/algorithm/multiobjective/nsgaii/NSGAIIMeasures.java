package org.uma.jmetal.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.measure.Measurable;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;
import org.uma.jmetal.util.measure.impl.DurationMeasure;
import org.uma.jmetal.util.measure.impl.SimpleMeasureManager;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAIIMeasures<S extends Solution<?>> extends NSGAII<S> implements Measurable {
    protected CountingMeasure evaluations;
    protected DurationMeasure durationMeasure;
    protected SimpleMeasureManager measureManager;

    protected BasicMeasure<List<S>> solutionListMeasure;
    protected BasicMeasure<Integer> numberOfNonDominatedSolutionsInPopulation;
    protected BasicMeasure<Double> hypervolumeValue;

    protected Front referenceFront;

    /**
     * Constructor
     */
    public NSGAIIMeasures(Problem<S> problem, int populationSize,
                          int matingPoolSize, int offspringPopulationSize,
                          CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                          SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator, SolutionListEvaluator<S> evaluator) {
        super(problem, populationSize, matingPoolSize, offspringPopulationSize,
                crossoverOperator, mutationOperator, selectionOperator, dominanceComparator, evaluator);

        this.referenceFront = new ArrayFront();

        this.initMeasures();
    }

    @Override
    protected void initProgress() {
        this.evaluations.reset(this.getMaxPopulationSize());
    }

    @Override
    protected void updateProgress() {
        this.evaluations.increment(this.getMaxPopulationSize());

        this.solutionListMeasure.push(this.getPopulation());

        if (this.referenceFront.getNumberOfPoints() > 0) {
            this.hypervolumeValue.push(
                    new PISAHypervolume<S>(this.referenceFront).evaluate(
                            SolutionListUtils.getNondominatedSolutions(this.getPopulation())));
        }
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
        this.numberOfNonDominatedSolutionsInPopulation = new BasicMeasure<>();
        this.solutionListMeasure = new BasicMeasure<>();
        this.hypervolumeValue = new BasicMeasure<>();

        this.measureManager = new SimpleMeasureManager();
        this.measureManager.setPullMeasure("currentExecutionTime", this.durationMeasure);
        this.measureManager.setPullMeasure("currentEvaluation", this.evaluations);
        this.measureManager.setPullMeasure("numberOfNonDominatedSolutionsInPopulation",
                this.numberOfNonDominatedSolutionsInPopulation);

        this.measureManager.setPushMeasure("currentPopulation", this.solutionListMeasure);
        this.measureManager.setPushMeasure("currentEvaluation", this.evaluations);
        this.measureManager.setPushMeasure("hypervolume", this.hypervolumeValue);
    }

    @Override
    public MeasureManager getMeasureManager() {
        return this.measureManager;
    }

    @Override
    protected List<S> replacement(List<S> population,
                                  List<S> offspringPopulation) {
        List<S> pop = super.replacement(population, offspringPopulation);

        Ranking<S> ranking = new DominanceRanking<>(this.dominanceComparator);
        ranking.computeRanking(population);

        this.numberOfNonDominatedSolutionsInPopulation.set(ranking.getSubfront(0).size());

        return pop;
    }

    public CountingMeasure getEvaluations() {
        return this.evaluations;
    }

    @Override
    public String getName() {
        return "NSGAIIM";
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version II. Version using measures";
    }

    public void setReferenceFront(Front referenceFront) {
        this.referenceFront = referenceFront;
    }
}
