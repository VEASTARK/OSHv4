package osh.mgmt.globalcontroller.modules.scheduling;

import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.time.TimeExchange;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerEventEnum;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;
import osh.mgmt.globalcontroller.modules.signals.HandleSignalsModule;

import java.time.ZonedDateTime;

/**
 * Represents the decision of when to trigger a new scheduling.
 *
 * @author Sebastian Kramer
 */
public class HandleSchedulingModule extends GlobalControllerModule {

    /**
     * Constructs this module with the given global data sotrage container.
     *
     * @param data the global data storage container for all modules
     */
    public HandleSchedulingModule(GlobalControllerDataStorage data) {
        super(data);
        this.PRIORITY = 0;

        this.getData().subscribe(GlobalControllerEventEnum.FORCE_SCHEDULING, this);
    }

    @Override
    public void onSystemIsUp() {
        //TODO: reactivate as soon as delay at start is handled better
//        this.getData().setLastTimeSchedulingStarted(this.getData().getNow());

        //assert required modules are present
        assert (this.getData().getControllerModule(ExecuteSchedulingModule.class) != null
                && this.getData().getControllerModule(HandleSignalsModule.class) != null);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        boolean reschedulingRequired = false;

        ZonedDateTime now = exchange.getTime();
        ZonedDateTime lastTimeSchedulingStarted = this.getData().getLastTimeSchedulingStarted();

        //check if something has been changed:
        for (InterdependentProblemPart<?, ?> problemPart : this.getData().getProblemParts()) {
            if (problemPart.isToBeScheduled() && !problemPart.getTimestamp().isBefore(lastTimeSchedulingStarted)) {
                reschedulingRequired = true;
                break;
            }
        }

        if (reschedulingRequired) {
            this.executeScheduling();
        }
    }

    private void executeScheduling() {
        ZonedDateTime now = this.getData().getNow();
        this.getData().setLastTimeSchedulingStarted(now);

        DetailedOptimisationResults result =
                this.getData().getControllerModule(ExecuteSchedulingModule.class)
                        .executeScheduling(
                                this.getData().getPriceSignals(),
                                this.getData().getPowerLimitSignals(),
                                this.getData().getProblemParts());

        this.getData().setLastOptimisationResults(result);
        this.getData().notify(GlobalControllerEventEnum.SCHEDULING_FINISHED);
        this.getData().setLastOptimisationResults(null);
    }

    @Override
    public void notifyForEvent(GlobalControllerEventEnum event) {
        if (event == GlobalControllerEventEnum.FORCE_SCHEDULING) {
            this.executeScheduling();
        }
    }
}
