package osh.datatypes.registry.oc.ipp.solutionEncoding.translators;

import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.AbstractEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.BinaryEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;

import java.util.BitSet;

/**
 * Represents an extension of the {@link BinaryVariableTranslator} where a state-machine is used to en-/decode long
 * variables instead of gray encoding
 *
 * @author Sebastian Kramer
 */
public class BinaryBiStateVariableTranslator extends BinaryVariableTranslator {

    private static final long serialVersionUID = -5869520807916817602L;

    /**
     * how many bits are used to represent each state of the state-machine
     */
    private final int bitsPerState;

    /**
     * No-arg constructor for serialization.
     */
    protected BinaryBiStateVariableTranslator() {
        this.bitsPerState = -1;
    }

    /**
     * Creates this variable translator with the given number of bits to use to represent each state of the
     * state-machine.
     *
     * @param bitsPerState the number of bits to use to represent each state of the state-machine
     */
    public BinaryBiStateVariableTranslator(int bitsPerState) {
        this.bitsPerState = bitsPerState;
    }

    @Override
    public AbstractEncodedVariableInformation getVariableInformationLong(int variableCount, double[][] variableBoundaries) {
        return new BinaryEncodedVariableInformation(variableCount * this.bitsPerState);
    }

    @Override
    public DecodedSolutionWrapper decodeLong(BitSet encodedVariable, int variableCount, double[][] variableBoundaries) {
        long[] decoded = new long[variableCount];

        /*
            The state-machine encompasses two states: on and off. It will remain in the off-state unless all bits
            used to encode one step are set, and it will remain in the on-state unless all used bits are not set.

            The decoded solution will contain the information about which transisiton to make for each step:
                0 = move to off-state
                1 = move to on-state
                2 = remain
         */
        for (int i = 0; i < variableCount * this.bitsPerState; i += this.bitsPerState) {
            int res;

            boolean anded = true, ored = false; // and / or
            for (int j = 0; j < this.bitsPerState; j++) {
                anded &= encodedVariable.get(i + j);
                ored |= encodedVariable.get(i + j);
            }
            if (!anded && ored) { // bits are not all equal
                res = 2; // keep last state
            }
            //all 1 --> on
            else if (ored) {
                res = 1;
            }
            //all 0 --> off
            else {
                res = 0;
            }

            decoded[i / this.bitsPerState] = res;
        }

        return new DecodedSolutionWrapper(decoded);
    }
}
