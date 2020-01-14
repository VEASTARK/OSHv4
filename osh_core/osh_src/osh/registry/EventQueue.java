package osh.registry;

import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.CommandExchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;


/**
 * @author Till Schuberth, Ingo Mauser
 */
public class EventQueue {

    public static final int MAXSIZE = 1024;

    private String name;
    private IGlobalLogger logger;
    private final Queue<ExchangeWrapper<? extends EventExchange>> queue = new ArrayDeque<>();

    public EventQueue(IGlobalLogger logger) {
        this(logger, "anonymous");
    }

    public EventQueue(IGlobalLogger logger, String name) {
        if (logger == null) throw new NullPointerException("logger is null");
        this.logger = logger;
        this.name = name;
    }

    public synchronized <T extends EventExchange, U extends T> void enqueue(Class<T> type, U ex) {
        this.queue.add(new ExchangeWrapper<>(type, ex));
        if (this.queue.size() > MAXSIZE) {

            String append = "";
            if (StateChangedExchange.class.isAssignableFrom(type)) {
                UUID uuid = ((StateChangedExchange) ex).getStatefulEntity();
                Class<? extends StateExchange> stateType = ((StateChangedExchange) ex).getType();
                append = " Statefulentity: " + uuid + ", state type: " + stateType;
            } else if (CommandExchange.class.isAssignableFrom(type)) {
                append = " Receiver: " + ((CommandExchange) ex).getReceiver();
            }

            this.logger.logError(
                    "Queue overflow for "
                            + this.name
                            + ", size > MAXSIZE ("
                            + MAXSIZE
                            + "), throwing away old events. New event "
                            + ex.getClass().getName()
                            + " from "
                            + ex.getSender()
                            + append);
            //throw away
            while (this.queue.size() > MAXSIZE) this.queue.poll();
        }
    }

    public synchronized ExchangeWrapper<? extends EventExchange> getNext() {
        return this.queue.poll();
    }

    public synchronized ExchangeWrapper<? extends EventExchange> peekNext() {
        return this.queue.peek();
    }

    public synchronized void removeFirst() {
        this.queue.poll();
    }

    public synchronized boolean isNotEmpty() {
        return !this.queue.isEmpty();
    }

}
