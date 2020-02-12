package osh.datatypes.registry.oc.ipp.solutionEncoding.translators;

import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.AbstractEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.RealEncodedVariableInformation;

/**
 * @author Sebastian Kramer
 */
public class RealVariableTranslator extends AbstractVariableTranslator<double[]> {

    private static final long serialVersionUID = 1034633604660787959L;

    @Override
    public AbstractEncodedVariableInformation getVariableInformationBoolean(int variableCount) {
        double[][] boundaries = new double[variableCount][2];
        for (int i = 0; i < variableCount; i++) {
            boundaries[i][0] = 0.0;
            boundaries[i][1] = 1.0;
        }

        return new RealEncodedVariableInformation(variableCount, boundaries);
    }

    @Override
    public AbstractEncodedVariableInformation getVariableInformationLong(int variableCount, double[][] variableBoundaries) {
        return new RealEncodedVariableInformation(variableCount, variableBoundaries);
    }

    @Override
    public AbstractEncodedVariableInformation getVariableInformationDouble(int variableCount, double[][] variableBoundaries) {
        return new RealEncodedVariableInformation(variableCount, variableBoundaries);
    }

    @Override
    public DecodedSolutionWrapper decodeBoolean(double[] encodedVariable, int variableCount) {
        boolean[] decoded = new boolean[variableCount];

        for (int i = 0; i < variableCount; i++) {
            decoded[i] = encodedVariable[i] < 0.5;
        }

        return new DecodedSolutionWrapper(decoded);
    }

    @Override
    public DecodedSolutionWrapper decodeLong(double[] encodedVariable, int variableCount, double[][] variableBoundaries) {
        long[] decoded = new long[variableCount];

        for (int i = 0; i < variableCount; i++) {

            long partial = Math.round(encodedVariable[i]);

            if (partial > variableBoundaries[i][1]) {
                partial = Math.floorMod(partial, (long) variableBoundaries[i][1]);
            } else if (partial < variableBoundaries[i][0]) {
                partial = Math.floorMod(partial, (long) variableBoundaries[i][0]);
            }

            decoded[i] = partial;
        }

        return new DecodedSolutionWrapper(decoded);
    }

    @Override
    public DecodedSolutionWrapper decodeDouble(double[] encodedVariable, int variableCount, double[][] variableBoundaries) {
        double[] decoded = new double[variableCount];

        for (int i = 0; i < variableCount; i++) {

            double partial = encodedVariable[i];

            if (partial > variableBoundaries[i][1]) {
                partial = (int) flooredModulo(partial, variableBoundaries[i][1]);
            } else if (partial < variableBoundaries[i][0]) {
                partial = (int) flooredModulo(partial, variableBoundaries[i][0]);
            }

            decoded[i] = partial;
        }

        return new DecodedSolutionWrapper(decoded);
    }

    /**
     * Returns the floor modulus, like {@link Math#floorMod}.
     * @param a dividend
     * @param n the divisor
     * @return the floor modulus
     */
    private static double flooredModulo(double a, double n) {
        return n < 0 ? -mod(-a, -n) : mod(a, n);
    }

    /**
     * Returns the modulus.
     * @param a dividend
     * @param n the divisor
     * @return the modulus
     */
    private static double mod(double a, double n) {
        return a < 0 ? (a % n + n) % n : a % n;
    }
}
