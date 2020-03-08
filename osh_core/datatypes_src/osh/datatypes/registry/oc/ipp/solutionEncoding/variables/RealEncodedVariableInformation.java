package osh.datatypes.registry.oc.ipp.solutionEncoding.variables;

import java.util.Objects;

/**
 * Represents information about the real encoded variables needed for the optimization loop.
 *
 * @author Sebastian Kramer
 */
public class RealEncodedVariableInformation extends AbstractEncodedVariableInformation {

    /**
     * The number of variables needed for the optimization loop.
     */
    private final int variableCount;

    /**
     * The min/max boundaries of every variable needed for the optimization loop.
     */
    private final double[][] variableBoundaries;

    /**
     * Creates this variable information with the given variable count and the given min/max boundaries for the
     * optimization loop.
     *
     * @param variableCount the number of variables needed
     * @param variableBoundaries the min/max boundaries of every variable needed
     */
    public RealEncodedVariableInformation(int variableCount, double[][] variableBoundaries) {
        Objects.requireNonNull(variableBoundaries);
        assert variableCount == variableBoundaries.length;

        this.variableCount = variableCount;
        this.variableBoundaries = variableBoundaries;
    }

    /**
     * Returns the number of variables needed for the optimization loop.
     * @return the number of variables needed for the optimization loop
     */
    public int getVariableCount() {
        return this.variableCount;
    }

    /**
     * Returns the min/max boundaries of all the variables needed for the optimization loop.
     * @return the min/max boundaries of all the variables needed for the optimization loop
     */
    public double[][] getVariableBoundaries() {
        return this.variableBoundaries;
    }

    @Override
    public boolean needsNoVariables() {
        return this.variableCount == 0;
    }

}
