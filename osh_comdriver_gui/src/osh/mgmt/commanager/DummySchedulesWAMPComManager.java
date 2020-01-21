package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.hal.exchange.GUIEpsComExchange;
import osh.hal.exchange.GUIPlsComExchange;
import osh.hal.exchange.GUIScheduleComExchange;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class DummySchedulesWAMPComManager extends ComManager implements IDataRegistryListener {

    public DummySchedulesWAMPComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOCRegistry().subscribe(EpsStateExchange.class, this);
        this.getOCRegistry().subscribe(PlsStateExchange.class, this);
        this.getOCRegistry().subscribe(GUIScheduleStateExchange.class, this);

    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        //NOTHING
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {

        if (exchange instanceof GUIScheduleStateExchange) {
            GUIScheduleStateExchange se = (GUIScheduleStateExchange) exchange;
            GUIScheduleComExchange gsce = new GUIScheduleComExchange(
                    this.getUUID(), se.getTimestamp(), se.getDebugGetSchedules(), se.getStepSize());
            this.updateOcDataSubscriber(gsce);
        } else if (exchange instanceof EpsStateExchange) {
            EpsStateExchange eee = (EpsStateExchange) exchange;
            GUIEpsComExchange gece = new GUIEpsComExchange(this.getUUID(), eee.getTimestamp());
            gece.setPriceSignals(eee.getPriceSignals());
            this.updateOcDataSubscriber(gece);
        } else if (exchange instanceof PlsStateExchange) {
            PlsStateExchange pse = (PlsStateExchange) exchange;
            GUIPlsComExchange gpce = new GUIPlsComExchange(this.getUUID(), pse.getTimestamp());
            gpce.setPowerLimitSignals(pse.getPowerLimitSignals());
            this.updateOcDataSubscriber(gpce);
        }
    }

}
