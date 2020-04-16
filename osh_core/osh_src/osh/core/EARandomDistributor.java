package osh.core;

import osh.configuration.oc.AlgorithmType;
import osh.core.interfaces.IOSH;

/**
 * Distributes random generators in a deterministic and independent inside the OSH. Once enabled will ensure that the same component gets
 * the exact same random generator independent of each other component in the simulation.
 *
 * Will break backwards compatibility so operates in a legacy mode for now.
 *
 * @author Sebastian Kramer
 */
public class EARandomDistributor extends AbstractRandomDistributor<AlgorithmType, Integer> {


    /**
     * Creates this distributor with the given simulation entitiy and a master seed.
     *
     * @param entity     the simulation entity
     * @param masterSeed the master seed
     */
    public EARandomDistributor(final IOSH entity, final long masterSeed) {
        super(entity, masterSeed);
    }

    @Override
    public long getSeedFromIdentifiers(final long masterSeed, final AlgorithmType firstIdentifier, final Integer secondIdentifier) {
        //we ignore the second identifier as this is only the index of the algorithm in the configuration file and
        // should not influence the stream of random numbers. It is only needed to separate multiple algorithms of
        // the same type from getting the same random generator and so influencing each other.
        byte[] nameBytes = firstIdentifier.toString().getBytes();
        long constructedSeed = masterSeed;
        for (int i = 0; i < nameBytes.length; i++) {
            constructedSeed += i % 2 == 0 ? ((long) nameBytes[i]) >> 32 : ((long) nameBytes[i]) << 32;
        }

        return constructedSeed;
    }
}
