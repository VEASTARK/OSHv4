package org.uma.jmetal.algorithm.multiobjective.gwasfga;

import org.uma.jmetal.algorithm.multiobjective.gwasfga.util.GWASFGARanking;
import org.uma.jmetal.algorithm.multiobjective.mombi.util.ASFWASFGA;
import org.uma.jmetal.algorithm.multiobjective.mombi.util.AbstractUtilityFunctionsSet;
import org.uma.jmetal.algorithm.multiobjective.wasfga.WASFGA;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;

import java.util.List;

/**
 * This class executes the GWASFGA algorithm described in:
 * Saborido, R., Ruiz, A. B. and Luque, M. (2015). Global WASF-GA: An Evolutionary Algorithm in
 * Multiobjective Optimization to Approximate the whole Pareto Optimal Front.
 * Evolutionary Computation Accepted for publication.
 *
 * @author Juanjo Durillo
 */
public class GWASFGA<S extends Solution<?>> extends WASFGA<S> {
    private static final long serialVersionUID = 1L;
    private final AbstractUtilityFunctionsSet<S> achievementScalarizingUtopia;
    private final AbstractUtilityFunctionsSet<S> achievementScalarizingNadir;

    public GWASFGA(Problem<S> problem, int populationSize, CrossoverOperator<S> crossoverOperator,
                   MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator,
                   SolutionListEvaluator<S> evaluator, double epsilon, String weightVectorsFileName) {
        super(problem, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator, epsilon,
                null, weightVectorsFileName);

        this.setMaxPopulationSize(populationSize);

        int halfVectorSize = super.weights.length / 2;
        int evenVectorsSize = (super.weights.length % 2 == 0) ? halfVectorSize : (halfVectorSize + 1);

        double[][] evenVectors = new double[evenVectorsSize][this.getProblem().getNumberOfObjectives()];
        double[][] oddVectors = new double[halfVectorSize][this.getProblem().getNumberOfObjectives()];

        int index = 0;
        for (int i = 0; i < super.weights.length; i += 2)
            evenVectors[index++] = super.weights[i];

        index = 0;
        for (int i = 1; i < super.weights.length; i += 2)
            oddVectors[index++] = super.weights[i];

        this.achievementScalarizingNadir = this.createUtilityFunction(this.getNadirPoint(), evenVectors);
        this.achievementScalarizingUtopia = this.createUtilityFunction(this.getReferencePoint(), oddVectors);
    }

    public GWASFGA(Problem<S> problem, int populationSize, CrossoverOperator<S> crossoverOperator,
                   MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator,
                   SolutionListEvaluator<S> evaluator, double epsilon) {
        this(problem, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator, epsilon,
                "");
    }

    private AbstractUtilityFunctionsSet<S> createUtilityFunction(List<Double> referencePoint, double[][] weights) {
        return new ASFWASFGA<>(weights, referencePoint);
    }

    protected Ranking<S> computeRanking(List<S> solutionList) {
        Ranking<S> ranking = new GWASFGARanking<>(this.achievementScalarizingUtopia, this.achievementScalarizingNadir);
        ranking.computeRanking(solutionList);
        return ranking;
    }

    @Override
    public String getName() {
        return "GWASFGA";
    }

    @Override
    public String getDescription() {
        return "Global Weighting Achievement Scalarizing Function Genetic Algorithm";
    }
}
