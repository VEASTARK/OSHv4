package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;

/**
 * Invoker thread for callback functions.
 * Which function is invoked under which condition is handled
 * via strategy pattern.
 *
 * @author Kaibin Bao
 * @see RealtimeSubscriberInvoker
 */
public class InvokerThread extends Thread {
    public InvokerEntry<?> entry;
    public IGlobalLogger log;
    private boolean dead;

    public InvokerThread(IGlobalLogger log, InvokerEntry<?> entry) throws OSHException {
        super();

        this.setName("InvokerThread for " + entry.getName());

        if (log == null) {
            throw new OSHException("CBGlobalLogger log == null");
        }
        this.log = log;
        this.entry = entry;
    }

    @Override
    public void run() {
        try {
            while (!this.entry.shouldExit()) {
                synchronized (this.entry.getSyncObject()) {
                    // wait for invocation condition
                    while (!this.entry.shouldInvoke()) {
                        try {
                            this.entry.getSyncObject().wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            this.log.logError("Thread interrupted. Should not happen! Will DIE now.", e);
                            return;
                        }
                    }
                } /* synchronized */

                // invoke callback
                if (InvokerThreadRegistry.invoke(this.entry, this.log, this.getName()))
                    break; // if a really bad error happens
            }
        } finally {
            //do this in any case
            this.dead = true;
            this.entry.threadDied();
        }
    }

    public boolean isDead() {
        return this.dead;
    }
}
