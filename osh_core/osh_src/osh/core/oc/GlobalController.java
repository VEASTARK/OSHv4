package osh.core.oc;

import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.GAConfiguration;
import osh.core.interfaces.IOSHOC;
import osh.esc.OCEnergySimulationCore;

import java.util.UUID;

/**
 * Superclass for the global controller unit
 *
 * @author Florian Allerding
 */
public abstract class GlobalController extends Controller {

    protected final OSHParameterCollection configurationParameters;
    protected final GAConfiguration gaConfiguration;
    protected final OCEnergySimulationCore ocESC;
    private GlobalOCUnit assignedOCUnit;


    /**
     * CONSTRUCTOR
     */
    public GlobalController(IOSHOC osh, OSHParameterCollection configurationParameters,
                            GAConfiguration gaConfiguration, OCEnergySimulationCore ocESC) {
        super(osh);
        this.configurationParameters = configurationParameters;
        this.gaConfiguration = gaConfiguration;
        this.ocESC = ocESC;
    }


    protected void assignControllerBox(GlobalOCUnit assignedOCUnit) {
        this.assignedOCUnit = assignedOCUnit;
    }

    /**
     * get the local o/c-unit to which thing controller belongs...
     */
    public final GlobalOCUnit getAssignedOCUnit() {
        return this.assignedOCUnit;
    }

    /**
     * get a local controller unit from a specific local o/c-unit
     */
    public LocalController getLocalController(UUID deviceID) {
        LocalOCUnit _localOC = this.assignedOCUnit.getLocalUnits().get(deviceID);

        if (_localOC != null)
            return _localOC.localController;
        else
            return null;
    }

    /**
     * return the according global observer unit
     */
    public GlobalObserver getGlobalObserver() {
        return this.assignedOCUnit.getObserver();
    }

}
