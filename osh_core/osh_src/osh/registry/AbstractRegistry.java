package osh.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import osh.OSHComponent;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.registry.interfaces.IHasState;
import osh.registry.interfaces.IPromiseToBeImmutable;
import osh.utils.Triple;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Generic registry for communication between different modules of the OSH.
 * This registry works with the publish/subscribe architecture and enables subscription either for specific senders
 * (identified by their supplied UUID) of for any published exchanges of a sepcific type.
 *
 * @author Sebastian Kramer
 *
 * @param <I> the identifier of the objects to be exchanged in this registry
 * @param <L> the listener interface all subscriber need to implement to use this registry
 */
public abstract class AbstractRegistry<I, L> extends OSHComponent {

    /**
     * A map containing all data currently known to this registry.
     */
    private final Map<I, Map<UUID, AbstractExchange>> data = new Object2ObjectOpenHashMap<>();

    /**
     * The optional queue of exchange objects to publish to subscribers if this step is to only be completed
     * by a specific call and not directly after each publish call
     */
    private final ConcurrentLinkedQueue<Triple<I, UUID, AbstractExchange>> deferQueue = new  ConcurrentLinkedQueue<>();

    /**
     * A mapping of each listener to it's wrapper object
     */
    private final Map<L, AbstractListenerWrapper<L>> wrapperMap = new Object2ObjectOpenHashMap<>();

    /**
     * A map of each object identifier and publisher identity to the subscribers of this specific identifier and
     * publisher
     */
    private final Map<I, Map<UUID, List<AbstractListenerWrapper<L>>>> singleSubscribers =
            new Object2ObjectOpenHashMap<>();

    /**
     * A map of object identifiers to subscribers who wish to be notified regardless of the publisher's identity
     */
    private final Map<I, List<AbstractListenerWrapper<L>>> manySubscribers =
            new Object2ObjectOpenHashMap<>();

    /**
     * The flag if calls to subscribers after a publish should be made instantly or deferred to a queue till a specific
     * flush command is given
     */
    private boolean deferCallbacks;

    private final Lock dataReadLock;
    private final Lock dataWriteLock;
    private final Lock subscriberReadLock;
    private final Lock subscriberWriteLock;

    /**
     * Generates a registry with the given organic management entity and the flag if this registry supports a
     * simulation or real-world use
     *
     * @param entity the organic management entity
     * @param isSimulation flag if this registry is run in a simulation
     */
    public AbstractRegistry(IOSH entity, boolean isSimulation) {
        super(entity);
        this.deferCallbacks = isSimulation;

        ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock queueLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock listenerLock = new ReentrantReadWriteLock();

        this.dataReadLock = dataLock.readLock();
        this.dataWriteLock = dataLock.writeLock();
        this.subscriberReadLock = listenerLock.readLock();
        this.subscriberWriteLock = listenerLock.writeLock();
    }

    /**
     * Abstract method signature for converting the explicit listener to a wrapper used in this registry
     *
     * @param listener the explicit listener
     * @return a wrapper around the listener
     */
    abstract AbstractListenerWrapper<L> convertCallback(L listener);

    /**
     * Looks up and retrieves the the wrapper for a given explicit listener or if not already existing calls the
     * conversion method to create one and returns it
     *
     * @param listener the explicit listener
     * @return the retrieved listener wrapper or a newly constructed one if not existing
     */
    private AbstractListenerWrapper<L> retrieveCallback(L listener) {
        return this.wrapperMap.computeIfAbsent(listener, l -> this.convertCallback(listener));
    }

    /**
     * Publishes the given exchange object under the given identifier and with the given sender. Listeners will be
     * instantly (and concurrently) called unless the defer flag in this registry is set.
     *
     * The exchange object will be cloned for each listeners unless it indicates it's immuteability by implementing
     * the {@link IPromiseToBeImmutable} flag interface.
     *
     * @param identifier the identifier under wich the exchange object will be published
     * @param sender the sender under which the exchange object will be published
     * @param exchange the exchange object
     */
    public void publish(I identifier, UUID sender, AbstractExchange exchange) {
        assert exchange != null;
        this.dataWriteLock.lock();
        try {
            this.data.computeIfAbsent(identifier, k -> new Object2ObjectOpenHashMap<>()).put(sender, exchange);
        } finally {
            this.dataWriteLock.unlock();
        }

        if (!this.deferCallbacks) {
            this.getListeners(identifier, sender).parallelStream().forEach(t -> t.onListen(
                    exchange instanceof IPromiseToBeImmutable ? exchange : (AbstractExchange) exchange.clone()));
        } else {
            this.deferQueue.add(new Triple<>(identifier, sender, exchange));
        }
    }

