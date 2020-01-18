package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.PvCommandExchange;
import osh.hal.exchange.PvControllerExchange;
import osh.registry.interfaces.IEventTypeReceiver;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class PvLocalController extends LocalController implements IEventTypeReceiver {


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

        this.getOCRegistry().register(PvCommandExchange.class, this);
    }

    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) {
        PvCommandExchange _cmd = (PvCommandExchange) event;
        if (!_cmd.getReceiver().equals(this.getDeviceID())) return;

        PvControllerExchange _cx = new PvControllerExchange(
                this.getDeviceID(),
                this.getTimer().getUnixTime(),
                _cmd.getNewPvSwitchedOn(),
                (int) Math.round(_cmd.getReactivePowerTargetValue()));
        this.updateOcDataSubscriber(_cx);
    }

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }


}
