package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.PvCommandExchange;
import osh.hal.exchange.PvControllerExchange;
import osh.registry.interfaces.IDataRegistryListener;


/**
 * @author Ingo Mauser
 */
public class PvLocalController extends LocalController implements IDataRegistryListener {


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public PvLocalController(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getTimer().registerComponent(this, 1);

        this.getOCRegistry().subscribe(PvCommandExchange.class, this.getUUID(), this);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        PvCommandExchange _cmd = (PvCommandExchange) exchange;
        if (!_cmd.getReceiver().equals(this.getUUID())) return;

        PvControllerExchange _cx = new PvControllerExchange(
                this.getUUID(),
                this.getTimer().getUnixTime(),
                _cmd.getNewPvSwitchedOn(),
                (int) Math.round(_cmd.getReactivePowerTargetValue()));
        this.updateOcDataSubscriber(_cx);
    }
}
