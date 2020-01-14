package osh.mgmt.localobserver;

import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;

/**
 * @author Ingo Mauser
 */
public class BcontrolLocalObserver extends LocalObserver {

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public BcontrolLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onDeviceStateUpdate() {
        //NOTHING
    }

    @Override
    public IModelOfObservationExchange getObservedModelData(
            IModelOfObservationType type) {
        return null;
    }

}
