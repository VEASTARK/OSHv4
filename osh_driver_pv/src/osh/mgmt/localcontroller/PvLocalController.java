package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.PvCommandExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
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
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.HOUR);

        this.getOCRegistry().subscribe(PvCommandExchange.class, this.getUUID(), this);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        PvCommandExchange _cmd = (PvCommandExchange) exchange;
        if (!_cmd.getReceiver().equals(this.getUUID())) return;

        PvControllerExchange _cx = new PvControllerExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime(),
                _cmd.getNewPvSwitchedOn(),
                (int) Math.round(_cmd.getReactivePowerTargetValue()));
        this.updateOcDataSubscriber(_cx);
    }
}