    /**
     * Publishes the given exchange object under the given identifier with the sender defined in the exchange object
     * itself.
     *
     * @param identifier the identifier under wich the exchange object will be published
     * @param exchange the exchange object
     */
    public void publish(I identifier, AbstractExchange exchange) {
        this.publish(identifier, exchange.getSender(), exchange);
    }

    /**
     * Publishes the given exchange object under the given identifier with the given sender as an {@link IHasState}
     * object
     *
     * @param identifier the identifier under wich the exchange object will be published
     * @param sender the sender under which the exchange object will be published as an IHasState object
     * @param exchange the exchange object
     */
    public void publish(I identifier, IHasState sender, AbstractExchange exchange) {
        this.publish(identifier, sender.getUUID(), exchange);
    }

    /**
     * Returns all listeners subscribed to the given identifier and sender.
     *
     * @param identifier the given identifier
     * @param sender the given sender
     * @return a list of all subscriber to the specific given configuration
     */
    private List<AbstractListenerWrapper<L>> getListeners(I identifier, UUID sender) {

        this.subscriberReadLock.lock();
        try {
            List<AbstractListenerWrapper<L>> allListeners =
                    new ObjectArrayList<>(this.manySubscribers.getOrDefault(identifier, ObjectLists.emptyList()));
            if (this.singleSubscribers.containsKey(identifier)) {
                allListeners.addAll(this.singleSubscribers.get(identifier).getOrDefault(sender, ObjectLists.emptyList()));
            }
            return allListeners;
        } finally {
            this.subscriberReadLock.unlock();
        }
    }

    /**
     * Subscribes for all published exchanges under the given identifier and the given sender with the given listener
     * interface. This will also publish all currently existing exchanges to the given listener for the given
     * configuration (identifier, sender) if their timestamp matches the current time.
     *
     * @param identifier the given identifier
     * @param sender the given sender
     * @param listener the given listener interface
     */
    public void subscribe(I identifier, UUID sender, L listener) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(sender);
        Objects.requireNonNull(listener);

        this.subscriberWriteLock.lock();
        try {
            //adds this subscription configuration (identifier, sender) and the listener wrapper to the map
            // containing all subscribers for specific sender
            this.singleSubscribers.computeIfAbsent(identifier, k -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(sender,
                    l -> new ArrayList<>()).add(this.retrieveCallback(listener));
        } finally {
            this.subscriberWriteLock.unlock();
        }

        //retrieves all currently existing exchange objects for the given subscription configuration (identifer,
        // sender) and publishes them to the subscriber
        if (!this.deferCallbacks) {
            this.dataReadLock.lock();
            try {
                if (this.data.containsKey(identifier) && this.data.get(identifier).containsKey(sender)) {
                    AbstractExchange toPublish = this.data.get(identifier).get(sender);
                    if (toPublish.getTimestamp() == this.getTimer().getUnixTime()) {
                        this.retrieveCallback(listener).onListen(toPublish instanceof IPromiseToBeImmutable ?
                                toPublish : (AbstractExchange) toPublish.clone());
                    }
                }
            } finally {
                this.dataReadLock.unlock();
            }
        }
    }

    public void subscribe(I identifier, IHasState sender, L listener) {
        this.subscribe(identifier, sender.getUUID(), listener);
    }

    /**
     * Subscribes for all published exchanges under the given identifier with the given listener interface. This will
     * also publish all currently existing exchanges to the given listener for the given identifier if their
     * timestamp matches the current time.
     *
     * @param identifier the given identifier
     * @param listener the given listener interface
     */
    public void subscribe(I identifier, L listener) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(listener);

        this.subscriberWriteLock.lock();
        try {
            //adds this subscription configuration (identifier, sender) and the listener wrapper to the map
            // containing all subscribers
            this.manySubscribers.computeIfAbsent(identifier, k -> new ArrayList<>()).add(this.retrieveCallback(listener));
        } finally {
            this.subscriberWriteLock.unlock();
        }

