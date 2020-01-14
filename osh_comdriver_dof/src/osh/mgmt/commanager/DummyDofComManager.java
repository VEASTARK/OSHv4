package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.interfaces.IOSHOC;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class DummyDofComManager extends ComManager {

    public DummyDofComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        //NOTHING
    }

}
