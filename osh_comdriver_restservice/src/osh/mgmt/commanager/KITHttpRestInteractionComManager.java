package osh.mgmt.commanager;

import osh.core.interfaces.IOSHOC;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class KITHttpRestInteractionComManager extends HttpRestInteractionBusManager {

    public KITHttpRestInteractionComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

}
