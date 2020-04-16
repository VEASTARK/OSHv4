package osh.core.oc;

import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.CostConfiguration;
import osh.configuration.oc.EAConfiguration;
import osh.configuration.oc.GlobalControllerConfiguration;
import osh.core.interfaces.IOSHOC;
import osh.esc.OptimizationEnergySimulationCore;

import java.util.UUID;

/**
 * Superclass for the global controller unit
 *
 * @author Florian Allerding
 */
public abstract class GlobalController extends Controller {

    protected final OSHParameterCollection configurationParameters;
    protected final GlobalControllerConfiguration controllerConfiguration;
    protected final EAConfiguration eaConfiguration;
    protected final CostConfiguration costConfiguration;
    protected final OptimizationEnergySimulationCore optimizationESC;
    private GlobalOCUnit assignedOCUnit;


    /**
     * CONSTRUCTOR
     */
    public GlobalController(IOSHOC osh, GlobalControllerConfiguration controllerConfiguration,
                            OptimizationEnergySimulationCore optimizationESC) {
        super(osh);
        this.controllerConfiguration = controllerConfiguration;
        this.configurationParameters = new OSHParameterCollection();
        this.configurationParameters.loadCollection(controllerConfiguration.getGlobalControllerParameters());
        this.eaConfiguration = controllerConfiguration.getEaConfiguration();
        this.costConfiguration = controllerConfiguration.getCostConfiguration();
        this.optimizationESC = optimizationESC;
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
