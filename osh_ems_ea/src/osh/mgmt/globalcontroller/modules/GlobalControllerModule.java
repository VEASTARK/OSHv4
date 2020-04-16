package osh.mgmt.globalcontroller.modules;

import osh.core.interfaces.ILifeCycleListener;
import osh.datatypes.registry.AbstractExchange;
import osh.eal.time.TimeExchange;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.ITimeRegistryListener;

/**
 * Represents an abstract module for the global controller.
 *
 * @author Sebastian Kramer
 */
public abstract class GlobalControllerModule implements ILifeCycleListener, IDataRegistryListener,
        ITimeRegistryListener, Comparable<GlobalControllerModule> {

    private final GlobalControllerDataStorage data;

    protected int PRIORITY;

    /**
     * Constructs this module with the given global data sotrage container.
     *
     * @param data the global data storage container for all modules
     */
    public GlobalControllerModule(GlobalControllerDataStorage data) {
        this.data = data;
        this.data.registerControllerModule(this.getClass(), this);
        this.PRIORITY = Integer.MIN_VALUE;
    }

    @Override
    public void onSystemRunning() {
        //nothing for now
    }

    @Override
    public void onSystemShutdown() {
        //nothing for now
    }

    @Override
    public void onSystemIsUp() {
        //nothing for now
    }

    @Override
    public void onSystemHalt() {
        //nothing for now
    }

    @Override
    public void onSystemResume() {
        //nothing for now
    }

    @Override
    public void onSystemError() {
        //nothing for now
    }

    @Override
    public <T extends AbstractExchange> void onExchange(final T exchange) {
        //nothing for now
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        //nothing for now
    }

    public GlobalControllerDataStorage getData() {
        return this.data;
    }

    public void notifyForEvent(GlobalControllerEventEnum event) {
        //nothing for now
    }

    @Override
    public int compareTo(GlobalControllerModule o) {
        return Integer.compare(this.PRIORITY, o.PRIORITY);
    }
}
