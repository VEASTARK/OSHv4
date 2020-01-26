package osh.datatypes.registry.oc.ipp.solutionEncoding.variables;

import java.io.Serializable;
import java.util.Objects;

/**
 * Provides a wrapper for a decoded solution for access the partial-problems.
 *
 * @author Sebastian Kramer
 */
public class DecodedSolutionWrapper implements Serializable {

    private static final long serialVersionUID = 3983491376087156143L;

    private final boolean[] booleanArray;
    private final long[] longArray;
    private final double[] doubleArray;

    /**
     * No-arg constructor for serialization.
     */
    protected DecodedSolutionWrapper() {
        this.booleanArray = null;
        this.longArray = null;
        this.doubleArray = null;
    }

    /**
     * Creates a wrapper around the given boolean array of variables.
     *
     * @param booleanArray the boolean array of variables
     */
    public DecodedSolutionWrapper(boolean[] booleanArray) {
        Objects.requireNonNull(booleanArray);
        this.booleanArray = booleanArray;
        this.longArray = null;
        this.doubleArray = null;
    }

    /**
     * Creates a wrapper around the given long array of variables.
     *
     * @param longArray the boolean long of variables
     */
    public DecodedSolutionWrapper(long[] longArray) {
        Objects.requireNonNull(longArray);
        this.booleanArray = null;
        this.longArray = longArray;
        this.doubleArray = null;
    }

    /**
     * Creates a wrapper around the given double array of variables.
     *
     * @param doubleArray the double array of variables
     */
    public DecodedSolutionWrapper(double[] doubleArray) {
        Objects.requireNonNull(doubleArray);
        this.booleanArray = null;
        this.longArray = null;
        this.doubleArray = doubleArray;
    }

    /**
     * Returns the boolean array this wapper is constructed around.
     *
     * @return the underlying boolean array
     */
    public boolean[] getBooleanArray() {
        return this.booleanArray;
    }

    /**
     * Returns the long array this wapper is constructed around.
     *
     * @return the underlying long array
     */
    public long[] getLongArray() {
        return this.longArray;
    }

    /**
     * Returns the double array this wapper is constructed around.
     *
     * @return the underlying double array
     */
    public double[] getDoubleArray() {
        return this.doubleArray;
    }
}
