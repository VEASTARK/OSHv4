package osh.datatypes.registry.oc.ipp.solutionEncoding.translators;

import osh.configuration.oc.VariableEncoding;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.*;

import java.util.BitSet;
import java.util.Objects;

/**
 * Collects information about the encoding of the required variables of the underlying problem and provides
 * functionality to decode solutions back to native variables.
 *
 * @author Sebastian Kramer
 */
public class IPPSolutionHandler {

    private final AbstractVariableTranslator<BitSet> binaryTranslator;
    private final AbstractVariableTranslator<double[]> realTranslator;

    private VariableType variableType;
    private int variableCount;
    private double[][] variableBoundaries;

    private static final AbstractEncodedVariableInformation EMPTY_BINARY_VARIABLE = new BinaryEncodedVariableInformation(0);
    private static final AbstractEncodedVariableInformation EMPTY_REAL_VARIABLE = new RealEncodedVariableInformation(0,
            new double[][]{});

    /**
     * Constructs this solution handler with the given translators for binary and real encoding.
     *
     * @param binaryTranslator translator for binary encoding
     * @param realTranslator translator for real encoding
     */
    public IPPSolutionHandler(AbstractVariableTranslator<BitSet> binaryTranslator, AbstractVariableTranslator<double[]> realTranslator) {
        this.binaryTranslator = binaryTranslator;
        this.realTranslator = realTranslator;
    }

    /**
     * Updates the variable information for this class to expose and handle the decoding for.
     *
     * @param variableType the type of variables required
     * @param variableCount the number of variables required
     * @param variableBoundaries the min/max boundaries of each variable required
     */
    public void updateVariableInformation(VariableType variableType, int variableCount, double[][] variableBoundaries) {
        Objects.requireNonNull(variableType);
        Objects.requireNonNull(variableBoundaries);
        //sanity
        assert (variableType == VariableType.BOOLEAN || variableBoundaries.length == variableCount);

        this.variableType = variableType;
        this.variableCount = variableCount;
        this.variableBoundaries = variableBoundaries;
    }

    /**
     * Calculates the encoding requirements for the needed variables this objects knows about according to the given
     * variable encoding type.
     *
     * @param variableEncoding the variable encoding type
     * @return the encoding requirements for the needed variables
     */
    public AbstractEncodedVariableInformation getVariableInformation(VariableEncoding variableEncoding) {
        //translators will be null for non-controllable ipps
        if (variableEncoding == VariableEncoding.BINARY) {
            return this.binaryTranslator != null ? this.binaryTranslator.getVariableInformation(this.variableType,
                    this.variableCount, this.variableBoundaries) : EMPTY_BINARY_VARIABLE;
        } else {
            return this.realTranslator != null ? this.realTranslator.getVariableInformation(this.variableType,
                    this.variableCount, this.variableBoundaries) : EMPTY_REAL_VARIABLE;
        }
    }

    /**
     * Decode the given binary encoded solution and return it's representation as variables.
     *
     * @param encodedSolution the binary encoded solution
     * @return the decoded solution inside a solution wrapper
     */
    public DecodedSolutionWrapper decode(BitSet encodedSolution) {
        if (this.binaryTranslator != null) {
            return this.binaryTranslator.decode(encodedSolution, this.variableType, this.variableCount,
                    this.variableBoundaries);
        }
        return null;
    }

    /**
     * Decode the given real encoded solution and return it's representation as variables.
     *
     * @param encodedSolution the real encoded solution
     * @return the decoded solution inside a solution wrapper
     */
    public DecodedSolutionWrapper decode(double[] encodedSolution) {
        if (this.realTranslator != null) {
            return this.realTranslator.decode(encodedSolution, this.variableType, this.variableCount,
                    this.variableBoundaries);
        }
        return null;
    }
}
