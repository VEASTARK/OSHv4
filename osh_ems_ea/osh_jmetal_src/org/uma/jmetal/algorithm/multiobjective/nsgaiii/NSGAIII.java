package org.uma.jmetal.algorithm.multiobjective.nsgaiii;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.EnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.ReferencePoint;
import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by ajnebro on 30/10/14.
 * Modified by Juanjo on 13/11/14
 * <p>
 * This implementation is based on the code of Tsung-Che Chiang
 * http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm
 */
@SuppressWarnings("serial")
public class NSGAIII<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final SolutionListEvaluator<S> evaluator;
    protected final Vector<Integer> numberOfDivisions;
    protected final List<ReferencePoint<S>> referencePoints = new Vector<>();
    protected int iterations;

    /**
     * Constructor
     */
    public NSGAIII(NSGAIIIBuilder<S> builder) { // can be created from the NSGAIIIBuilder within the same package
        super(builder.getProblem());
        this.addStoppingRule(new EvaluationsStoppingRule(builder.getPopulationSize(),
                builder.getMaxIterations() * builder.getPopulationSize()));

        this.crossoverOperator = builder.getCrossoverOperator();
        this.mutationOperator = builder.getMutationOperator();
        this.selectionOperator = builder.getSelectionOperator();

        this.evaluator = builder.getEvaluator();

        /// NSGAIII
        this.numberOfDivisions = new Vector<>(1);
        this.numberOfDivisions.add(12); // Default value for 3D problems

        (new ReferencePoint<S>()).generateReferencePoints(this.referencePoints, this.getProblem().getNumberOfObjectives(), this.numberOfDivisions);

        int populationSize = this.referencePoints.size();
        while (populationSize % 4 > 0) {
            populationSize++;
        }

        this.setMaxPopulationSize(populationSize);

        JMetalLogger.logger.info("rpssize: " + this.referencePoints.size());
    }

    /**
     * Constructor
     *
     * @param problem
     * @param crossoverOperator
     * @param mutationOperator
     * @param selectionOperator
     * @param evaluator
     */
    public NSGAIII(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                   MutationOperator<S> mutationOperator,
                   SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        super(problem);

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.evaluator = evaluator;

        /* divisions according to the original NSGAIII study
         *
         *   objectives   | outer   | inner
         *   3            | 12      | 0
         *   5            |  6      | 0
         *   8            |  3      | 2
         *   10           |  3      | 2
         *   15           |  2      | 1
         */
        int noObjectives = this.getProblem().getNumberOfObjectives();

        if (noObjectives <= 3) {
            this.numberOfDivisions = new Vector<>(1);
            this.numberOfDivisions.add(12);
        } else if (noObjectives <= 6) {
            this.numberOfDivisions = new Vector<>(1);
            this.numberOfDivisions.add(6);
        } else if (noObjectives <= 8) {
            this.numberOfDivisions = new Vector<>(2);
            this.numberOfDivisions.add(3);
            this.numberOfDivisions.add(2);
        } else if (noObjectives <= 10) {
            this.numberOfDivisions = new Vector<>(2);
            this.numberOfDivisions.add(3);
            this.numberOfDivisions.add(2);
        } else {
            this.numberOfDivisions = new Vector<>(2);
            this.numberOfDivisions.add(2);
            this.numberOfDivisions.add(1);
        }

        (new ReferencePoint<S>()).generateReferencePoints(this.referencePoints, noObjectives, this.numberOfDivisions);

        int populationSize = this.referencePoints.size();
        while (populationSize % 4 > 0) {
            populationSize++;
        }

        this.setMaxPopulationSize(populationSize);

        JMetalLogger.logger.info("rpssize: " + referencePoints.size()); ;
    }

    @Override
    protected void initProgress() {
        this.iterations = 1;
    }

    @Override
    protected void updateProgress() {
        this.iterations++;
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
            parents.add(population.get(i));
            parents.add(population.get(Math.min(i + 1, this.getMaxPopulationSize() - 1)));

            List<S> offspring = this.crossoverOperator.execute(parents);

            this.mutationOperator.execute(offspring.get(0));
            this.mutationOperator.execute(offspring.get(1));

            offspringPopulation.add(offspring.get(0));
            offspringPopulation.add(offspring.get(1));
        }
        return offspringPopulation;
    }


    private List<ReferencePoint<S>> getReferencePointsCopy() {
        List<ReferencePoint<S>> copy = new ArrayList<>();
        for (ReferencePoint<S> r : this.referencePoints) {
            copy.add(new ReferencePoint<>(r));
        }
        return copy;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {

        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);

        Ranking<S> ranking = this.computeRanking(jointPopulation);

        //List<Solution> pop = crowdingDistanceSelection(ranking);
        List<S> pop = new ArrayList<>();
        List<List<S>> fronts = new ArrayList<>();
        int rankingIndex = 0;
        int candidateSolutions = 0;
        while (candidateSolutions < this.getMaxPopulationSize()) {
            fronts.add(ranking.getSubfront(rankingIndex));
            candidateSolutions += ranking.getSubfront(rankingIndex).size();
            if ((pop.size() + ranking.getSubfront(rankingIndex).size()) <= this.getMaxPopulationSize())
                this.addRankedSolutionsToPopulation(ranking, rankingIndex, pop);
            rankingIndex++;
        }

        // A copy of the reference list should be used as parameter of the environmental selection
        EnvironmentalSelection<S> selection =
                new EnvironmentalSelection<>(fronts, this.getMaxPopulationSize(), this.getReferencePointsCopy(),
                        this.getProblem().getNumberOfObjectives());

        pop = selection.execute(pop);

        return pop;
    }

    @Override
    public List<S> getResult() {
        return this.getNonDominatedSolutions(this.getPopulation());
    }

    protected Ranking<S> computeRanking(List<S> solutionList) {
        Ranking<S> ranking = new DominanceRanking<>();
        ranking.computeRanking(solutionList);

        return ranking;
    }

    protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
        List<S> front;

        front = ranking.getSubfront(rank);

        population.addAll(front);
    }

    protected List<S> getNonDominatedSolutions(List<S> solutionList) {
        return SolutionListUtils.getNondominatedSolutions(solutionList);
    }

    @Override
    public String getName() {
        return "NSGAIII";
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version III";
    }

}
