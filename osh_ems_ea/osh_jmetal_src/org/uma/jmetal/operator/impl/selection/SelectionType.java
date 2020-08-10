package org.uma.jmetal.operator.impl.selection;

/**
 * Represents the different types of selection operators.
 *
 * @author Sebastian Kramer
 */
public enum SelectionType {

    BEST("BestSolutionSelection"),
    DE("DifferentialEvolutionSelection"),
    BINARY_TOURNAMENT("BinaryTournamentSelection"),
    TOURNAMENT("TournamentSelection"),
    NARY_TOURNAMENT("NaryTournamentSelection"),
    NARY_RANDOM("NaryRandomSelection"),
    RANDOM("RandomSelection"),
    RANKING_CROWDING("RankingAndCrowdingSelection"),
    RANKING_DIR_SCORE("RankingAndDirScoreSelection"),
    RANKING_PREFERENCE("RankingAndPreferenceSelection"),
    ROULETTE_WHEEL("RouletteWheelSelection"),
    SPATIAL_SPREAD("SpatialSpreadDeviationSelection"),
    SUS("StochasticUniversalSampling"),
    STOCHASTIC_ACCEPTANCE("StochasticAcceptanceSelection");

    private final String name;

    /**
     * Construct the selection type with the given name.
     *
     * @param name the name of the selection type
     */
    SelectionType(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the selection type.
     * @return the name of the selection type
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
    public static SelectionType fromName(String name) {
        for (SelectionType e : SelectionType.values()) {
            if (e.name.equalsIgnoreCase(name)) return e;
        }

        throw new IllegalArgumentException(name);
    }
}
