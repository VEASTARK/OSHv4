package osh.mgmt.globalcontroller.jmetal.esc;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.ArrayDoubleSolution;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.RealEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;

/**
 * Represents a real encoded version of the {@link EnergyManagementProblem}.
 *
 * @author Sebastian Kramer
 */
public class RealEnergyManagementProblem extends EnergyManagementProblem<DoubleSolution> implements DoubleProblem {

    private static final long serialVersionUID = -1109739689225594618L;
    private final double[] lowerLimit;
    private final double[] upperLimit;

    /**
     * Constructs this real encoded energy management problem with the provided problem evaluator and solution
     * distributor
     *
     * @param evaluator the evaluator for the problem
     * @param distributor the solution distributor for the problem
     */
    public RealEnergyManagementProblem(EMProblemEvaluator evaluator, SolutionDistributor distributor) {
        super(evaluator);

        RealEncodedVariableInformation variableInformation =
                (RealEncodedVariableInformation) distributor.getVariableInformation(VariableEncoding.REAL);

        int numberOfVariables = variableInformation.getVariableCount();
        double[][] boundaries = variableInformation.getVariableBoundaries();
        this.lowerLimit = new double[numberOfVariables];
        this.upperLimit = new double[numberOfVariables];

        this.setNumberOfVariables(numberOfVariables);

        for (int i = 0; i < boundaries.length; i++) {
            this.lowerLimit[i] = boundaries[i][0];
            this.upperLimit[i] = boundaries[i][1];
        }
    }

    @Override
    public DoubleSolution createSolution() {
        return new ArrayDoubleSolution(this);
    }

    @Override
    public Double getLowerBound(int index) {
        return this.lowerLimit[index];
    }

    @Override
    public Double getUpperBound(int index) {
        return this.upperLimit[index];
    }

    @Override
    public double getUnboxedLowerBound(int index) {
        return this.lowerLimit[index];
    }

    @Override
    public double getUnboxedUpperBound(int index) {
        return this.upperLimit[index];
    }
}
