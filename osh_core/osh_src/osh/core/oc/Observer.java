package osh.core.oc;

import osh.core.OCComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.eal.time.TimeExchange;
import osh.registry.interfaces.ITimeRegistryListener;

/**
 * superclass for all observer
 *
 * @author florian
 * abstract superclass for all observers
 */
public abstract class Observer extends OCComponent implements ITimeRegistryListener, ILifeCycleListener {

    protected IModelOfObservationType modelOfObservationType;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public Observer(IOSH osh) {
        super(osh);
    }

    /**
     * For the communication between the observer and the controller
     * The observer (controller?) can invoke this method to get some observed data.
     * Only an interface will be communicated, so feel free to create some own classes...
     *
     * @return
     */
    public abstract IModelOfObservationExchange getObservedModelData(IModelOfObservationType type);

    public final IModelOfObservationExchange getObservedModelData() {
        synchronized (this) {
            return this.getObservedModelData(this.modelOfObservationType);
        }
    }

    public IModelOfObservationType getModelOfObservationType() {
        return this.modelOfObservationType;
    }

    public void setModelOfObservationType(
            IModelOfObservationType modelOfObservationType) {
        this.modelOfObservationType = modelOfObservationType;
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        //...in case of use please override
    }

    @Override
    public void onSystemError() {
        //...in case of use please override
    }

    @Override
    public void onSystemHalt() {
        //...in case of use please override
    }

    @Override
    public void onSystemRunning() {
        //...in case of use please override
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        //...in case of use please override
    }

    @Override
    public void onSystemResume() {
        //...in case of use please override
    }

    @Override
    public void onSystemShutdown() {
        //...in case of use please override
    }
}
