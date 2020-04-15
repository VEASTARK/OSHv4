package osh.mgmt.globalcontroller;

import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.GAConfiguration;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalController;
import osh.datatypes.registry.AbstractExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.GAParameters;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;
import osh.mgmt.globalcontroller.modules.communication.CommunicateCommandPredictionModule;
import osh.mgmt.globalcontroller.modules.communication.CommunicateGUIModule;
import osh.mgmt.globalcontroller.modules.scheduling.ExecuteSchedulingModule;
import osh.mgmt.globalcontroller.modules.scheduling.HandleSchedulingModule;
import osh.mgmt.globalcontroller.modules.signals.HandleSignalsModule;
import osh.mgmt.globalobserver.OSHGlobalObserver;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a global controller, that can be modified by adding/removing modules that perform certain actions.
 *
 * @author Sebastian Kramer
 *
 */
public class ModularGlobalController extends GlobalController implements IDataRegistryListener {

    private OSHGlobalObserver oshGlobalObserver;
    private final GAParameters gaparameters;

    private GlobalControllerDataStorage dataStorage;

    private final List<GlobalControllerModule> modules = new ArrayList<>();

    /**
     * Constructs this global controller with the given {@link IOSHOC} global entity, the {@link osh.esc.EnergySimulationCore}
     * and the configuration parameters.
     *
     * @param entity the global entity
     * @param configurationParameters the configuration parameters for the global controller
     * @param gaConfiguration the configuration parameters for the genetic algorithm
     * @param ocESC the energy-simulation-core for the optimization loop
     *
     * @throws Exception when the gaConfiguration could not be parsed
     */
    public ModularGlobalController(
            IOSHOC entity,
            OSHParameterCollection configurationParameters,
            GAConfiguration gaConfiguration, OCEnergySimulationCore ocESC) throws Exception {
        super(entity, configurationParameters, gaConfiguration, ocESC);

        try {
            this.gaparameters = new GAParameters(this.gaConfiguration);
        } catch (Exception ex) {
            this.getGlobalLogger().logError("Can't parse GAParameters, will shut down now!");
            throw ex;
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // safety first...
        if (this.getGlobalObserver() instanceof OSHGlobalObserver) {
            this.oshGlobalObserver = (OSHGlobalObserver) this.getGlobalObserver();
        } else {
            throw new OSHException("this global controller only works with global observers of type " + OSHGlobalObserver.class.getName());
        }

        try {
            this.dataStorage = new GlobalControllerDataStorage(
                    this.getAssignedOCUnit().getUnitID(),
                    this.getTimeDriver().getTimeAtStart(),
                    this.getOSH().getOSHStatus(),
                    this.getOCRegistry(),
                    this,
                    this.getGlobalObserver(),
                    this.configurationParameters,
                    this.gaparameters,
                    this.getGlobalLogger(),
                    this.ocESC);
        } catch (Exception e) {
            throw new OSHException(e);
        }
        this.dataStorage.setLastTimeSchedulingStarted(this.getTimeDriver().getTimeAtStart().plusSeconds(60));

        this.modules.add(new HandleSignalsModule(this.dataStorage));
        this.modules.add(new HandleSchedulingModule(this.dataStorage));
        this.modules.add(new ExecuteSchedulingModule(this.dataStorage));
        this.modules.add(new CommunicateCommandPredictionModule(this.dataStorage));
        if (this.dataStorage.getStatus().hasGUI()) {
            this.modules.add(new CommunicateGUIModule(this.dataStorage));
        }
        Collections.sort(this.modules);


        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);

        for (GlobalControllerModule module : this.modules) {
            module.onSystemIsUp();
        }
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

        for (GlobalControllerModule module : this.modules) {
            module.onSystemShutdown();
        }
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        for (GlobalControllerModule module : this.modules) {
            module.onExchange(exchange);
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        this.dataStorage.setNow(exchange.getTime());
        this.dataStorage.setProblemParts(this.oshGlobalObserver.getProblemParts());

        for (GlobalControllerModule module : this.modules) {
            module.onTimeExchange(exchange);
        }
    }
}
