package org.uma.jmetal.operator.impl.mutation;

/**
 * Represents the different types of mutation operators.
 *
 * @author Sebastian Kramer
 */
public enum MutationType {

    //TODO: change the legacy names for the "auto-prob" operators as soon as the next backwards-compatibility
    // breaking update is released
    BIT_FLIP_AUTO("BitFlipAutoProbMutation"),
    BIT_FLIP("BitFlipMutation"),
    BIT_FLIP_APPROX("ApproximateBitFlipMutation"),
    BIT_FLIP_BLOCK("BlockBitFlipMutation"),
    INTEGER_POLYNOMIAL("IntegerPolynomialMutation"),
    POLYNOMIAL("PolynomialMutation"),
    POLYNOMIAL_AUTO("PolynomialAutoProbMutation"),
    POLYNOMIAL_APPROX("ApproximatePolynomialMutation"),
    NON_UNIFORM("NonUniformMutation"),
    NULL("NullMutation"),
    PERMUTATION_SWAP("PermutationSwapMutation"),
    SIMPLE_RANDOM("SimpleRandomMutation"),
    UNIFORM("UniformMutation");

    private final String name;

    /**
     * Construct the mutation type with the given name.
     *
     * @param name the name of the mutation type
     */
    MutationType(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the mutation type.
     * @return the name of the mutation type
     */
    public String getName() {
        return this.name;
    }

    /**
     * Finds and returns the enum value corresponding to the given name.
     *
     * @param name the name of the enum value to find
     * @return the enum value corresponding to the name
     */
    public static MutationType fromName(String name) {
        for (MutationType e : MutationType.values()) {
            if (e.name.equalsIgnoreCase(name)) return e;
        }

        throw new IllegalArgumentException(name);
    }
}
