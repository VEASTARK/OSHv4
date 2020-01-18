package osh.registry;

import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Till Schuberth, Ingo Mauser, Kaibin Bao
 */
public class StateChangedEventSet {

    public static final int MAXSIZE = 1024;
    public boolean overfull;

    private String name;
    private IGlobalLogger logger;
    private final Set<StateChangedExchange> eventSet = new HashSet<>();

    public StateChangedEventSet(IGlobalLogger logger) {
        this(logger, "anonymous");
    }

    public StateChangedEventSet(IGlobalLogger logger, String name) {
        if (logger == null) throw new NullPointerException("logger is null");
        this.logger = logger;
        this.name = name;
    }

    public synchronized void enqueue(StateChangedExchange ex) {
        this.eventSet.add(ex);
        if (!this.overfull && this.eventSet.size() > MAXSIZE) {

            Class<? extends StateExchange> type = ex.getType();

            this.logger.logWarning(
                    "Queue overfull for "
                            + this.name
                            + ", size > MAXSIZE ("
                            + MAXSIZE
                            + "). New event "
                            + ex.getClass().getName()
                            + " for "
                            + type
                            + " from "
                            + ex.getSender());
            //throw away current
            //while (eventset.size() > MAXSIZE) eventset.poll();

            this.overfull = true;
        } else if (this.overfull) {
            this.logger.logWarning(
                    "Queue for " + this.name + " normalized again.");
            this.overfull = false;
        }
    }

    public synchronized StateChangedExchange getNext() {
        Iterator<StateChangedExchange> first = this.eventSet.iterator();
        if (first.hasNext()) {
            StateChangedExchange ex = first.next();
            first.remove();
            return ex;
        } else {
            return null;
        }
    }

    public synchronized StateChangedExchange peekNext() {
        Iterator<StateChangedExchange> first = this.eventSet.iterator();
        if (first.hasNext()) {
            return first.next();
        } else {
            return null;
        }
    }

    public synchronized void removeFirst() {
        Iterator<StateChangedExchange> first = this.eventSet.iterator();
        if (first.hasNext()) {
            first.next();
            first.remove();
        }
    }

    public synchronized boolean isEmpty() {
        return this.eventSet.isEmpty();
    }

}
