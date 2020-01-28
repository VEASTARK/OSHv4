package osh.datatypes.registry.oc.ipp.solutionEncoding.translators;

import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.AbstractEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.BinaryEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.utils.BitSetConverter;

import java.util.BitSet;

/**
 * Represents a generic two-way translator for en- and decoding binary variables needed for the optimization loop.
 *
 *
 *
 * @author Sebastian Kramer
 */
public class BinaryVariableTranslator extends AbstractVariableTranslator<BitSet> {

    private static final long serialVersionUID = -1770521754246159115L;

    private static final int SIGNIFICANT_PLACES = 6;
    private static final double SIGNIFICANT_PLACES_MULTIPLIER = Math.pow(10.0, SIGNIFICANT_PLACES);

    @Override
    public AbstractEncodedVariableInformation getVariableInformationBoolean(int variableCount) {
        return new BinaryEncodedVariableInformation(variableCount);
    }

    @Override
    public AbstractEncodedVariableInformation getVariableInformationLong(int variableCount, double[][] variableBoundaries) {
        int bitCount = 0;

        for (int i = 0; i < variableCount; i++) {
            bitCount += this.bitsUsedForVariable(variableBoundaries[i]);
        }

        return new BinaryEncodedVariableInformation(bitCount);
    }

    @Override
    public AbstractEncodedVariableInformation getVariableInformationDouble(int variableCount, double[][] variableBoundaries) {
        int bitCount = 0;

        for (int i = 0; i < variableCount; i++) {
            double[] correctedBoundaries = {variableBoundaries[i][0] * SIGNIFICANT_PLACES_MULTIPLIER, variableBoundaries[i][1] * SIGNIFICANT_PLACES_MULTIPLIER};
            bitCount += this.bitsUsedForVariable(correctedBoundaries);
        }

        return new BinaryEncodedVariableInformation(bitCount);
    }

    @Override
    public DecodedSolutionWrapper decodeBoolean(BitSet encodedVariable, int variableCount) {
        boolean[] decoded = new boolean[variableCount];

        for (int i = 0; i < variableCount; i++) {
            decoded[i] = encodedVariable.get(i);
        }

        return new DecodedSolutionWrapper(decoded);
    }

    @Override
    public DecodedSolutionWrapper decodeLong(BitSet encodedVariable, int variableCount, double[][] variableBoundaries) {
        long[] decoded = new long[variableCount];
        int currentBitPosition = 0;

        for (int i = 0; i < variableCount; i++) {
            int bitsNeeded = this.bitsUsedForVariable(variableBoundaries[i]);
            if (bitsNeeded != 0) {
                BitSet partial = encodedVariable.get(currentBitPosition, currentBitPosition + bitsNeeded);
                decoded[i] = this.bitSetToLong(partial, bitsNeeded, variableBoundaries[i]);
            }
            currentBitPosition += bitsNeeded;
        }

        return new DecodedSolutionWrapper(decoded);
    }

    @Override
    public DecodedSolutionWrapper decodeDouble(BitSet encodedVariable, int variableCount, double[][] variableBoundaries) {
        double[] decoded = new double[variableCount];
        int currentBitPosition = 0;

        for (int i = 0; i < variableCount; i++) {
            double[] correctedBoundaries = {variableBoundaries[i][0] * SIGNIFICANT_PLACES_MULTIPLIER, variableBoundaries[i][1] * SIGNIFICANT_PLACES_MULTIPLIER};
            int bitsNeeded = this.bitsUsedForVariable(correctedBoundaries);
            if (bitsNeeded != 0) {
                BitSet partial = encodedVariable.get(currentBitPosition, currentBitPosition + bitsNeeded);
                long decodedLong = this.bitSetToLong(partial, bitsNeeded, variableBoundaries[i]);
                decoded[i] = decodedLong / SIGNIFICANT_PLACES_MULTIPLIER;

                //just to ensure that the resulting variable conforms to the boundaries
                if (decoded[i] < variableBoundaries[i][0]) {
                    decoded[i] = variableBoundaries[i][0];
                } else if (decoded[i] > variableBoundaries[i][1]) {
                    decoded[i] = variableBoundaries[i][1];
                }
            }
            currentBitPosition += bitsNeeded;
        }

        return new DecodedSolutionWrapper(decoded);
    }

    /**
     * Decodes the given bit-set encoded by this translator with gray-encoding and with the given variable boundaries
     * to a long variable and returns it.
     *
     * @param bitSet the encoded bit-set
     * @param bitCount the number of bits contained in the bit-set
     * @param variableBoundaries the min/max boundaries of the encoded variables
     * @return the bit-set decoded to a long variable
     */
    protected long bitSetToLong(BitSet bitSet, int bitCount, double[] variableBoundaries) {

        long partialNumber = BitSetConverter.gray2long(bitSet);

        //the number can occupy a bigger number space (0 to 2^log_2(upperbound-lowerbound)) then
        // upperbound-lowerbound so we adjust it to the percentage reached of the possible number space
        partialNumber = Math.round((partialNumber / Math.pow(2, bitCount)) * (variableBoundaries[1] - variableBoundaries[0]));

        //adding result to the lower boundary
        partialNumber += variableBoundaries[0];

        return partialNumber;
    }

    /**
     * Calculates and returns how many bits would be needed to encode a long variable with the given min/max boundaries.
     *
     * @param variableBoundaries the given min/max boundaries of the long variable
     * @return how many bits would be needed to encode a long variable with the given min/max boundaries
     */
    private int bitsUsedForVariable(double[] variableBoundaries) {
        if (variableBoundaries[1] == variableBoundaries[0]) {
            return 0;
        }
        //workaround if (upperbound - lowerbound) = 1, because log_2 would be 0
        else if (variableBoundaries[1] - variableBoundaries[0] == 1) {
            return 1;
        } else {
            return (int) Math.ceil(Math.log(variableBoundaries[1] - variableBoundaries[0]) / Math.log(2));
        }
    }
}
