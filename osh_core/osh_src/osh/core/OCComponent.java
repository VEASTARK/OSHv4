package osh.core;

import osh.OSHComponent;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHOC;
import osh.core.interfaces.IOSHStatus;
import osh.registry.DataRegistry.OCRegistry;

/**
 * Global superclass for all O/C-components in the Organic Smart Home
 *
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public abstract class OCComponent extends OSHComponent {

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public OCComponent(IOSH osh) {
        super(osh);
    }


    public IOSHStatus getControllerBoxStatus() {
        return this.getOSH().getOSHStatus();
    }

    @Override
    public IOSHOC getOSH() {
        return (IOSHOC) super.getOSH();
    }

    protected OCRegistry getOCRegistry() {
        return this.getOSH().getOCRegistry();
    }

}
