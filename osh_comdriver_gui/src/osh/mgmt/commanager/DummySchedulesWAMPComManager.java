package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.hal.exchange.GUIEpsComExchange;
import osh.hal.exchange.GUIPlsComExchange;
import osh.hal.exchange.GUIScheduleComExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class DummySchedulesWAMPComManager extends ComManager implements IHasState, IEventTypeReceiver {

    public DummySchedulesWAMPComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
        this.getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);
        this.getOCRegistry().registerStateChangeListener(GUIScheduleStateExchange.class, this);

    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        //NOTHING
    }

    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
        if (event instanceof StateChangedExchange) {
            StateChangedExchange exsc = (StateChangedExchange) event;

            if (exsc.getType().equals(GUIScheduleStateExchange.class)) {
                GUIScheduleStateExchange se =
                        this.getOCRegistry().getState(GUIScheduleStateExchange.class, exsc.getStatefulEntity());
                GUIScheduleComExchange gsce = new GUIScheduleComExchange(
                        this.getUUID(), se.getTimestamp(), se.getDebugGetSchedules(), se.getStepSize());
                this.updateOcDataSubscriber(gsce);
            } else if (exsc.getType().equals(EpsStateExchange.class)) {
                EpsStateExchange eee = this.getOCRegistry().getState(EpsStateExchange.class, exsc.getStatefulEntity());
                GUIEpsComExchange gece = new GUIEpsComExchange(this.getUUID(), eee.getTimestamp());
                gece.setPriceSignals(eee.getPriceSignals());
                this.updateOcDataSubscriber(gece);
            } else if (exsc.getType().equals(PlsStateExchange.class)) {
                PlsStateExchange pse = this.getOCRegistry().getState(PlsStateExchange.class, exsc.getStatefulEntity());
                GUIPlsComExchange gpce = new GUIPlsComExchange(this.getUUID(), pse.getTimestamp());
                gpce.setPowerLimitSignals(pse.getPowerLimitSignals());
                this.updateOcDataSubscriber(gpce);
            }
        }
    }

}
