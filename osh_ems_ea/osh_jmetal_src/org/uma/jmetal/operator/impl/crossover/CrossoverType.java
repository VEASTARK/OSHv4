package org.uma.jmetal.operator.impl.crossover;

/**
 * Represents the different types of crossover operators rules.
 *
 * @author Sebastian Kramer
 */
public enum CrossoverType {

    BINARY_N_POINT("BinaryNPointCrossover"),
    BINARY_N_POINT_LEGACY("SingleBinaryNPointsCrossover"),
    BLX_ALPHA("BLXAlphaCrossover"),
    DE("DifferentialEvolutionCrossover"),
    HUX("HUXCrossover"),
    INTEGER_SBX("IntegerSBXCrossover"),
    SBX("SBXCrossover"),
    NULL("NullCrossover"),
    PMX("PMXCrossover"),
    BINARY_SHUFFLE("ShuffledBinaryCrossover"),
    ONE_POINT("SinglePointCrossover"),
    TWO_POINT("DifferentialEvolutionCrossover"),
    BINARY_UNIFORM("UniformBinaryCrossover");

    private final String name;

    /**
     * Construct the crossover type with the given name.
     *
     * @param name the name of the crossover type
     */
    CrossoverType(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the crossover type.
     * @return the name of the crossover type
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
    public static CrossoverType fromName(String name) {
        for (CrossoverType e : CrossoverType.values()) {
            if (e.name.equalsIgnoreCase(name)) return e;
        }

        throw new IllegalArgumentException(name);
    }
}
