package osh.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.reflections.Reflections;
import osh.OSHComponent;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.IPromiseToBeImmutable;
import osh.registry.interfaces.IProvidesIdentity;
import osh.utils.Triple;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Registry for communication between different modules of the OSH.
 * This registry works with the publish/subscribe architecture and enables subscription either for specific senders
 * (identified by their supplied UUID) of for any published exchanges of a sepcific type.
 *
 * @author Sebastian Kramer
 *
 */
public class Registry extends OSHComponent {

    /**
     * Information provided through the {@link org.reflections} package about the classpath of which all exchange
     * object lie.
     */
    private static final Reflections reflections = new Reflections(AbstractExchange.class.getPackageName());

    /**
     * A map containing all data currently known to this registry.
     */
    private final Map<Class<? extends AbstractExchange>, Map<UUID, AbstractExchange>> data =
            new Object2ObjectOpenHashMap<>();

    /**
     * The optional queue of exchange objects to publish to subscribers if this step is to only be completed
     * by a specific call and not directly after each publish call
     */
    private final ConcurrentLinkedQueue<Triple<Class<? extends AbstractExchange>, UUID, AbstractExchange>> deferQueue = new  ConcurrentLinkedQueue<>();

    /**
     * A mapping of each listener to it's wrapper object
     */
    private final Map<IDataRegistryListener, DataListenerWrapper> wrapperMap = new Object2ObjectOpenHashMap<>();

    /**
     * A map of each object identifier and publisher identity to the subscribers of this specific identifier and
     * publisher
     */
    private final Map<Class<? extends AbstractExchange>, Map<UUID, List<DataListenerWrapper>>> singleSubscribers =
            new Object2ObjectOpenHashMap<>();

