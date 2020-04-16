package osh.mgmt.globalcontroller;

import osh.configuration.oc.CostConfiguration;
import osh.configuration.oc.GlobalControllerConfiguration;
import osh.configuration.system.ConfigurationParameter;
import osh.core.EARandomDistributor;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalController;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.details.energy.CostConfigurationStateExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.esc.OptimizationEnergySimulationCore;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;
import osh.mgmt.globalobserver.OSHGlobalObserver;
import osh.registry.interfaces.IDataRegistryListener;
import osh.utils.string.ParameterConstants;

import java.lang.reflect.Constructor;
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

    private GlobalControllerDataStorage dataStorage;

    private final List<GlobalControllerModule> modules = new ArrayList<>();

    /**
     * Constructs this global controller with the given {@link IOSHOC} global entity, the {@link osh.esc.EnergySimulationCore}
     * and the controller configuration.
     *
     * @param entity the global entity
     * @param controllerConfiguration the controller configuration
     * @param optimizationESC the energy-simulation-core for the optimization loop
     *
     */
    public ModularGlobalController(IOSHOC entity, GlobalControllerConfiguration controllerConfiguration,
                                   OptimizationEnergySimulationCore optimizationESC) {
        super(entity, controllerConfiguration, optimizationESC);

        //set to true if single threaded execution is desired
        if (false) {
            ConfigurationParameter singleT = new ConfigurationParameter();
            singleT.setParameterName(ParameterConstants.EA_ALGORITHM.singleThreaded);
            singleT.setParameterValue("" + true);
            singleT.setParameterType(Boolean.class.getName());
            this.eaConfiguration.getAlgorithms().forEach(c ->c.getAlgorithmParameters().add(singleT));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // safety first...
        if (this.getGlobalObserver() instanceof OSHGlobalObserver) {
            this.oshGlobalObserver = (OSHGlobalObserver) this.getGlobalObserver();
        } else {
            throw new OSHException("this global controller only works with global observers of type " + OSHGlobalObserver.class.getName());
        }

        this.getOCRegistry().publish(CostConfigurationStateExchange.class,
                new CostConfigurationStateExchange(this.getAssignedOCUnit().getUnitID(), this.getTimeDriver().getCurrentTime(),
                        new CostConfiguration(this.costConfiguration)));

        EARandomDistributor eaRandomDistributor = new EARandomDistributor(this.getOSH(), this.controllerConfiguration.getOptimizationMainRandomSeed());
        eaRandomDistributor.startClock();

        try {
            this.dataStorage = new GlobalControllerDataStorage(
                    this.getAssignedOCUnit().getUnitID(),
                    this.getTimeDriver().getTimeAtStart(),
                    this.getOSH().getOSHStatus(),
                    this.getOCRegistry(),
                    this,
                    this.getGlobalObserver(),
                    this.configurationParameters,
                    this.eaConfiguration,
                    this.getGlobalLogger(),
                    this.optimizationESC,
                    this.costConfiguration,
                    eaRandomDistributor);
        } catch (Exception e) {
            throw new OSHException(e);
        }

        for (String className : this.controllerConfiguration.getControllerModules()) {

            Class<GlobalControllerModule> clazz;
            try {
                clazz = (Class<GlobalControllerModule>) Class.forName(className);
            } catch (ClassNotFoundException ex) {
                throw new OSHException(ex);
            }

            GlobalControllerModule module;
            try {
                Constructor<GlobalControllerModule> constructor = clazz.getConstructor(
                        GlobalControllerDataStorage.class);
                module = constructor.newInstance(this.dataStorage);
                this.getGlobalLogger().logInfo("ControllerModule: " + module.getClass().getSimpleName() + " loaded ...... [OK]");
            } catch (InstantiationException iex) {
                throw new OSHException("Instantiation of " + clazz + " failed!", iex);
            } catch (Exception ex) {
                throw new OSHException(ex);
            }

            this.modules.add(module);
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
