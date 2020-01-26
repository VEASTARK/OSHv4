package osh.datatypes.registry.oc.ipp.solutionEncoding.variables;

/**
 * Represents information about the binary encoded variables needed for the optimization loop.
 *
 * @author Sebastian Kramer
 */
public class BinaryEncodedVariableInformation  extends AbstractEncodedVariableInformation {


    private static final long serialVersionUID = -3308540972492499143L;

    private final int bitCount;

    /**
     * No-arg constructor for serialization.
     */
    protected BinaryEncodedVariableInformation() {
        this.bitCount = 0;
    }

    /**
     * Creates this variable information with the given bit-count needed.
     *
     * @param bitCount the bit-count needed for the underlying problem
     */
    public BinaryEncodedVariableInformation(int bitCount) {
        this.bitCount = bitCount;
    }

    /**
     * Returns the bit-count needed for the optimization loop.
     * @return the bit-count needed for the optimization loop
     */
    public int getBitCount() {
        return this.bitCount;
    }

    @Override
    public boolean needsNoVariables() {
        return this.bitCount == 0;
    }

}
