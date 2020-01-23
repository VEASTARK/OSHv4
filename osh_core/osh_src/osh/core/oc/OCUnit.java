package osh.core.oc;

import osh.core.interfaces.IOSHOC;
import osh.eal.EALTimeDriver;

import java.util.UUID;

/**
 * abstract superclass for the O/C-Unit container
 *
 * @author Florian Allerding
 */
public abstract class OCUnit {

    protected final UUID unitID;
    private final IOSHOC osh;


    public OCUnit(UUID unitID, IOSHOC osh) {
        this.osh = osh;
        this.unitID = unitID;
    }


    protected EALTimeDriver getSystemTimer() {
        return this.osh.getTimeDriver();
    }

    public UUID getUnitID() {
        return this.unitID;
    }

}
