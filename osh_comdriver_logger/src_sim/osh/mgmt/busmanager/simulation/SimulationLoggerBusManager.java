package osh.mgmt.busmanager.simulation;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.AbstractExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.mgmt.busmanager.LoggerBusManager;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class SimulationLoggerBusManager extends LoggerBusManager {

    /**
     * CONSTRUCTOR
     */
    public SimulationLoggerBusManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        //TODO
    }

    private UUID getGlobalOCUnitUUID() {
        return this.getOSH().getGlobalObserver().getAssignedOCUnit().getUnitID();
    }

}
