package osh.datatypes.registry.oc.ipp.solutionEncoding.translators;

import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.AbstractEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a two-way translator for en- and decoding variables needed for the optimization loops.
 *
 * @author Sebastian Kramer
 *
 * @param <T> the object-type of variables to be decoded
 */
public abstract class AbstractVariableTranslator<T> implements Serializable {

    private static final long serialVersionUID = -3947281696273650875L;

    /**
     * Returns information about how to represent an encoding conforming to the given type of variables
     * requested, the given numbers of variables needed and their given min/max boundaries.
     *
     * @param variableType the type of variables to encode
     * @param variableCount the number of variables to encode
     * @param variableBoundaries the min/max boundaries of each variable to encode
     *
     * @return information about how to represent an encoding conforming to the given requirements
     */
    public AbstractEncodedVariableInformation getVariableInformation(VariableType variableType,
                                                                     int variableCount,
                                                                     double[][] variableBoundaries) {
        Objects.requireNonNull(variableType);
        Objects.requireNonNull(variableBoundaries);
        assert (variableType == VariableType.BOOLEAN || variableBoundaries.length == variableCount);

        switch (variableType) {
            case BOOLEAN: return this.getVariableInformationBoolean(variableCount);
            case LONG: return this.getVariableInformationLong(variableCount, variableBoundaries);
            default: return this.getVariableInformationDouble(variableCount, variableBoundaries);
        }
    }

    /**
     * Returns information about the encoding representing boolean variables with the given numbers of variables
     * requested.
     *
     * @param variableCount the number of variables to encode
     *
     * @return information about the encoding representing the given number of boolean variables
     */
    public abstract AbstractEncodedVariableInformation getVariableInformationBoolean(int variableCount);

    /**
     * Returns information about the encoding representing long variables with the given numbers of variables
     * reuqested and the given min/max boundaries of each variable.
     *
     * @param variableCount the number of variables to encode
     * @param variableBoundaries the min/max boundaries of each variable to encode
     *
     * @return information about the encoding conforming to the given requirements
     */
    public abstract AbstractEncodedVariableInformation getVariableInformationLong(int variableCount, double[][] variableBoundaries);

    /**
     * Returns information about the encoding representing double variables with the given numbers of variables
     * reuqested and the given min/max boundaries of each variable.
     *
     * @param variableCount the number of variables to encode
     * @param variableBoundaries the min/max boundaries of each variable to encode
     *
     * @return information about the encoding conforming to the given requirements
     */
    public abstract AbstractEncodedVariableInformation getVariableInformationDouble(int variableCount, double[][] variableBoundaries);

    /**
     * Decodes the given encoding containting the given variable types, the given number of variables and the
     * given min/max boundaries of each contained variable.
     *
     * @param encodedVariable the encoding
     * @param variableType the type of variables encoded
     * @param variableCount the number of variables encoded
     * @param variableBoundaries the min/max boundaries of each encoded variable
     *
     * @return a wrapper around the decoded array of variables
     */
    public DecodedSolutionWrapper decode(T encodedVariable, VariableType variableType, int variableCount,
                                         double[][] variableBoundaries) {
        Objects.requireNonNull(encodedVariable);
        Objects.requireNonNull(variableType);
        Objects.requireNonNull(variableBoundaries);
        assert (variableType == VariableType.BOOLEAN || variableBoundaries.length == variableCount);

        switch (variableType) {
            case BOOLEAN: return this.decodeBoolean(encodedVariable, variableCount);
            case LONG: return this.decodeLong(encodedVariable, variableCount, variableBoundaries);
            default: return this.decodeDouble(encodedVariable, variableCount, variableBoundaries);
        }
    }

    /**
     * Decodes the given encoding containting the given number of boolean variables.
     *
     * @param encodedVariable the encoding
     * @param variableCount the number of variables encoded
     *
     * @return a wrapper around the decoded array of booleans
     */
    public abstract DecodedSolutionWrapper decodeBoolean(T encodedVariable, int variableCount);

    /**
     * Decodes the given encoding containting the given number of long variables and the
     * given min/max boundaries of each contained variable.
     *
     * @param encodedVariable the encoding
     * @param variableCount the number of variables encoded
     * @param variableBoundaries the min/max boundaries of each encoded variable
     *
     * @return a wrapper around the decoded array of longs
     */
    public abstract DecodedSolutionWrapper decodeLong(T encodedVariable, int variableCount, double[][] variableBoundaries);

    /**
     * Decodes the given encoding containting the given number of double variables and the
     * given min/max boundaries of each contained variable.
     *
     * @param encodedVariable the encoding
     * @param variableCount the number of variables encoded
     * @param variableBoundaries the min/max boundaries of each encoded variable
     *
     * @return a wrapper around the decoded array of doubles
     */
    public abstract DecodedSolutionWrapper decodeDouble(T encodedVariable, int variableCount, double[][] variableBoundaries);
}
