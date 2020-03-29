package osh.mgmt.globalcontroller.jmetal.esc;

import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.solution.impl.DefaultBinarySolution;
import osh.configuration.oc.EAObjectives;
import osh.configuration.oc.VariableEncoding;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.BinaryEncodedVariableInformation;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a binary encoded version of the {@link EnergyManagementProblem}.
 *
 * @author Sebastian Kramer
 */
public class BinaryEnergyManagementProblem extends EnergyManagementProblem<BinarySolution> implements BinaryProblem {

    private static final long serialVersionUID = -7649643802236593447L;
    private final int[] bitCount;

    /**
     * Constructs this binary encoded energy management problem with the provided problem evaluator, the collection of
     * objectives and the solution distributor
     *
     * @param evaluator the evaluator for the problem
     * @param objectives the collection of objective
     * @param distributor the solution distributor for the problem
     */
    public BinaryEnergyManagementProblem(EMProblemEvaluator evaluator, List<EAObjectives> objectives,
                                         SolutionDistributor distributor) {
        super(evaluator, objectives);

        this.setNumberOfVariables(1);
        this.bitCount = new int[1];
        this.bitCount[0] =
                ((BinaryEncodedVariableInformation) distributor.getVariableInformation(VariableEncoding.BINARY)).getBitCount();
    }

    @Override
    public BinarySolution createSolution() {
        return new DefaultBinarySolution(this);
    }

    @Override
    public int getNumberOfBits(int index) {
        return this.bitCount[index];
    }

    @Override
    public int getTotalNumberOfBits() {
        return Arrays.stream(this.bitCount).sum();
    }

}
