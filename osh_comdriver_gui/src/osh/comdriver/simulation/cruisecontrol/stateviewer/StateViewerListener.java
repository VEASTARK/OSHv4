package osh.comdriver.simulation.cruisecontrol.stateviewer;

import osh.datatypes.registry.AbstractExchange;


/**
 * @author Till Schuberth
 */
public interface StateViewerListener {
    void stateViewerRegistryChanged(StateViewerRegistryEnum registry);

    void stateViewerClassChanged(Class<? extends AbstractExchange> cls);
}