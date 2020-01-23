package osh.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.OSHComponent;
import osh.core.interfaces.IOSH;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.registry.interfaces.ITimeRegistryListener;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Registry for communication between the time-driver and any modules depending on
 * This registry works with the publish/subscribe architecture and enables subscription either for specific senders
 * (identified by their supplied UUID) of for any published exchanges of a sepcific type.
 *
 * @author Sebastian Kramer
 *
 */
public class TimeRegistry extends OSHComponent {

    /**
     * A mapping of each time event to it's listeners.
     */
    private final Map<TimeSubscribeEnum, Set<ITimeRegistryListener>> subscribers =
            new EnumMap<>(TimeSubscribeEnum.class);

    /**
     * A mapping of unions of time event's to a union of all listeners of each single event.
     */
    private final Map<EnumSet<TimeSubscribeEnum>, Set<ITimeRegistryListener>> subscriberUnion =
            new Object2ObjectOpenHashMap<>();

    private final ReentrantLock subscriberLock = new ReentrantLock();

    /**
     * Flag of the calls to each subscriber should be executed concurrently or not.
     */
    private final boolean executeConcurrently;

    /**
     * Generates a time-registry with the given organic management entity and the flag if this registry supports a
     * simulation or real-world use
     *
     * @param entity the organic management entity
     * @param isSimulation flag if this registry is run in a simulation
     */
    public TimeRegistry(IOSH entity, boolean isSimulation) {
        super(entity);
        this.executeConcurrently = !isSimulation;
    }

    /**
     * Publishes the given time exchange to all listeners subscurbed to it's contained set of
     * {@link TimeSubscribeEnum} events. Listeners will be called concurrently or sequentially based on this registry
     * existing in the real world or in a simulation respectively.
     *
     * @param exchange the time exchange object
     */
    public void publish(final TimeExchange exchange) {
        Objects.requireNonNull(exchange);
        if (exchange.getTimeEvents().isEmpty()) throw new IllegalArgumentException();

        if (this.executeConcurrently) {
            this.getListeners(exchange.getTimeEvents()).parallelStream().forEach(t -> t.onTimeExchange(exchange));
        } else {
            this.getListeners(exchange.getTimeEvents()).forEach(t -> t.onTimeExchange(exchange));
        }
    }

    /**
     * Calculates a set of all listeners to the given set of time-events and returns it.
     *
     * @param timeEvents the given set of time events
     * @return a set of all listeners to the given time events
     */
    private Set<ITimeRegistryListener> getListeners(EnumSet<TimeSubscribeEnum> timeEvents) {
        if (this.subscriberUnion.containsKey(timeEvents)) {
            return this.subscriberUnion.get(timeEvents);
        } else {
            this.subscriberLock.lock();
            try {
                Set<ITimeRegistryListener> listeners =
                        timeEvents.stream().flatMap(t -> this.subscribers.getOrDefault(t, Collections.emptySet())
                                .stream()).collect(Collectors.toCollection(ObjectOpenHashSet::new));
                this.subscriberUnion.put(timeEvents, listeners);
                return listeners;
            } finally {
                this.subscriberLock.unlock();
            }
        }
    }

    /**
     * Subscribes for a given singular time event with the given listener.
     *
     * @param listener the given listener interface
     * @param timeEvent the given singular time event
     */
    public void subscribe(ITimeRegistryListener listener, TimeSubscribeEnum timeEvent) {
        Objects.requireNonNull(listener);
        Objects.requireNonNull(timeEvent);

        this.subscriberLock.lock();
        try {
            this.subscribers.computeIfAbsent(timeEvent, k -> new ObjectOpenHashSet<>()).add(listener);

            this.subscriberUnion.entrySet().stream().filter(e -> e.getKey().contains(timeEvent))
                    .forEach(t -> t.getValue().add(listener));
        } finally {
            this.subscriberLock.unlock();
        }
    }

    /**
     * Subscribes for a given collection of time events with the given listener.
     *
     * @param listener the given listener interface
     * @param timeEvents the collections of time events to subscribe to
     */
    public void subscribe(ITimeRegistryListener listener, TimeSubscribeEnum... timeEvents) {
        Objects.requireNonNull(listener);
        Objects.requireNonNull(timeEvents);

        this.subscriberLock.lock();
        try {
            Arrays.stream(timeEvents).forEach(t -> this.subscribers.computeIfAbsent(t, k -> new ObjectOpenHashSet<>()).add(listener));

            Arrays.stream(timeEvents).forEach(i -> this.subscriberUnion.entrySet().stream().filter(e -> e.getKey().contains(i))
                    .forEach(t -> t.getValue().add(listener)));
        } finally {
            this.subscriberLock.unlock();
        }
    }
}
