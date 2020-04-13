package osh.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.OSHComponent;
import osh.core.interfaces.IOSH;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Distributes random generators in a deterministic and independent way inside the OSH. Once enabled will ensure that
 * the same component gets the exact same random generator independent of each other component in the simulation.
 *
 * @author Sebastian Kramer
 */
public abstract class AbstractRandomDistributor<I, J> extends OSHComponent {

    private final long initialRandomSeed;

    private ZonedDateTime startTime;

    private final Object2ObjectOpenHashMap<IdentifierTuple<I, J>, RandomWrapper> identifierTupleToRandomGenMap =
            new Object2ObjectOpenHashMap<>();

    /**
     * Creates this distributor with the given simulation entitiy and a master seed.
     *
     * @param entity the simulation entity
     * @param masterSeed the master seed
     */
    public AbstractRandomDistributor(IOSH entity, long masterSeed) {
        super(entity);

        this.initialRandomSeed = new OSHRandom(masterSeed).getNextLong();
    }

    /**
     * Returns a unique random generator based on the unique primary and secondary identifiers.
     *
     * Will ensure that every random generator is unique at any time t by iterating over a master random generator exactly n times, where
     * n is either seconds passed since the last request of the same caller for a random generator or the seconds passed since creation
     * of this class.
     *
     * @param primaryIdentifier the unique primary identifier
     * @param secondaryIdentifier the unique secondary identifier
     *
     * @return a unique random generator based on the unique identifiers
     */
    public OSHRandom getRandomGenerator(I primaryIdentifier, J secondaryIdentifier) {

        RandomWrapper random;
        ZonedDateTime currentTime = this.getTimeDriver().getCurrentTime();
        IdentifierTuple<I, J> identifierTuple = new IdentifierTuple<>(primaryIdentifier, secondaryIdentifier);

        if (this.identifierTupleToRandomGenMap.containsKey(identifierTuple)) {
            random = this.identifierTupleToRandomGenMap.get(identifierTuple);
        } else {
            //create a new wrapper
            long constructedSeed =
                    this.getSeedFromIdentifiers(this.initialRandomSeed, primaryIdentifier, secondaryIdentifier);

            random = new RandomWrapper(constructedSeed, this.startTime);

            this.identifierTupleToRandomGenMap.put(identifierTuple, random);
        }

        if (!random.lastCalled.isEqual(currentTime)) {
            //starting at 1 as we already need to call nextLong() one time to generate the next seed
            for (long i = 1; i < Duration.between(random.lastCalled, currentTime).toSeconds(); i++) {
                random.baseRandom.getNextLong();
            }
            random.currentRandomSeed = random.baseRandom.getNextLong();
            random.lastCalled = currentTime;
        } else {
            //multiple calls from same component, better be sure and generate a new random seed
            random.currentRandomSeed = this.getSeedFromIdentifiers(random.currentRandomSeed, primaryIdentifier, secondaryIdentifier);
        }

        return new OSHRandom(random.currentRandomSeed);
    }

    /**
     * Signals that the OSH has started and we can draw the start-time from the time driver.
     */
    public void startClock() {
        this.startTime = this.getTimeDriver().getTimeAtStart();
    }

    public abstract long getSeedFromIdentifiers(long masterSeed, I firstIdentifier, J secondIdentifier);

    /**
     * Container class for the tuple of both used identifiers. Used as key for the mapping of random-wrapper to caller.
     */
    private static final class IdentifierTuple<I, J> {

        private final I primaryIdentifier;
        private final J secondaryIdentifier;

        private IdentifierTuple(I primaryIdentifier, J secondaryIdentifier) {
            this.primaryIdentifier = primaryIdentifier;
            this.secondaryIdentifier = secondaryIdentifier;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            final IdentifierTuple<?, ?> that = (IdentifierTuple<?, ?>) o;
            return this.primaryIdentifier.equals(that.primaryIdentifier) &&
                    this.secondaryIdentifier.equals(that.secondaryIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.primaryIdentifier, this.secondaryIdentifier);
        }
    }

    /**
     * Unique wrapper for each caller's random generator
     */
    private static final class RandomWrapper {

        private ZonedDateTime lastCalled;
        private final OSHRandom baseRandom;
        private long currentRandomSeed;

        private RandomWrapper(long baseRandomSeed, ZonedDateTime lastCalled) {
            this.lastCalled = lastCalled;

            this.baseRandom = new OSHRandom(baseRandomSeed);
            this.currentRandomSeed = this.baseRandom.getNextLong();
        }
    }
}
