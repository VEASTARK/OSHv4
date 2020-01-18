package osh.registry;

import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.threads.EventQueueSubscriberInvoker;
import osh.core.threads.InvokerThreadRegistry;
import osh.core.threads.exceptions.InvokerThreadException;
import osh.core.threads.exceptions.SubscriberNotFoundException;
import osh.datatypes.registry.CommandExchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.registry.interfaces.IRegistry;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author Till Schuberth, Florian Allerding, Ingo Mauser
 */
public abstract class Registry extends OSHComponent implements IRegistry {

    private final Map<Class<? extends StateExchange>, Map<UUID, StateExchange>> states = new HashMap<>();
    private final Map<Class<? extends StateExchange>, Set<EventReceiverWrapper>> stateListeners = new HashMap<>();

    private final Map<Class<? extends EventExchange>, Set<EventReceiverWrapper>> eventListeners = new HashMap<>();

    private final Map<EventReceiverWrapper, EventQueue> queues = new HashMap<>();
    private final Map<EventReceiverWrapper, StateChangedEventSet> stateChangedEventSets = new HashMap<>();

    private final InvokerThreadRegistry invokerRegistry;


    /**
     * CONSTRUCTOR
     */
    public Registry(IOSH osh) {
        super(osh);
        this.invokerRegistry = new InvokerThreadRegistry(osh);
    }

    /**
     * listener.onQueueEventReceived() is called
     */
    public synchronized EventQueue register(
            Class<? extends EventExchange> type, IEventTypeReceiver subscriber)
            throws OSHException {
        return this.register(type, new EventReceiverWrapper(subscriber));
    }

    /**
     * listener.onQueueEventReceived() is called
     */
    private synchronized EventQueue register(
            Class<? extends EventExchange> type, EventReceiverWrapper subscriber)
            throws OSHException {
        if (type == null || subscriber == null)
            throw new IllegalArgumentException("argument is null");

        // add subscriber to the set of subscribers for this event type
        Set<EventReceiverWrapper> eventTypeSubscribers = this.eventListeners.computeIfAbsent(type, k -> new HashSet<>());
        eventTypeSubscribers.add(subscriber);

        // create one queue for every subscriber
        EventQueue queue = this.queues.get(subscriber);
        if (queue == null) {
            queue = new EventQueue(this.getGlobalLogger(), "EventQueue for "
                    + subscriber.getUUID().toString());
            this.queues.put(subscriber, queue);
            this.invokerRegistry.addQueueSubscriber(subscriber, queue);
        }
        return queue;
    }

    public <T extends EventExchange, U extends T> void sendEvent(Class<T> type,
                                                                 U ex) {
        if (type == null || ex == null)
            throw new IllegalArgumentException("argument is null");

        this.sendEvent(type, ex, null);
    }

    public <T extends CommandExchange, U extends T> void sendCommand(
            Class<T> type, U ex) {
        if (type == null || ex == null)
            throw new IllegalArgumentException("argument is null");

        UUID receiver = ex.getReceiver();
        if (receiver == null)
            throw new NullPointerException("CommandExchange: receiver is null");

        this.sendEvent(type, ex, receiver);
    }

    @SuppressWarnings("unchecked")
    private synchronized <T extends EventExchange, U extends T> void sendEvent(
            Class<T> type, U ex, UUID receiver) {
        Set<EventReceiverWrapper> listeners = this.eventListeners.get(type);
        if (listeners == null)
            return; // no listeners

        for (EventReceiverWrapper r : listeners) {
            if (receiver != null) {
                if (!r.getUUID().equals(receiver))
                    continue; // not the receiver
            }
            EventQueue queue = this.queues.get(r);

            U exClone;
            try {
                //a cast to T should be sufficient, but if I
                //can't cast to U, cloning isn't implemented
                //properly anyway...
                exClone = (U) ex.clone();
            } catch (ClassCastException e) {
                throw new RuntimeException("You didn't implement cloning properly in your EventExchange-subclass.", e);
            }
            this.enqueueAndNotify(queue, type, exClone, r);
        }
    }

    public synchronized <T extends StateExchange> T getState(
            Class<T> type,
            UUID stateProvider) {
        Map<UUID, StateExchange> map = this.states.get(type);
        if (map == null)
            return null;

        StateExchange state = map.get(stateProvider);
        if (state == null)
            return null;

        @SuppressWarnings("unchecked")
        T t = (T) (state.clone());

        return t;
    }

    public synchronized <T extends StateExchange> Map<UUID, T> getStates(
            Class<? extends T> type) {
        Map<UUID, StateExchange> map = this.states.get(type);
        if (map == null)
            return new HashMap<>();

        Map<UUID, T> copy = new HashMap<>();
        for (Entry<UUID, StateExchange> e : map.entrySet()) {
            UUID uuid = e.getKey();
            StateExchange ex = e.getValue();

            @SuppressWarnings("unchecked")
            T clone = (T) ex.clone();

            copy.put(uuid, clone);
        }
        return copy;
    }

    public Set<Class<? extends StateExchange>> getTypes() {
        Set<Class<? extends StateExchange>> types;

        synchronized (this) {
            types = new HashSet<>(this.states.keySet());
        }

        return types;
    }

    public synchronized <T extends StateExchange, U extends T> void setState(
            Class<T> type,
            IHasState provider,
            U state) {

        if (!provider.getUUID().equals(state.getSender()))
            throw new IllegalArgumentException(
                    "provider uuid doesn't match sender uuid");

        this.setState(type, provider.getUUID(), state);
    }

