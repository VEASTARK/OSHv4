package osh.core.oc;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * container class for the virtual O/C-unit. it represents the central controlling unit
 *
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class GlobalOCUnit extends OCUnit {

    private final GlobalObserver observer;
    private final GlobalController controller;
    private final HashMap<UUID, LocalOCUnit> localUnits;


    /**
     * CONSTRUCTOR
     */
    public GlobalOCUnit(
            UUID unitID,
            IOSHOC osh,
            GlobalObserver globalObserver,
            GlobalController globalController) {
        super(unitID, osh);

        this.localUnits = new HashMap<>();
        this.observer = globalObserver;
        this.controller = globalController;

        this.observer.assignControllerBox(this);
        this.controller.assignControllerBox(this);
    }


    public GlobalObserver getObserver() {
        return this.observer;
    }

    public Controller getController() {
        return this.controller;
    }

    public void registerLocalUnit(LocalOCUnit localUnit) throws OSHException {

        // put and check if it already exists
        LocalOCUnit old;
        if ((old = this.localUnits.put(localUnit.getUnitID(), localUnit)) != null) {
            throw new OSHException("UUID " + localUnit.getUnitID() + " already registered!" + old.toString());
        }
    }

    protected HashMap<UUID, LocalOCUnit> getLocalUnits() {
        return this.localUnits;
    }

    public UUID[] getLocalUnitsUUIDs() {
        UUID[] uuids = new UUID[this.localUnits.size()];
        int i = 0;
        for (Entry<UUID, LocalOCUnit> localUnit : this.localUnits.entrySet()) {
            uuids[i] = localUnit.getKey();
            i++;
        }

        return uuids;
    }

}