package org.uma.jmetal.algorithm.multiobjective.wasfga;

import org.uma.jmetal.algorithm.InteractiveAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.mombi.AbstractMOMBI;
import org.uma.jmetal.algorithm.multiobjective.mombi.util.ASFWASFGA;
import org.uma.jmetal.algorithm.multiobjective.mombi.util.AbstractUtilityFunctionsSet;
import org.uma.jmetal.algorithm.multiobjective.wasfga.util.WASFGARanking;
import org.uma.jmetal.algorithm.multiobjective.wasfga.util.WeightVectors;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.fileinput.VectorFileUtils;
import org.uma.jmetal.util.solutionattribute.Ranking;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the preference based algorithm named WASF-GA on jMetal5.0
 *
 * @author Juanjo Durillo
 * <p>
 * This algorithm is described in the paper: A.B. Ruiz, R. Saborido, M.
 * Luque "A Preference-based Evolutionary Algorithm for Multiobjective
 * Optimization: The Weighting Achievement Scalarizing Function Genetic
 * Algorithm". Journal of Global Optimization. May 2015, Volume 62,
 * Issue 1, pp 101-129
 * DOI = {10.1007/s10898-014-0214-y}
 */
public class WASFGA<S extends Solution<?>> extends AbstractMOMBI<S> implements
        InteractiveAlgorithm<S, List<S>> {
    private static final long serialVersionUID = 1L;
    protected final double epsilon;
    private final AbstractUtilityFunctionsSet<S> achievementScalarizingFunction;
    private final List<Double> interestPoint;
    private final String weightVectorsFileName;
    protected int evaluations;
    protected double[][] weights;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public WASFGA(Problem<S> problem,
                  int populationSize,
                  CrossoverOperator<S> crossoverOperator,
                  MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator,
                  SolutionListEvaluator<S> evaluator,
                  double epsilon,
                  List<Double> referencePoint,
                  String weightVectorsFileName,
                  IEALogger eaLogger) {

        super(problem, crossoverOperator, mutationOperator, selectionOperator, evaluator, eaLogger);
        this.weightVectorsFileName = weightVectorsFileName;
        this.setMaxPopulationSize(populationSize);
        this.interestPoint = referencePoint;
        this.achievementScalarizingFunction = this.createUtilityFunction();
        this.epsilon = epsilon;
    }

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public WASFGA(Problem<S> problem,
                  int populationSize,
                  CrossoverOperator<S> crossoverOperator,
                  MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator,
                  SolutionListEvaluator<S> evaluator,
                  double epsilon,
                  List<Double> referencePoint,
                  IEALogger eaLogger) {

        this(problem,
                populationSize,
                crossoverOperator,
                mutationOperator,
                selectionOperator,
                evaluator,
                epsilon,
                referencePoint,
                "",
                eaLogger);
    }

    public AbstractUtilityFunctionsSet<S> createUtilityFunction() {
        //If a file with weight vectors is not given as parameter, weights are calculated or read from the resources file of jMetal
        if (this.weightVectorsFileName != null && this.weightVectorsFileName.isEmpty()) {
            //For two biobjective problems weights are computed
            if (this.problem.getNumberOfObjectives() == 2) {
                this.weights = WeightVectors.initializeUniformlyInTwoDimensions(this.epsilon, this.getMaxPopulationSize());
            }
            //For more than two objectives, weights are read from the resources file of jMetal
            else {
                String dataFileName = "W" + this.problem.getNumberOfObjectives() + "D_" + this.getMaxPopulationSize() + ".dat";
                this.weights = VectorFileUtils.readVectors("MOEAD_Weights/" + dataFileName);
            }
        } else { //If a file with weight vectors is given as parameter, weights are read from that file
            //weights = WeightVectors.readFromFile(this.weightVectorsFileName) ;
            this.weights = VectorFileUtils.readVectors(this.weightVectorsFileName);
        }
        this.weights = WeightVectors.invert(this.weights, true);

        //We validate that the weight vectors are valid:
        //The number of components of each weight is similar to the number of objectives of the problem being solved.
        if (!WeightVectors.validate(this.weights, this.problem.getNumberOfObjectives())) {
            throw new JMetalException("Weight vectors are invalid. Check that weight vectors have as many components" +
                    " as objectives the problem being solved has.");
        }

        //By default, the algorithm uses as many weight vectors as individual in the population.
        //In a future, a new parameter should be added to specify the number of weight vectors to use.
        //As it is mentioned in the paper, the number of weight vectors must lower than or equal to the population size.
        if (this.weights.length != this.maxPopulationSize) {
            throw new JMetalException("The number of weight vectors (" + this.weights.length + ") and the population size (" +
                    this.maxPopulationSize + ") have different values. This behaviour will change in a future " +
                    "version.");
        }

        return new ASFWASFGA<>(this.weights, this.interestPoint);
    }

    public void updatePointOfInterest(List<Double> newPointOfInterest) {
        ((ASFWASFGA<S>) this.achievementScalarizingFunction).updatePointOfInterest(newPointOfInterest);
    }

    public int getPopulationSize() {
        return this.getMaxPopulationSize();
    }

    @Override
    public void specificMOEAComputations() {
        this.updateNadirPoint(this.getPopulation());
        this.updateReferencePoint(this.getPopulation());
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);
        Ranking<S> ranking = this.computeRanking(jointPopulation);
        return this.selectBest(ranking);
    }

    protected Ranking<S> computeRanking(List<S> solutionList) {
        Ranking<S> ranking = new WASFGARanking<>(this.achievementScalarizingFunction);
        ranking.computeRanking(solutionList);
        return ranking;
    }

    protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int index, List<S> population) {
        population.addAll(ranking.getSubfront(index));
    }

    protected void addLastRankedSolutionsToPopulation(Ranking<S> ranking, int index, List<S> population) {
        List<S> front = ranking.getSubfront(index);
        int remain = this.getPopulationSize() - population.size();
        population.addAll(front.subList(0, remain));
    }

    protected List<S> selectBest(Ranking<S> ranking) {
        List<S> population = new ArrayList<>(this.getPopulationSize());
        int rankingIndex = 0;

        while (this.populationIsNotFull(population)) {
            if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
                this.addRankedSolutionsToPopulation(ranking, rankingIndex, population);
                rankingIndex++;
            } else {
                this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
            }
        }
        return population;
    }

    private boolean subfrontFillsIntoThePopulation(Ranking<S> ranking, int index, List<S> population) {
        return (population.size() + ranking.getSubfront(index).size() < this.getPopulationSize());
    }

    @Override
    public List<S> getResult() {
        return this.getNonDominatedSolutions(this.getPopulation());
    }

    protected List<S> getNonDominatedSolutions(List<S> solutionList) {
        return SolutionListUtils.getNondominatedSolutions(solutionList);
    }

    @Override
    public String getName() {
        return "WASFGA";
    }

    @Override
    public String getDescription() {
        return "Weighting Achievement Scalarizing Function Genetic Algorithm";
    }
}