    /**
     * Set the state of an arbitrary object. !USE WITH CARE! This is used by bus
     * drivers.
     *
     * @param type
     * @param state
     */
    public synchronized <T extends StateExchange, U extends T> void setStateOfSender(
            Class<T> type, U state) {
        this.setState(type, state.getSender(), state);
    }

    private synchronized <T extends StateExchange, U extends T> void setState(
            Class<T> type, UUID uuid, U state) {
        Map<UUID, StateExchange> map = this.states.computeIfAbsent(type, k -> new HashMap<>());
        map.put(uuid, state);

        // inform listeners
        Set<EventReceiverWrapper> listeners = this.stateListeners.get(type);
        // only one exchange for every listener needed, because StateChangedExchange not modifiable
        StateChangedExchange stChEx = new StateChangedExchange(
                this.getTimer().getUnixTime(),
                type,
                uuid);
        if (listeners != null) {
            for (EventReceiverWrapper r : listeners) {
                this.notifyStateChange(stChEx, r);
            }
        }
    }

    /**
     * Registers a listener which is notified whenever a state (from any device)
     * is changed. This is a legacy function, please use the new version with
     * IEventTypeReceiver as listener
     *
     * @param type
     * @param listener
     * @throws OSHException
     */
    public synchronized void registerStateChangeListener(
            Class<? extends StateExchange> type, IEventTypeReceiver listener)
            throws OSHException {
        this.registerStateChangeListener(type, new EventReceiverWrapper(listener));
    }

    /**
     * Registers a listener which is notified whenever a state (from any device)
     * is changed. This is a legacy function, please use the new version with
     * IEventTypeReceiver as listener
     *
     * @param type
     * @param listener
     * @throws OSHException
     */
    private synchronized void registerStateChangeListener(
            Class<? extends StateExchange> type, EventReceiverWrapper listener)
            throws OSHException {
        if (type == null || listener == null)
            throw new IllegalArgumentException("argument is null");
        Set<EventReceiverWrapper> listeners = this.stateListeners.computeIfAbsent(type, k -> new HashSet<>());
        listeners.add(listener);

        if (!this.stateChangedEventSets.containsKey(listener)) {
            String name;
            if (listener.getUUID() == null)
                name = "StateChangeListenerQueue for "
                        + listener.getClass().getName();
            else
                name = "StateChangeListenerQueue for "
                        + listener.getUUID().toString();

            StateChangedEventSet eventSet = new StateChangedEventSet(this.getGlobalLogger(), name);
            this.stateChangedEventSets.put(listener, eventSet);
            this.invokerRegistry.addStateSubscriber(listener, eventSet);
        }

        // push all current states (may be optional)
        Map<UUID, StateExchange> map = this.states.get(type);
        if (map != null) {
            for (Entry<UUID, StateExchange> e : map.entrySet()) {
                long timestamp = this.getTimer().getUnixTime();
                this.notifyStateChange(
                        new StateChangedExchange(timestamp, type, e.getKey()),
                        listener);
            }
        }
    }

    /**
     * enqueue ex in Queue queue and notify ComponentThread
     */
    private <T extends EventExchange> void enqueueAndNotify(EventQueue queue, Class<T> eventType, T ex,
                                                            EventReceiverWrapper receiver) {

        queue.enqueue(eventType, ex);

        try {
            this.invokerRegistry.invoke(receiver);
        } catch (SubscriberNotFoundException e) {
            this.getGlobalLogger().logWarning("receiver has not been found!", e);
        } catch (InvokerThreadException e) {
            this.getGlobalLogger().logError("thread exception", e);
        }
    }

    private void notifyStateChange(StateChangedExchange ex,
                                   EventReceiverWrapper receiver) {
        StateChangedEventSet eventSet = this.stateChangedEventSets.get(receiver);
        if (eventSet != null) {
            eventSet.enqueue(ex);
        } else {
            this.getGlobalLogger().logError("event set of " + receiver + " not found!");
        }

        try {
            this.invokerRegistry.notifyStateSubscriber(receiver);
        } catch (SubscriberNotFoundException e) {
            this.getGlobalLogger().logWarning("receiver has not been found!", e);
        } catch (InvokerThreadException e) {
            this.getGlobalLogger().logError("thread exception", e);
        }
    }

    /**
     * Let the {@link EventQueueSubscriberInvoker}s process all queues. Only
     * during simulation, all queue processing is done after this call. Use this
     * for the simulation engine.
     */
    public synchronized void flushAllQueues() {
        this.invokerRegistry.triggerInvokers();
    }

    /**
     * Use this for the simulation engine.
     *
     * @return false iff there is at least one event in some queue
     */
    public synchronized boolean areAllQueuesEmpty() {
        for (EventQueue queue : this.queues.values()) {
            if (queue.isNotEmpty())
                return false;
        }

        return true;
    }

    // TODO: WE SHOULD SYNCHRONIZE with queues, not always with the complete
    // registry

    /**
     * Use the returned object to perform one atomic operation consisting of
     * multiple method calls. Example:
     * <p>
     * synchronized (registry.getSyncObject()) { Type bla =
     * registry.getState(..); if (bla.func()) { registry.setState(...); } }
     *
     * @return Object for synchronization
     */
    public Object getSyncObject() {
        return this;
    }

    /**
     * Starts threads of the internal invoker thread registry.
     */
    public void startQueueProcessingThreads() {
        this.invokerRegistry.startThreads();
    }

    // FIXME: registry unregister functions
    // TODO: unregister functions unimplemented. Be careful, because you also have to
    // remove all queues that are not longer needed, otherwise they will get
    // filled and never be emptied.
}
