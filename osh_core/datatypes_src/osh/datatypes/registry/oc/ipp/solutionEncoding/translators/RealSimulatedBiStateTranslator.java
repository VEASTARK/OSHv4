package osh.datatypes.registry.oc.ipp.solutionEncoding.translators;

import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.AbstractEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.RealEncodedVariableInformation;

import java.util.Arrays;

/**
 * Represents an extension of the {@link RealVariableTranslator} where a state-machine is simulated on real encoded
 * variables to function like {@link BinaryBiStateVariableTranslator}.
 *
 * @author Sebastian Kramer
 */
public class RealSimulatedBiStateTranslator extends RealVariableTranslator {

    private static final long serialVersionUID = -1003700618048227434L;

    /**
     * how many bits are used to represent each state of the state-machine
     */
    private final int bitsPerState;

    /**
     * No-arg constructor for serialization.
     */
    protected RealSimulatedBiStateTranslator() {
        this.bitsPerState = -1;
    }

    /**
     * Creates this variable translator with the given number of simulated bits to use to represent each state of the
     * state-machine.
     *
     * @param bitsPerState the number of simulated bits to use to represent each state of the state-machine
     */
    public RealSimulatedBiStateTranslator(int bitsPerState) {
        this.bitsPerState = bitsPerState;
    }

    @Override
    public AbstractEncodedVariableInformation getVariableInformationLong(int variableCount, double[][] variableBoundaries) {
        double[][] correctedVariableBoundaries = new double[variableCount][2];
        Arrays.fill(correctedVariableBoundaries, new double[]{0.0, 1.0});
        return new RealEncodedVariableInformation(variableCount, correctedVariableBoundaries);
    }

    @Override
    public DecodedSolutionWrapper decodeLong(double[] encodedVariable, int variableCount, double[][] variableBoundaries) {
        long[] decoded = new long[variableCount];
        /*
            The number space between 0.0 and 1.0 will be parted into as many divisions as the number of bits to
            simulate. The lowest partition will then represent a move towards the off-state while the highest will
            represent a move towards the on-state. All others will be interpreted as 'remain in the previous state'.
         */
        double lowerBound = 1.0 / Math.pow(2.0, this.bitsPerState);
        double upperBound = 1.0 - lowerBound;

        for (int i = 0; i < variableCount; i++) {

            double variable = encodedVariable[i];

            if (variable < lowerBound) {
                decoded[i] = 0;
            } else if (variable > upperBound) {
                decoded[i] = 1;
            } else {
                decoded[i] = 2;
            }
        }

        return new DecodedSolutionWrapper(decoded);
    }
}
