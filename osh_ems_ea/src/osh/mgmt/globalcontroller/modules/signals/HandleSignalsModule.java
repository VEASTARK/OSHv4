package osh.mgmt.globalcontroller.modules.signals;

import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerEventEnum;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;

/**
 * @author Sebastian Kramer
 */
public class HandleSignalsModule extends GlobalControllerModule {

    private boolean receivedNewSignals;

    public HandleSignalsModule(GlobalControllerDataStorage data) {
        super(data);
        this.PRIORITY = 2;
    }

    @Override
    public void onSystemIsUp() {
        this.getData().getOCRegistry().subscribe(EpsStateExchange.class, this);
        this.getData().getOCRegistry().subscribe(PlsStateExchange.class, this);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        super.onExchange(exchange);

        if (exchange instanceof EpsStateExchange) {
            EpsStateExchange ese = (EpsStateExchange) exchange;
            this.getData().setPriceSignals(ese.getPriceSignals());
            this.getData().notify(GlobalControllerEventEnum.RECEIVED_REGULAR_EPS);
            this.receivedNewSignals = true;
        } else if (exchange instanceof PlsStateExchange) {
            PlsStateExchange pse = (PlsStateExchange) exchange;
            this.getData().setPowerLimitSignals(pse.getPowerLimitSignals());

            this.getData().notify(GlobalControllerEventEnum.RECEIVED_REGULAR_PLS);
            this.receivedNewSignals = true;
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        // save current EPS and PLS to registry for logger
        if (this.receivedNewSignals || exchange.getTimeEvents().contains(TimeSubscribeEnum.MINUTE)){
            EpsPlsStateExchange epse = new EpsPlsStateExchange(
                    this.getData().getUUID(),
                    this.getData().getNow(),
                    this.getData().getPriceSignals(),
                    this.getData().getPowerLimitSignals(),
                    this.getData().getEpsOptimizationObjective(),
                    this.getData().getPlsOptimizationObjective(),
                    this.getData().getVarOptimizationObjective(),
                    this.getData().getUpperOverlimitFactor(),
                    this.getData().getLowerOverlimitFactor(),
                    this.receivedNewSignals);


            this.getData().getOCRegistry().publish(
                    EpsPlsStateExchange.class,
                    epse);

            this.receivedNewSignals = false;
        }
    }

}
