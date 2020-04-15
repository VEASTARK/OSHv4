package osh.mgmt.globalcontroller.modules.communication;

import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerEventEnum;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;
import osh.mgmt.globalcontroller.modules.scheduling.DetailedOptimisationResults;
import osh.mgmt.globalcontroller.modules.scheduling.ExecuteSchedulingModule;
import osh.mgmt.globalcontroller.modules.scheduling.HandleSchedulingModule;

import java.util.Map.Entry;
import java.util.UUID;

/**
 * Represents the communication of the commands and predictions resulting from a scheduling to the OSH components.
 *
 * @author Sebastian Kramer
 */
public class CommunicateCommandPredictionModule extends GlobalControllerModule {

    /**
     * Constructs this module with the given global data sotrage container.
     *
     * @param data the global data storage container for all modules
     */
    public CommunicateCommandPredictionModule(GlobalControllerDataStorage data) {
        super(data);
        data.subscribe(GlobalControllerEventEnum.SCHEDULING_FINISHED, this);
    }

    @Override
    public void onSystemIsUp() {
        assert (this.getData().getControllerModule(ExecuteSchedulingModule.class) != null
                && this.getData().getControllerModule(HandleSchedulingModule.class) != null);
    }

    @Override
    public void notifyForEvent(GlobalControllerEventEnum event) {
        if (event == GlobalControllerEventEnum.SCHEDULING_FINISHED) {
            DetailedOptimisationResults results = this.getData().getLastOptimisationResults();

            if (results != null) {

                for (Entry<UUID, EASolutionCommandExchange<?>> en : results.getSolutionExchanges().entrySet()) {
                    this.getData().getOCRegistry().publish(
                            EASolutionCommandExchange.class,
                            en.getValue());
                }

                for (Entry<UUID, EAPredictionCommandExchange<?>> en : results.getPredictionExchanges().entrySet()) {
                    this.getData().getOCRegistry().publish(
                            EAPredictionCommandExchange.class,
                            en.getValue());
                }
            }
        }
    }
}
