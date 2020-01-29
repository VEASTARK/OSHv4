package osh.datatypes.registry.oc.ipp.solutionEncoding.translators;

import osh.utils.BitSetConverter;

import java.util.BitSet;

/**
 * Represents a specified two-way translator for en- and decoding binary variables that will not shrink decoded
 * variables so they lie in [minBoundary, maxBoundary] but instead will leave that calculation to the underlying
 * problem-part
 *
 * @author Sebastian Kramer
 */
public class BinaryFullRangeVariableTranslator extends BinaryVariableTranslator {


    private static final long serialVersionUID = -7249680293848640237L;

    /**
     * Works similar to {@link BinaryVariableTranslator#bitSetToLong} but will not ensure that the resulting
     * variables lies in [minBoundary, maxBoundary]. This will have to be handled in the underlying problem-part.
     *
     * @param bitSet             the encoded bit-set
     * @param bitCount           the number of bits contained in the bit-set
     * @param variableBoundaries the min/max boundaries of the encoded variables
     * @return
     */
    @Override
    protected long bitSetToLong(BitSet bitSet, int bitCount, double[] variableBoundaries) {
        long partialNumber = BitSetConverter.gray2long(bitSet);

        //adding result to the lower boundary
        partialNumber += variableBoundaries[0];

        return partialNumber;
    }

    /**
     * Calculates and returns how many bits would be needed to encode a long variable with the given min/max boundaries.
     * Will use a slightly larger variable window than {@link BinaryVariableTranslator#bitsUsedForVariable} to keep
     * compatibility with older OSH versions.
     * </br>
     * Will be removed to keep consistency with the next update that breaks backwards kompatibility.
     *
     * @param variableBoundaries the given min/max boundaries of the long variable
     * @return how many bits would be needed to encode a long variable with the given min/max boundaries
     */
    @Override
    protected int bitsUsedForVariable(double[] variableBoundaries) {
        //TODO: remove as soon as backwards compatibility is broken by another update
        if (variableBoundaries[1] == variableBoundaries[0]) {
            return 0;
        }
        //workaround if (upperbound - lowerbound) = 1, because log_2 would be 0
        else if (variableBoundaries[1] - variableBoundaries[0] == 1) {
            return 1;
        } else {
            return (int) Math.ceil(Math.log(variableBoundaries[1] - variableBoundaries[0] + 1) / Math.log(2));
        }
    }
}
