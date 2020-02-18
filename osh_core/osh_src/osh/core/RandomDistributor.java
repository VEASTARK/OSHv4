package osh.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.OSH;
import osh.OSHComponent;

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
public class RandomDistributor extends OSHComponent {

    private final long initialRandomSeed;

    private final static boolean IS_LEGACY_MODE = true;
    private final OSHRandom legacyRandom;

    private ZonedDateTime startTime;

    private final HashSet<UUID> allIdentifier = new HashSet<>();
    private final Object2ObjectOpenHashMap<UUIDClass, RandomWrapper> uuidClassToRandomGenMap = new Object2ObjectOpenHashMap<>();

    /**
     * Creates this distributor with the given simulation entitiy and a master seed.
     *
     * @param entity the simulation entity
     * @param masterSeed the master seed
     */
    public RandomDistributor(OSH entity, long masterSeed) {
        super(entity);

        if (IS_LEGACY_MODE) {
            this.initialRandomSeed = -1L;
            this.legacyRandom = new OSHRandom(masterSeed);
        } else {
            this.initialRandomSeed = new OSHRandom(masterSeed).getNextLong();
            this.legacyRandom = null;
        }
    }

    /**
     * Returns a unique random generator based on the unique identifier and the class of the caller.
     *
     * Will ensure that every random generator is unique at any time t by iterating over a master random generator exactly n times, where
     * n is either seconds passed since the last request of the same caller for a random generator or the seconds passed since creation
     * of this class.
     *
     * @param identifier the unique identifier
     * @param className the class of the caller
     *
     * @return a unique random generator based on the unique identifier and the class of the caller
     */
    public OSHRandom getRandomGenerator(UUID identifier, Class<?> className) {
        return this.getRandomGenerator(identifier, className, false);
    }


    /**
     * Returns a unique random generator based on the unique identifier and the class of the caller.
     *
     * Will ensure that every random generator is unique at any time t by iterating over a master random generator exactly n times, where
     * n is either seconds passed since the last request of the same caller for a random generator or the seconds passed since creation
     * of this class.
     *
     * @param identifier the unique identifier
     * @param className the class of the caller
     * @param getBaseRandom flag if a new random generator should be created in legacy mode
     *
     * @return a unique random generator based on the unique identifier and the class of the caller
     */
    public OSHRandom getRandomGenerator(UUID identifier, Class<?> className, boolean getBaseRandom) {

        if (IS_LEGACY_MODE) {
            if (!getBaseRandom) {
                return new OSHRandom(this.legacyRandom.getNextLong());
            } else {
                return this.legacyRandom;
            }
        }

        RandomWrapper random;
        ZonedDateTime currentTime = this.getTimeDriver().getCurrentTime();

        if (this.allIdentifier.contains(identifier)) {
            UUIDClass dummy = new UUIDClass(identifier, className.getName());

            if (!this.uuidClassToRandomGenMap.containsKey(dummy)) {
                this.getGlobalLogger().logError("Two components with same UUID but different classes demand a random " +
                        "generator, unspecified behaviour .... ");

                long uuidUpper = identifier.getMostSignificantBits();
                long uuidLower = identifier.getLeastSignificantBits();

                long constructedSeed = this.initialRandomSeed + (uuidUpper >> 32) + (uuidLower << 32);

                random = new RandomWrapper(new Random(constructedSeed).nextLong(), currentTime);

                this.uuidClassToRandomGenMap.put(dummy, random);
            } else {
                random = this.uuidClassToRandomGenMap.get(dummy);
            }

        } else {
            //create a new wrapper
            long uuidUpper = identifier.getMostSignificantBits();
            long uuidLower = identifier.getLeastSignificantBits();

            long constructedSeed = this.initialRandomSeed + (uuidUpper >> 32) + (uuidLower << 32);

            random = new RandomWrapper(constructedSeed, this.startTime);

            this.uuidClassToRandomGenMap.put(new UUIDClass(identifier, className.getName()), random);
            this.allIdentifier.add(identifier);
        }

        if (!random.lastCalled.isEqual(currentTime)) {
            //starting at 1 as we already need to call nextLong() one time to generate the next seed
            for (long i = 1; i < Duration.between(random.lastCalled, currentTime).toSeconds(); i++) {
                random.baseRandom.getNextLong();
            }
            random.currentRandom = new OSHRandom(random.baseRandom.getNextLong());
            random.lastCalled = currentTime;
        } else {
            //multiple calls from same component, better be sure and generate a new random gen
            random.currentRandom = new OSHRandom(random.baseRandom.getNextLong());
        }

        return random.currentRandom;
    }

    /**
     * Signals that the OSH has started and we can draw the start-time from the time driver.
     */
    public void startClock() {
        this.startTime = this.getTimeDriver().getTimeAtStart();
    }

    /**
     * Container class for the tuple of class-name and unique id. Used as key for the mapping of random-wrapper to caller.
     */
    private static final class UUIDClass {

        private final String className;
        private final UUID identifier;

        private UUIDClass(UUID identifier, String className) {
            this.identifier = identifier;
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            UUIDClass uuidClass = (UUIDClass) o;

            if (!this.className.equals(uuidClass.className)) return false;
            return this.identifier.equals(uuidClass.identifier);
        }

        @Override
        public int hashCode() {
            int result = this.className.hashCode();
            result = 31 * result + this.identifier.hashCode();
            return result;
        }

        private UUID getIdentifier() {
            return this.identifier;
        }

        private String getClassName() {
            return this.className;
        }
    }

    /**
     * Unique wrapper for each caller's random generator
     */
    private static final class RandomWrapper {

        private ZonedDateTime lastCalled;
        private final OSHRandom baseRandom;
        private OSHRandom currentRandom;

        private RandomWrapper(long baseRandomSeed, ZonedDateTime lastCalled) {
            this.lastCalled = lastCalled;

            this.baseRandom = new OSHRandom(baseRandomSeed);
            this.currentRandom = new OSHRandom(this.baseRandom.getNextLong());
        }
    }
}
