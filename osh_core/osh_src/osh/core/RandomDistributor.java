package osh.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.OSH;
import osh.OSHComponent;
import osh.core.interfaces.IOSH;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

/**
 * Distributes random generators in a deterministic and independent inside the OSH. Once enabled will ensure that the same component gets
 * the exact same random generator independent of each other component in the simulation.
 *
 * Will break backwards compatibility so operates in a legacy mode for now.
 *
 * @author Sebastian Kramer
 */
public class RandomDistributor extends AbstractRandomDistributor<UUID, Class<?>> {


    /**
     * Creates this distributor with the given simulation entitiy and a master seed.
     *
     * @param entity     the simulation entity
     * @param masterSeed the master seed
     */
    public RandomDistributor(final IOSH entity, final long masterSeed) {
        super(entity, masterSeed);
    }

    @Override
    public long getSeedFromIdentifiers(final long masterSeed, final UUID firstIdentifier, final Class<?> secondIdentifier) {
        //we ignore the second identifier as the seed should only depend on the UUID (to prevent major changes from
        // renaming classes or introduction of sub-/superclasses). It is only needed to separate multiple classes with
        // the same uuid from getting the same random generator and so influencing each other.
        long uuidUpper = firstIdentifier.getMostSignificantBits();
        long uuidLower = firstIdentifier.getLeastSignificantBits();

        return masterSeed + (uuidUpper >> 32) + (uuidLower << 32);
    }
}
