package osh.comdriver.simulation.cruisecontrol.stateviewer;

import osh.datatypes.registry.StateExchange;


/**
 * @author Till Schuberth
 */
public interface StateViewerListener {
    void stateViewerRegistryChanged(StateViewerRegistryEnum registry);

    void stateViewerClassChanged(Class<? extends StateExchange> cls);
}