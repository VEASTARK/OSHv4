package org.uma.jmetal.algorithm.multiobjective.abyss;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.LocalSearchOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.localsearch.ArchiveMutationLocalSearch;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;

/**
 * @author Cristobal Barba
 */
public class ABYSSBuilder implements AlgorithmBuilder<ABYSS> {
    private final DoubleProblem problem;
    private final CrowdingDistanceArchive<DoubleSolution> archive;
    protected LocalSearchOperator<DoubleSolution> improvementOperator;
    private CrossoverOperator<DoubleSolution> crossoverOperator;
    private MutationOperator<DoubleSolution> mutationOperator;
    private int numberOfSubranges;
    private int populationSize;
    private int refSet1Size;
    private int refSet2Size;
    private int archiveSize;
    private int maxEvaluations;

    public ABYSSBuilder(DoubleProblem problem, Archive<DoubleSolution> archive) {
        this.populationSize = 20;
        this.maxEvaluations = 25000;
        this.archiveSize = 100;
        this.refSet1Size = 10;
        this.refSet2Size = 10;
        this.numberOfSubranges = 4;
        this.problem = problem;
        double crossoverProbability = 0.9;
        double distributionIndex = 20.0;
        this.crossoverOperator = new SBXCrossover(crossoverProbability, distributionIndex);
        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        this.mutationOperator = new PolynomialMutation(mutationProbability, distributionIndex);
        int improvementRounds = 1;
        this.archive = (CrowdingDistanceArchive<DoubleSolution>) archive;
        this.improvementOperator = new ArchiveMutationLocalSearch<>(improvementRounds, this.mutationOperator, this.archive, problem);
    }

    public CrossoverOperator<DoubleSolution> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public ABYSSBuilder setCrossoverOperator(CrossoverOperator<DoubleSolution> crossoverOperator) {
        this.crossoverOperator = crossoverOperator;
        return this;
    }

    public LocalSearchOperator<DoubleSolution> getImprovementOperator() {
        return this.improvementOperator;
    }

    public ABYSSBuilder setImprovementOperator(ArchiveMutationLocalSearch<DoubleSolution> improvementOperator) {
        this.improvementOperator = improvementOperator;
        return this;
    }

    public MutationOperator<DoubleSolution> getMutationOperator() {
        return this.mutationOperator;
    }

    public ABYSSBuilder setMutationOperator(MutationOperator<DoubleSolution> mutationOperator) {
        this.mutationOperator = mutationOperator;
        return this;
    }

    public int getNumberOfSubranges() {
        return this.numberOfSubranges;
    }

    public ABYSSBuilder setNumberOfSubranges(int numberOfSubranges) {
        this.numberOfSubranges = numberOfSubranges;
        return this;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public ABYSSBuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    public int getRefSet1Size() {
        return this.refSet1Size;
    }

    public ABYSSBuilder setRefSet1Size(int refSet1Size) {
        this.refSet1Size = refSet1Size;
        return this;
    }

    public int getRefSet2Size() {
        return this.refSet2Size;
    }

    public ABYSSBuilder setRefSet2Size(int refSet2Size) {
        this.refSet2Size = refSet2Size;
        return this;
    }

    public int getArchiveSize() {
        return this.archiveSize;
    }

    public ABYSSBuilder setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;
        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public ABYSSBuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
        return this;
    }

    @Override
    public ABYSS build() {
        ABYSS algorithm =  new ABYSS(this.problem, this.populationSize, this.refSet1Size,
                this.refSet2Size, this.archiveSize, this.archive, this.improvementOperator, this.crossoverOperator,
                this.numberOfSubranges);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
        return algorithm;
    }
}
