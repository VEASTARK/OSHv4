package osh.mgmt.localobserver;

import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;

/**
 * @author Ingo Mauser
 */
public abstract class ThermalDemandLocalObserver
        extends LocalObserver {


    /**
     * CONSTRUCTOR
     */
    public ThermalDemandLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public IModelOfObservationExchange getObservedModelData(
            IModelOfObservationType type) {
        return null;
    }
}
