package osh.core.oc;

import osh.OSHComponent;
import osh.core.OCComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;

/**
 * superclass for all observer
 *
 * @author florian
 * abstract superclass for all observers
 */
public abstract class Observer extends OCComponent implements IRealTimeSubscriber, ILifeCycleListener {

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
        synchronized (this.getSyncObject()) {
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
    public void onNextTimePeriod() throws OSHException {
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


    @Override
    public OSHComponent getSyncObject() {
        return this;
    }


}
