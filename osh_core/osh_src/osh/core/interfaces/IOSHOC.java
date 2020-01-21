package osh.core.interfaces;

import osh.core.oc.GlobalController;
import osh.core.oc.GlobalObserver;
import osh.registry.DataRegistry.OCRegistry;

/**
 * Global O/C-unit interface
 *
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public interface IOSHOC extends IOSH {

    OCRegistry getOCRegistry();

    GlobalController getGlobalController();

    GlobalObserver getGlobalObserver();

}
