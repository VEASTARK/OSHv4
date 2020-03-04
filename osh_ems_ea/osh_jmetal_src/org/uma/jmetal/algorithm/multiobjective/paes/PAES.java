package org.uma.jmetal.algorithm.multiobjective.paes;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionStrategy;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.impl.AdaptiveGridArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 * <p>
 * This class implements the PAES algorithm.
 */
@SuppressWarnings("serial")
public class PAES<S extends Solution<?>> extends AbstractEvolutionStrategy<S, List<S>> {
    protected final int archiveSize;
    protected final int maxEvaluations;
    protected final int biSections;
    protected final AdaptiveGridArchive<S> archive;
    protected final Comparator<S> comparator;
    protected int evaluations;

    /**
     * Constructor
     */
    public PAES(Problem<S> problem, int archiveSize, int maxEvaluations, int biSections,
                MutationOperator<S> mutationOperator) {
        super(problem);
        this.setProblem(problem);
        this.archiveSize = archiveSize;
        this.maxEvaluations = maxEvaluations;
        this.biSections = biSections;
        this.mutationOperator = mutationOperator;

        this.archive = new AdaptiveGridArchive<>(archiveSize, biSections, problem.getNumberOfObjectives());
        this.comparator = new DominanceComparator<>();
    }

    /* Getters */
    public int getArchiveSize() {
        return this.archiveSize;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public int getBiSections() {
        return this.biSections;
    }

    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    @Override
    protected void initProgress() {
        this.evaluations = 0;
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
    protected List<S> createInitialPopulation() {
        List<S> solutionList = new ArrayList<>(1);
        solutionList.add(this.getProblem().createSolution());
        return solutionList;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        this.getProblem().evaluate(population.get(0));
        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        return population;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<S> reproduction(List<S> population) {
        S mutatedSolution = (S) population.get(0).copy();
        this.mutationOperator.execute(mutatedSolution);

        List<S> mutationSolutionList = new ArrayList<>(1);
        mutationSolutionList.add(mutatedSolution);
        return mutationSolutionList;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        S current = population.get(0);
        S mutatedSolution = offspringPopulation.get(0);

        int flag = this.comparator.compare(current, mutatedSolution);
        if (flag == 1) {
            current = (S) mutatedSolution.copy();
            this.archive.add(mutatedSolution);
        } else if (flag == 0) {
            if (this.archive.add(mutatedSolution)) {
                population.set(0, this.test(current, mutatedSolution, this.archive));
            }
        }

        population.set(0, current);
        return population;
    }

    @Override
    public List<S> getResult() {
        return this.archive.getSolutionList();
    }

    /**
     * Tests two solutions to determine which one becomes be the guide of PAES
     * algorithm
     *
     * @param solution        The actual guide of PAES
     * @param mutatedSolution A candidate guide
     */
    @SuppressWarnings("unchecked")
    public S test(S solution, S mutatedSolution, AdaptiveGridArchive<S> archive) {
        int originalLocation = archive.getGrid().location(solution);
        int mutatedLocation = archive.getGrid().location(mutatedSolution);

        if (originalLocation == -1) {
            return (S) mutatedSolution.copy();
        }

        if (mutatedLocation == -1) {
            return (S) solution.copy();
        }

        if (archive.getGrid().getLocationDensity(mutatedLocation) < archive.getGrid()
                .getLocationDensity(originalLocation)) {
            return (S) mutatedSolution.copy();
        }

        return (S) solution.copy();
    }

    @Override
    public String getName() {
        return "PAES";
    }

    @Override
    public String getDescription() {
        return "Pareto-Archived Evolution Strategy";
    }

}
