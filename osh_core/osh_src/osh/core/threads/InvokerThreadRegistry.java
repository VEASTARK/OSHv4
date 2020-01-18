package osh.core.threads;

import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.core.logging.IGlobalLogger;
import osh.core.threads.exceptions.InvokerThreadException;
import osh.core.threads.exceptions.SubscriberNotFoundException;
import osh.registry.EventQueue;
import osh.registry.EventReceiverWrapper;
import osh.registry.StateChangedEventSet;
import osh.registry.interfaces.IEventTypeReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages threads which are used to trigger callback functions
 *
 * @author Kaibin Bao, Till Schuberth
 */
public class InvokerThreadRegistry extends OSHComponent {

    /**
     * don't give away iterators, otherwise you will get concurrent modification exceptions
     */
    private final Map<InvokerEntry<?>, InvokerEntry<?>> invokers = new HashMap<>();
    private final List<InvokerEntry<?>> invokersAsList = new ArrayList<>();
    private final List<InvokerThread> invokerThreads = new ArrayList<>();
    private boolean threadsStarted;

    /* CONSTRUCTOR */
    public InvokerThreadRegistry(IOSH osh) {
        super(osh);
    }

    /* (DE-) REGISTER */

    /**
     * Common method for {@link InvokerThread} and triggerInvokersSequentially()
     *
     * @param invokerEntry
     * @param log
     * @param name
     * @return true iff a really bad error happens
     */
    static /* default */ boolean invoke(InvokerEntry<?> invokerEntry, IGlobalLogger log, String name) {
        try {
            invokerEntry.invoke();
        } catch (OSHException e) {
            e.printStackTrace();
            log.logError("ControllerBoxException in InvokerThread " + name, e);
        } catch (Exception e) {
            e.printStackTrace();
            log.logError("Really bad runtime exception. InvokerThread " + name + " will DIE now.", e);
            return true;
        }

        return false;
    }

    static private void notifyInvokerThread(InvokerEntry<?> invokerEntry) {
        synchronized (invokerEntry.getSyncObject()) {
            invokerEntry.getSyncObject().notifyAll();
        }
    }

    public void addRealtimeSubscriber(IRealTimeSubscriber subscriber, long refreshInterval, int priority) throws OSHException {
        if (subscriber == null) throw new NullPointerException("subscriber is null");

        RealtimeSubscriberInvoker subscriberInvoker
                = new RealtimeSubscriberInvoker(subscriber, refreshInterval, this.getTimer());

        synchronized (this) {
            if (this.invokers.containsKey(subscriberInvoker))
                throw new OSHException("RealTimeSubscriber is already registered");

            this.createThread(subscriberInvoker, priority);
        }
    }

    public void removeRealtimeSubscriber(IRealTimeSubscriber subscriber) {
        if (subscriber == null) throw new NullPointerException("subscriber is null");

        RealtimeSubscriberInvoker subscriberInvoker
                = new RealtimeSubscriberInvoker(subscriber, 0, this.getTimer());

        synchronized (this) {
            this.invokers.remove(subscriberInvoker);
            this.invokersAsList.remove(subscriberInvoker);
        }
    }

    public void addQueueSubscriber(EventReceiverWrapper subscriber, EventQueue queue) throws OSHException {
        if (subscriber == null) throw new NullPointerException("subscriber is null");

        EventQueueSubscriberInvoker subscriberInvoker
                = new EventQueueSubscriberInvoker(subscriber, queue);

        synchronized (this) {
            if (this.invokers.containsKey(subscriberInvoker))
                throw new OSHException("IEventReceiver is already registered");

            this.createThread(subscriberInvoker, Thread.NORM_PRIORITY);
        }
    }

