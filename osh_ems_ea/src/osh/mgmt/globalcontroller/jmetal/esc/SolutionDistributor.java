package osh.mgmt.globalcontroller.jmetal.esc;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.ArrayReal;
import jmetal.encodings.variable.Binary;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.AbstractEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.BinaryEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.RealEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;

import java.util.BitSet;
import java.util.List;

/**
 * Collection and distribution class for information about encoding variables one-way and decoding provided solutions
 * the other way.
 *
 * @author Sebastian Kramer
 */
public class SolutionDistributor {

    /**
     * the start- and end-positions of the solutions for the part-problems in a binary encoded solution.
     */
    private int[][] solutionPositionsForBinary;

    /**
     * the start- and end-positions of the solutions for the part-problems in a real encoded solution.
     */
    private int[][] solutionPositionsForReal;

    private BinaryEncodedVariableInformation binaryVariableInformation;
    private RealEncodedVariableInformation realVariableInformation;

    /**
     * Gathers all encoding information from the provided problem-parts and stores them.
     *
     * @param problemParts all problem parts to gather encdoing information from
     */
    public void gatherVariableInformation(List<InterdependentProblemPart<? extends ISolution, ? extends IPrediction>> problemParts) {
        this.binaryVariableInformation = this.gatherBinaryVariableInformation(problemParts);
        this.realVariableInformation = this.gatherRealVariableInformation(problemParts);
    }

    /**
     * Gathers how many bits each given problem-part requires and stores the start- and end-positions of each
     * problem-part in the encoded bit-string.
     *
     * @param problemParts the given problem-parts
     * @return encoding information of the merged problem from all given problem-parts
     */
    private BinaryEncodedVariableInformation gatherBinaryVariableInformation(List<InterdependentProblemPart<?
            extends ISolution, ? extends IPrediction>> problemParts) {

        this.solutionPositionsForBinary = new int[problemParts.size()][2];
        int bitCount = 0;

        for (InterdependentProblemPart<?, ?> ipp : problemParts) {
            int bitsUsed =
                    ((BinaryEncodedVariableInformation) ipp.getSolutionHandler().getVariableInformation(VariableEncoding.BINARY)).getBitCount();

            this.solutionPositionsForBinary[ipp.getId()] = new int[]{bitCount, bitCount + bitsUsed};
            bitCount += bitsUsed;
        }

        return new BinaryEncodedVariableInformation(bitCount);
    }

    /**
     * Gathers how many variables with the required variable boundaries each given problem-part requires and
     * stores the start- and end-positions of each problem-part in the encoded real-array.
     *
     * @param problemParts the given problem-parts
     * @return encoding information of the merged problem from all given problem-parts
     */
    private RealEncodedVariableInformation gatherRealVariableInformation(List<InterdependentProblemPart<?
            extends ISolution, ? extends IPrediction>> problemParts) {

        this.solutionPositionsForReal = new int[problemParts.size()][2];
        int variableCount = 0;
        double[][][] boundaries = new double[problemParts.size()][][];

        for (InterdependentProblemPart<?, ?> ipp : problemParts) {

            RealEncodedVariableInformation info =
                    ((RealEncodedVariableInformation) ipp.getSolutionHandler().getVariableInformation(VariableEncoding.REAL));

            this.solutionPositionsForReal[ipp.getId()] = new int[]{variableCount,
                    variableCount + info.getVariableCount()};
            variableCount += info.getVariableCount();

            boundaries[ipp.getId()] = info.getVariableBoundaries();
        }

        double[][] resultingBoundaries = new double[variableCount][2];

        int index = 0;

        for (double[][] boundary : boundaries) {
            for (double[] doubles : boundary) {
                resultingBoundaries[index] = doubles;
                index++;
            }
        }

        return new RealEncodedVariableInformation(variableCount, resultingBoundaries);
    }

    /**
     * Returns the encoding information for the given encoding type.
     *
     * @param encodingType the given required encoding type
     * @return the encoding information for the given encoding type
     */
    public AbstractEncodedVariableInformation getVariableInformation(VariableEncoding encodingType) {
        return encodingType == VariableEncoding.BINARY ? this.binaryVariableInformation : this.realVariableInformation;
    }

    /**
     * Distributes the given encoded solution to all given problem-parts to decode and implement.
     *
     * @param solution the encoded solution
     * @param problemParts the problem-parts to distribute the solution to
     */
    public void distributeSolution(Solution solution, List<InterdependentProblemPart<? extends ISolution, ? extends
                IPrediction>> problemParts) {

        if (solution.getType().getClass() == BinarySolutionType.class) {
            this.distributeBinarySolution(((Binary) solution.getDecisionVariables()[0]).bits_, problemParts);
        } else if (solution.getType().getClass() == ArrayRealSolutionType.class){
            this.distributeRealSolution(((ArrayReal) solution.getDecisionVariables()[0]).array_, problemParts);
        }
    }

    private void distributeBinarySolution(BitSet solution, List<InterdependentProblemPart<? extends ISolution, ? extends
            IPrediction>> problemParts) {

        for (InterdependentProblemPart<?, ?> ipp : problemParts) {

            int[] positions = this.solutionPositionsForBinary[ipp.getId()];

            //no variable for this ipp
            if (positions[1] - positions[0] == 0) {
                continue;
            }

            ipp.setSolution(solution.get(positions[0], positions[1]));
        }
    }

    private void distributeRealSolution(Double[] solution, List<InterdependentProblemPart<? extends ISolution, ? extends
            IPrediction>> problemParts) {

        assert (solution.length == this.realVariableInformation.getVariableCount());

        for (InterdependentProblemPart<?, ?> ipp : problemParts) {

            int[] positions = this.solutionPositionsForReal[ipp.getId()];

            //no variable for this ipp
            if (positions[1] - positions[0] == 0) {
                continue;
            }

            double[] variables = new double[positions[1] - positions[0]];

            for (int i = positions[0]; i < positions[1]; i++) {
                variables[i - positions[0]] = solution[i];
            }

            ipp.setSolution(variables);
        }
    }
}