    /**
     * A map of object identifiers to subscribers who wish to be notified regardless of the publisher's identity
     */
    private final Map<Class<? extends AbstractExchange>, List<DataListenerWrapper>> manySubscribers =
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
    public Registry(IOSH entity, boolean isSimulation) {
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
     * Looks up and retrieves the the wrapper for a given explicit listener or if not already existing creates a new
     * one and stores it in the map.
     *
     * @param listener the explicit listener
     * @return the retrieved listener wrapper or a newly constructed one if not existing
     */
    private DataListenerWrapper retrieveCallback(IDataRegistryListener listener) {
        return this.wrapperMap.computeIfAbsent(listener, l -> new DataListenerWrapper(listener));
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
    public <T extends AbstractExchange, U extends T> void publish(Class<T> identifier, UUID sender, U exchange) {
        assert exchange != null;
        this.dataWriteLock.lock();
        try {
            this.data.computeIfAbsent(identifier, k -> new Object2ObjectOpenHashMap<>()).put(sender, exchange);
        } finally {
            this.dataWriteLock.unlock();
        }

        if (!this.deferCallbacks) {
            this.getListeners(identifier, sender).parallelStream().forEach(t -> t.onListen(
                    exchange instanceof IPromiseToBeImmutable ? exchange : exchange.clone()));
        } else {
            this.deferQueue.add(new Triple<>(identifier, sender, exchange));
        }
    }

    /**
     * Publishes the given exchange object under the given identifier with the sender defined in the exchange object
     * itself. Will automatically set the sender of a command exchange to it's intended recipient,
     *
     * @param identifier the identifier under wich the exchange object will be published
     * @param exchange the exchange object
     */
    public <T extends AbstractExchange, U extends T> void publish(Class<T> identifier, U exchange) {
        if (CommandExchange.class.isAssignableFrom(identifier)) {
            this.publish(identifier, ((CommandExchange) exchange).getReceiver(), exchange);
        } else {
            this.publish(identifier, exchange.getSender(), exchange);
        }
    }

    /**
     * Publishes the given exchange object under the given identifier with the given sender as an {@link IProvidesIdentity}
     * object
     *
     * @param identifier the identifier under wich the exchange object will be published
     * @param sender the sender under which the exchange object will be published as an IHasState object
     * @param exchange the exchange object
     */
    public <T extends AbstractExchange, U extends T> void publish(Class<T> identifier, IProvidesIdentity sender,
                                                                  U exchange) {
        this.publish(identifier, sender.getUUID(), exchange);
    }

    /**
     * Returns all listeners subscribed to the given identifier and sender.
     *
     * @param identifier the given identifier
     * @param sender the given sender
     * @return a list of all subscriber to the specific given configuration
     */
    private List<DataListenerWrapper> getListeners(Class<? extends AbstractExchange> identifier, UUID sender) {

        this.subscriberReadLock.lock();
        try {
            List<DataListenerWrapper> allListeners = new ArrayList<>();
            Class<?> currentClass = identifier;

            while (AbstractExchange.class.isAssignableFrom(currentClass)) {

                if (this.singleSubscribers.containsKey(currentClass) && this.singleSubscribers.get(currentClass).containsKey(sender)) {
                    allListeners.addAll(this.singleSubscribers.get(currentClass).get(sender));
                }
                if (this.manySubscribers.containsKey(currentClass)) {
                    allListeners.addAll(this.manySubscribers.get(currentClass));
                }
                currentClass = currentClass.getSuperclass();
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
    public void subscribe(Class<? extends AbstractExchange> identifier, UUID sender, IDataRegistryListener listener) {
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
                    if (toPublish.getTimestamp().isEqual(this.getTimeDriver().getCurrentTime())) {
                        this.retrieveCallback(listener).onListen(toPublish instanceof IPromiseToBeImmutable ?
                                toPublish : toPublish.clone());
                    }
                }
            } finally {
                this.dataReadLock.unlock();
            }
        }
    }

    /**
     *  Helper-method to automatically extract the sender identity from {@link IProvidesIdentity} interface and then
     *  delegate to the normal subscription process.
     *
     * @param identifier the given identifier
     * @param sender the identitiy of the sender as an {@link IProvidesIdentity} interface
     * @param listener the listener interface
     */
    public void subscribe(Class<? extends AbstractExchange> identifier, IProvidesIdentity sender, IDataRegistryListener listener) {
        this.subscribe(identifier, sender.getUUID(), listener);
    }

    /**
     * Subscribes for all published exchanges under the given identifier with the given listener interface. This will
     * also publish all currently existing exchanges to the given listener for the given identifier if their
     * timestamp matches the current time.
     *
     * Will automatically reject subscribing to all command exchanges as these are  only intended for a specific recipient.
     *
     * @param identifier the given identifier
     * @param listener the given listener interface
     */
    public void subscribe(Class<? extends AbstractExchange> identifier, IDataRegistryListener listener) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(listener);

        if (CommandExchange.class.isAssignableFrom(identifier)) {
            throw new IllegalArgumentException("it is not possible to subscribe to all command exchanges, please " +
                    "provide a sender UUID");
        }

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
                        if (l.getTimestamp().isEqual(this.getTimeDriver().getCurrentTime())) {
                            this.retrieveCallback(listener).onListen(l instanceof IPromiseToBeImmutable ?
                                    l : l.clone());
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
    public boolean unSubscribe(Class<? extends AbstractExchange> identifier, UUID sender, IDataRegistryListener listener) {
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
    public boolean unSubscribe(Class<? extends AbstractExchange> identifier, IDataRegistryListener listener) {
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
    public AbstractExchange getData(Class<? extends AbstractExchange> identifier, UUID sender) {

        this.dataReadLock.lock();
        try {
            if (this.data.containsKey(identifier) && this.data.get(identifier).containsKey(sender)) {
               AbstractExchange exchange = this.data.get(identifier).get(sender);
               return exchange instanceof IPromiseToBeImmutable ? exchange : exchange.clone();
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
    public Map<UUID, AbstractExchange> getData(Class<? extends AbstractExchange> identifier) {

        this.dataReadLock.lock();
        try {
            if (this.data.containsKey(identifier)) {
                Map<UUID, AbstractExchange> states = new Object2ObjectOpenHashMap<>(this.data.get(identifier));
                if (states.isEmpty() || states.values().stream().allMatch(e -> e instanceof IPromiseToBeImmutable)) return states;

                states.replaceAll((k, v) -> v.clone());
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
    public Set<Class<? extends AbstractExchange>> getDataTypes() {
        return this.data.keySet();
    }

    /**
     * Flushes the queue of published exchanges and notifies all subscribers of the stored exchange objects
     *
     * @return true if the queue was empty after flushing (necessary because of concurrent access to this class)
     */
    public boolean flushQueue() {
        while (!this.deferQueue.isEmpty()) {
            Triple<Class<? extends AbstractExchange>, UUID, AbstractExchange> queueItem = this.deferQueue.poll();
            this.getListeners(queueItem.getFirst(), queueItem.getSecond()).forEach(t -> t.onListen(
                    queueItem.getThird() instanceof IPromiseToBeImmutable ?
                            queueItem.getThird() :
                            queueItem.getThird().clone()));
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
     * subscribers if {@link Registry#flushQueue()} is called.
     */
    public void stopContinuousRegistryFlush() {
        this.deferCallbacks = true;
    }

    /**
     * Registry for the exchange of objects in the Com-layer of the organic architecture
     */
    public static class ComRegistry extends Registry {

        /**
         * Generates a registry with the given organic management entity and the flag if this registry supports a
         * simulation or real-world use
         *
         * @param entity       the organic management entity
         * @param isSimulation flag if this registry is run in a simulation
         */
        public ComRegistry(IOSH entity, boolean isSimulation) {
            super(entity, isSimulation);
        }
    }

    /**
     * Registry for the exchange of objects in the EAL-layer of the organic architecture
     */
    public static class DriverRegistry extends Registry {

        /**
         * Generates a registry with the given organic management entity and the flag if this registry supports a
         * simulation or real-world use
         *
         * @param entity       the organic management entity
         * @param isSimulation flag if this registry is run in a simulation
         */
        public DriverRegistry(IOSH entity, boolean isSimulation) {
            super(entity, isSimulation);
        }
    }

    /**
     * Registry for the exchange of objects in the OC-layer of the organic architecture
     */
    public static class OCRegistry extends Registry {

        /**
         * Generates a registry with the given organic management entity and the flag if this registry supports a
         * simulation or real-world use
         *
         * @param entity       the organic management entity
         * @param isSimulation flag if this registry is run in a simulation
         */
        public OCRegistry(IOSH entity, boolean isSimulation) {
            super(entity, isSimulation);
        }
    }
}


