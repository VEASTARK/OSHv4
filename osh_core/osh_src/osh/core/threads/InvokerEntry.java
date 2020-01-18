package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IPromiseToEnsureSynchronization;


/**
 * Stores information about the callback function.
 * <p>
 * Strategy pattern.
 *
 * @author Kaibin Bao
 */
public abstract class InvokerEntry<T extends IPromiseToEnsureSynchronization> {

    /**
     * exit thread?
     */
    /* default */ boolean exit;
    private T subscriber;
    private boolean threadDead;

    public InvokerEntry(T subscriber) {
        if (subscriber == null) throw new NullPointerException();
        if (subscriber.getSyncObject() == null) throw new NullPointerException("synchronization object is null");

        this.subscriber = subscriber;
    }

    protected T getSubscriber() {
        return this.subscriber;
    }

    /**
     * Checks if condition for invocation is met
     *
     * @return true iff condition is met
     */
    public abstract boolean shouldInvoke();

    /**
     * {@link InvokerThread} calls getSyncObject().wait() if shouldInvoke() returns false.
     * <p>
     * Default synchronization object is this {@link InvokerEntry}.
     *
     * @return
     */
    public Object getSyncObject() {
        return this;
    }

    /**
     * Calls the callback method
     *
     * @throws OSHException
     */
    public abstract void invoke() throws OSHException;

    /**
     * should exit thread?
     */
    /* default */ boolean shouldExit() {
        return this.exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    /* default */ void threadDied() {
        this.threadDead = true;
    }

    /**
     * Returns a canonical name for debugging
     *
     * @return
     */
    public abstract String getName();


    /**
     * Needed for HashMap to work
     */
    @Override
    public abstract int hashCode();

    /**
     * Needed for HashMap to work
     */
    @Override
    public abstract boolean equals(Object obj);

    public boolean isThreadDead() {
        return this.threadDead;
    }
}