    /**
     * Invokes one specific {@link IEventTypeReceiver}
     *
     * @return true iff subscriber was found
     * @throws SubscriberNotFoundException
     * @throws InvokerThreadException
     */
    public void invoke(EventReceiverWrapper subscriber) throws SubscriberNotFoundException, InvokerThreadException {
        if (subscriber == null) throw new SubscriberNotFoundException("argument is null");

        EventQueueSubscriberInvoker subscriberInvoker
                = new EventQueueSubscriberInvoker(subscriber, null);

        synchronized (this) {
            InvokerEntry<?> realSubscriberInvoker = this.invokers.get(subscriberInvoker);

            if (realSubscriberInvoker == null)
                throw new SubscriberNotFoundException("subscriber not in list. UUID: " + subscriber.getUUID());
            if (realSubscriberInvoker.isThreadDead())
                throw new InvokerThreadException("queue thread for subscriber " + subscriber.getUUID() + " died some time ago.");

            notifyInvokerThread(realSubscriberInvoker);
        }
    }


    /* ********* */

    public void addStateSubscriber(EventReceiverWrapper subscriber, StateChangedEventSet eventSet) throws OSHException {
        if (subscriber == null) throw new NullPointerException("subscriber is null");

        StateSubscriberInvoker subscriberInvoker
                = new StateSubscriberInvoker(subscriber, eventSet);

        synchronized (this) {
            if (this.invokers.containsKey(subscriberInvoker))
                throw new OSHException("IEventReceiver is already registered");

            this.createThread(subscriberInvoker, Thread.NORM_PRIORITY);
        }
    }

    public void notifyStateSubscriber(EventReceiverWrapper subscriber) throws SubscriberNotFoundException, InvokerThreadException {
        if (subscriber == null) throw new SubscriberNotFoundException("argument is null");

        InvokerEntry<?> realSubscriberInvoker;

        synchronized (this) {
            realSubscriberInvoker = this.invokers.get(new StateSubscriberInvoker(subscriber, null));

            if (realSubscriberInvoker == null)
                throw new SubscriberNotFoundException("subscriber not in list. UUID: " + subscriber.getUUID());
            if (realSubscriberInvoker.isThreadDead())
                throw new InvokerThreadException("queue thread for subscriber " + subscriber.getUUID() + " died some time ago.");
        }

        notifyInvokerThread(realSubscriberInvoker);
    }

    /* INVOCATION */

    private InvokerThread createThread(InvokerEntry<?> subscriberInvoker, int priority) throws OSHException {
        InvokerThread thread = new InvokerThread(this.getGlobalLogger(), subscriberInvoker);

        // start a real thread if this is not a simulation
        if (this.isConcurrent()) {
            thread.setName(subscriberInvoker.getName());
            thread.setPriority(priority);
        }

        synchronized (this) {
            this.invokers.put(subscriberInvoker, subscriberInvoker);
            this.invokersAsList.add(subscriberInvoker);
        }

        // start a real thread if this is not a simulation
        if (this.isConcurrent()) {
            synchronized (this.invokerThreads) {
                this.invokerThreads.add(thread);
                if (this.threadsStarted) {
                    //thread came after a call to startThreads, start thread immediately (e.g. new driver loaded into a running system)
                    thread.start();
                }
            }
        } else {
            thread = null;
        }

        return thread;
    }

    private boolean isConcurrent() {
        return !this.getOSH().isSimulation();
    }

    /* USED BY SIMULATION / TIMER DRIVER */

    public void triggerInvokers() {
        // notify real threads concurrently if this is not a simulation
        if (this.isConcurrent()) {
            this.triggerInvokersConcurrently();
        } else {
            this.triggerInvokersSequentially();
        }
    }

    private void triggerInvokersSequentially() {
        synchronized (this) {
            for (InvokerEntry<?> invokerEntry : this.invokersAsList) {
                if (invokerEntry.shouldInvoke()) {
                    invoke(invokerEntry, this.getGlobalLogger(), "Sequential Invoker");
                }
            }
        }
    }

    private void triggerInvokersConcurrently() {
        synchronized (this) {
            for (InvokerEntry<?> invokerEntry : this.invokersAsList) {
                notifyInvokerThread(invokerEntry);
            }
        }
    }


    public void startThreads() {
//		getGlobalLogger().logError(invokerthreads.toString());

        synchronized (this.invokerThreads) {
            for (InvokerThread thread : this.invokerThreads) {
                thread.start();
            }
            this.threadsStarted = true;
        }
    }

}
