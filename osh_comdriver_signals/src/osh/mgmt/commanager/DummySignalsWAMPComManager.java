package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.hal.exchange.EpsComExchange;
import osh.hal.exchange.PlsComExchange;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class DummySignalsWAMPComManager extends ComManager implements IDataRegistryListener {

    public DummySignalsWAMPComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOCRegistry().subscribe(EpsStateExchange.class, this);
        this.getOCRegistry().subscribe(PlsStateExchange.class, this);

    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        //NOTHING
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof EpsStateExchange) {
            EpsStateExchange epse = (EpsStateExchange) exchange;

            EpsComExchange ece = new EpsComExchange(this.getUUID(), this.getTimeDriver().getCurrentTime(), epse.getPriceSignals());
            this.updateOcDataSubscriber(ece);
        } else if (exchange instanceof PlsStateExchange) {
            PlsStateExchange plse = (PlsStateExchange) exchange;

            PlsComExchange pce = new PlsComExchange(this.getUUID(), this.getTimeDriver().getCurrentTime(), plse.getPowerLimitSignals());
            this.updateOcDataSubscriber(pce);
        }
    }
}
