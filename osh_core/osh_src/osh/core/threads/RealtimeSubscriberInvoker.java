package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.eal.hal.HALRealTimeDriver;

/**
 * Invokes a {@link IRealTimeSubscriber} when the time has come.
 * <p>
 * A concrete strategy in the strategy pattern.
 *
 * @author Kaibin Bao
 */
public class RealtimeSubscriberInvoker extends InvokerEntry<IRealTimeSubscriber> {
    private final HALRealTimeDriver realTimeDriver;

    private final long invokeInterval;
    private long lastInvokeTimestamp; //usually 0 (iff: 1.1.1970)

    /* CONSTRUCTOR */

    /**
     * @param realTimeSubscriber
     * @param invokeInterval
     * @param realTimeDriver
     */
    public RealtimeSubscriberInvoker(
            IRealTimeSubscriber realTimeSubscriber,
            long invokeInterval,
            HALRealTimeDriver realTimeDriver) {
        super(realTimeSubscriber);

        this.invokeInterval = invokeInterval;
        this.realTimeDriver = realTimeDriver;
        this.lastInvokeTimestamp = realTimeDriver.getUnixTime();
    }

    @Override
    public boolean shouldInvoke() {
        long now = this.realTimeDriver.getUnixTime();
        return now >= (this.lastInvokeTimestamp + this.invokeInterval);
    }

    @Override
    public void invoke() throws OSHException {
        this.lastInvokeTimestamp = this.realTimeDriver.getUnixTime();
        synchronized (this.getSubscriber().getSyncObject()) {
            this.getSubscriber().onNextTimePeriod();
        }
    }

    @Override
    public String getName() {
        return "RealtimeSubscriberInvoker for " + this.getSubscriber().getClass().getName();
    }

    /* Delegate to realTimeSubscriber for HashMap */

    @Override
    public int hashCode() {
        return this.getSubscriber().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        RealtimeSubscriberInvoker that = (RealtimeSubscriberInvoker) o;

        return this.getSubscriber().equals(that.getSubscriber());
    }
}