        //retrieves all currently existing exchange objects for the given subscription configuration (identifer,
        // sender) and publishes them to the subscriber (either instantly or deferred to the queue)
        if (!this.deferCallbacks) {
            this.dataReadLock.lock();
            try {
                if (this.data.containsKey(identifier)) {
                    this.data.get(identifier).forEach((u, l) -> {
                        if (l.getTimestamp() == this.getTimer().getUnixTime()) {
                            this.retrieveCallback(listener).onListen(l instanceof IPromiseToBeImmutable ?
                                    l : (AbstractExchange) l.clone());
                        }
                    });
                }
            } finally {
                this.dataReadLock.unlock();
            }
        }
    }

    /**
     * Unsubscribes the given listener from exchanges under the given identifier with the given sender
     *
     * @param identifier the identifier of the exchange
     * @param sender the sender of the exchange
     * @param listener the listener to unsubscribe
     * @return true if the unsubscription was successfull
     */
    public boolean unSubscribe(I identifier, UUID sender, L listener) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(sender);
        Objects.requireNonNull(listener);

        boolean success;
        this.subscriberWriteLock.lock();
        try {
            return this.singleSubscribers.containsKey(identifier) && this.singleSubscribers.get(identifier).containsKey(sender) &&
                            this.wrapperMap.containsKey(listener) &&
                            this.singleSubscribers.get(identifier).get(sender).remove(this.wrapperMap.get(listener));
        } finally {
            this.subscriberWriteLock.unlock();
        }
    }

    /**
     * Unsubscribes the given listener from all exchanges under the given identifier.
     *
     * @param identifier the identifier of the exchange
     * @param listener the listener to unsubscribe
     * @return true if the unsubscription was successfull
     */
    public boolean unSubscribe(I identifier, L listener) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(listener);

        this.subscriberWriteLock.lock();
        try {
            return this.manySubscribers.containsKey(identifier) && this.wrapperMap.containsKey(listener) &&
                            this.manySubscribers.get(identifier).remove(this.wrapperMap.get(listener));
        } finally {
            this.subscriberWriteLock.unlock();
        }
    }

    /**
     * Retrieves the stored exchange that was published under the given identifier and the given sender.
     *
     * @param identifier the identifier of the exchange to retrieve
     * @param sender the sender of the exchange to retrieve
     * @return the stored exchange matching the given configuration or null if it does not exist
     */
    public AbstractExchange getData(I identifier, UUID sender) {

        this.dataReadLock.lock();
        try {
            if (this.data.containsKey(identifier) && this.data.get(identifier).containsKey(sender)) {
               AbstractExchange exchange = this.data.get(identifier).get(sender);
               return exchange instanceof IPromiseToBeImmutable ? exchange : (AbstractExchange) exchange.clone();
            } else {
                return null;
            }
        } finally {
            this.dataReadLock.unlock();
        }
    }

    /**
     * Retrives the map of all stored exchanges that were published under the given identifier
     *
     * @param identifier the identifier of all exchanges to be retrieved
     * @return a map of all the stored exchanges matching the given identifier mapped to their sender
     */
    public Map<UUID, AbstractExchange> getData(I identifier) {

        this.dataReadLock.lock();
        try {
            if (this.data.containsKey(identifier)) {
                Map<UUID, AbstractExchange> states = new Object2ObjectOpenHashMap<>(this.data.get(identifier));
                if (states.isEmpty() || states.values().stream().allMatch(e -> e instanceof IPromiseToBeImmutable)) return states;

                states.replaceAll((k, v) -> (AbstractExchange) v.clone());
                return states;
            } else {
                return Collections.emptyMap();
            }
        } finally {
            this.dataReadLock.unlock();
        }
    }

    /**
     * Retrieves a set of all currently used exchange identifiers
     *
     * @return a set of all currently used exchange identifiers
     */
    public Set<I> getDataTypes() {
        return this.data.keySet();
    }

    /**
     * Flushes the queue of published exchanges and notifies all subscribers of the stored exchange objects
     *
     * @return true if the queue was empty after flushing (necessary because of concurrent access to this class)
     */
    public boolean flushQueue() {
        while (!this.deferQueue.isEmpty()) {
            Triple<I, UUID, AbstractExchange> queueItem = this.deferQueue.poll();
            this.getListeners(queueItem.getFirst(), queueItem.getSecond()).forEach(t -> t.onListen(
                    queueItem.getThird() instanceof IPromiseToBeImmutable ?
                            queueItem.getThird() :
                            (AbstractExchange) queueItem.getThird().clone()));
        }

        return this.deferQueue.isEmpty();
    }

    /**
     * Changes the behaviour of this registry to notify subscribers immediately after an exchange object is published
     * instead of collecting all objects to publish in a queue. This will empty the current queue first and then
     * change the behaviour flag.
     */
    public void startContinuousRegistryFlush() {
        this.flushQueue();
        this.deferCallbacks = false;
    }

    /**
     * Changes the behaviour of this registry to first collect all published exchanges in a queue and only notify
     * subscribers if {@link AbstractRegistry#flushQueue()} is called.
     */
    public void stopContinuousRegistryFlush() {
        this.deferCallbacks = true;
    }
}
