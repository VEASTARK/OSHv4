package org.uma.jmetal.algorithm.multiobjective.espea;

import org.uma.jmetal.algorithm.multiobjective.espea.util.EnergyArchive.ReplacementStrategy;
import org.uma.jmetal.algorithm.multiobjective.espea.util.ScalarizationWrapper;
import org.uma.jmetal.algorithm.multiobjective.espea.util.ScalarizationWrapper.ScalarizationType;
import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RandomSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.List;

public class ESPEABuilder<S extends Solution<?>> implements AlgorithmBuilder<ESPEA<S>> {

    private final Problem<S> problem;
    private final IEALogger eaLogger;
    private int maxEvaluations;
    private int populationSize;
    private CrossoverOperator<S> crossoverOperator;
    private CrossoverOperator<S> fullArchiveCrossoverOperator;
    private MutationOperator<S> mutationOperator;
    private SelectionOperator<List<S>, S> selectionOperator;
    private SolutionListEvaluator<S> evaluator;
    private ScalarizationWrapper scalarization;
    private boolean normalizeObjectives;
    private ReplacementStrategy replacementStrategy;

    public ESPEABuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                        MutationOperator<S> mutationOperator, IEALogger eaLogger) {
        this.problem = problem;
        this.eaLogger = eaLogger;
        this.maxEvaluations = 25000;
        this.populationSize = 100;
        this.crossoverOperator = crossoverOperator;
        this.fullArchiveCrossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = new RandomSelection<>();
        this.evaluator = new SequentialSolutionListEvaluator<>();
        this.scalarization = new ScalarizationWrapper(ScalarizationType.UNIFORM);
        this.normalizeObjectives = true;
        this.replacementStrategy = ReplacementStrategy.LARGEST_DIFFERENCE;
    }

    @Override
    public ESPEA<S> build() {
        ESPEA<S> algorithm = new ESPEA<>(this.problem, this.populationSize, this.crossoverOperator,
                this.fullArchiveCrossoverOperator, this.mutationOperator,
                this.selectionOperator, this.scalarization, this.evaluator, this.normalizeObjectives,
                this.replacementStrategy, this.eaLogger);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
        return algorithm;
    }

    /**
     * @return the maxEvaluations
     */
    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    /**
     * @param maxEvaluations the maxEvaluations to set
     */
    public void setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
    }

    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return this.populationSize;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * @return the crossoverOperator
     */
    public CrossoverOperator<S> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    /**
     * @param crossoverOperator the crossoverOperator to set
     */
    public void setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
        this.crossoverOperator = crossoverOperator;
    }

    /**
     * @return the fullArchiveCrossoverOperator
     */
    public CrossoverOperator<S> getFullArchiveCrossoverOperator() {
        return this.fullArchiveCrossoverOperator;
    }

    /**
     * @param fullArchiveCrossoverOperator the fullArchiveCrossoverOperator to set
     */
    public void setFullArchiveCrossoverOperator(CrossoverOperator<S> fullArchiveCrossoverOperator) {
        this.fullArchiveCrossoverOperator = fullArchiveCrossoverOperator;
    }

    /**
     * @return the mutationOperator
     */
    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    /**
     * @param mutationOperator the mutationOperator to set
     */
    public void setMutationOperator(MutationOperator<S> mutationOperator) {
        this.mutationOperator = mutationOperator;
    }

    /**
     * @return the selectionOperator
     */
    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return this.selectionOperator;
    }

    /**
     * @param selectionOperator the selectionOperator to set
     */
    public void setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        this.selectionOperator = selectionOperator;
    }

    /**
     * @return the evaluator
     */
    public SolutionListEvaluator<S> getEvaluator() {
        return this.evaluator;
    }

    /**
     * @param evaluator the evaluator to set
     */
    public void setEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * @return the scalarization
     */
    public ScalarizationWrapper getScalarization() {
        return this.scalarization;
    }

    /**
     * @param scalarization the scalarization to set
     */
    public void setScalarization(ScalarizationWrapper scalarization) {
        this.scalarization = scalarization;
    }

    /**
     * @return the normalizeObjectives
     */
    public boolean isNormalizeObjectives() {
        return this.normalizeObjectives;
    }

    /**
     * @param normalizeObjectives the normalizeObjectives to set
     */
    public void setNormalizeObjectives(boolean normalizeObjectives) {
        this.normalizeObjectives = normalizeObjectives;
    }

    /**
     * @return the replacement strategy
     */
    public ReplacementStrategy getOperationType() {
        return this.replacementStrategy;
    }

    /**
     * @param replacementStrategy the replacement strategy to set
     */
    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }
}
