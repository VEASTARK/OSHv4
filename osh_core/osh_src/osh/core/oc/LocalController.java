package osh.core.oc;

import osh.core.interfaces.IOSHOC;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.eal.hal.exchange.IHALExchange;

import java.util.UUID;

/**
 * Superclass for all local controllers
 *
 * @author Florian Allerding, Ingo Mauser
 */
public abstract class LocalController extends Controller implements IOCHALDataPublisher {

    private LocalOCUnit assignedOCUnit;

    private IOCHALDataSubscriber monitorObject;


    /**
     * CONSTRUCTOR
     */
    public LocalController(IOSHOC osh) {
        super(osh);
    }


    /**
     * get the local o/c-unit to which thing controller belongs...
     */
    public final LocalOCUnit getAssignedOCUnit() {
        return this.assignedOCUnit;
    }

    /**
     * returns the local observerUnit according to this controller
     */
    public final LocalObserver getLocalObserver() {
        return this.assignedOCUnit.localObserver;
    }

    /**
     * For the communication between the observer and the controller
     * The observer can invoke this method to get some observed data.
     * Only an interface will be communicated, so feel free to create some own classes...
     */
    public IModelOfObservationExchange getDataFromLocalObserver() {
        return this.getLocalObserver().getObservedModelData();
    }

    /**
     * Observer-Pattern (design pattern)
     */
    @Override
    public void setOcDataSubscriber(IOCHALDataSubscriber monitorObject) {
        this.monitorObject = monitorObject;

    }

    @Override
    public final void removeOcDataSubscriber(IOCHALDataSubscriber monitorObject) {
        //TODO implement (BBE)
    }

    /**
     * Calls onControllerRequest() of the Driver indirectly
     */
    @Override
    public final void updateOcDataSubscriber(IHALExchange halExchange) {
        this.monitorObject.onDataFromOcComponent(halExchange);
    }

    protected final void assignLocalOCUnit(LocalOCUnit localOCUnit) {
        this.assignedOCUnit = localOCUnit;
    }

    public UUID getDeviceID() {
        return (this.assignedOCUnit != null) ? this.assignedOCUnit.getUnitID() : null;
    }

}
