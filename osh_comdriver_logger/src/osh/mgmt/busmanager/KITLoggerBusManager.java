package osh.mgmt.busmanager;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.AbstractExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;

import java.util.UUID;

/**
 * @author Florian Allerding, Ingo Mauser
 */
public class KITLoggerBusManager extends LoggerBusManager {


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param uuid
     */
    public KITLoggerBusManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);

//		this.ocRegistry.registerStateChangeListener(LoggerScheduleStateExchange.class, this);
//		this.ocRegistry.registerStateChangeListener(LoggerPriceAndLimitStateExchange.class, this);
//		this.ocRegistry.registerStateChangeListener(LoggerDeviceListStateExchange.class, this);
//		this.ocRegistry.registerStateChangeListener(LoggerWaterStorageStateExchange.class, this);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {

        AbstractExchange toSend = null;

//        if (event instanceof StateChangedExchange && ((StateChangedExchange) event).getStatefulEntity().equals(this.getUUID())) {
//			StateChangedExchange exsc = (StateChangedExchange) ex;
//			if (exsc.getType().equals(LoggerScheduleStateExchange.class)) {
//				toSend = (LoggerScheduleStateExchange) this.ocRegistry.getState(LoggerScheduleStateExchange.class, exsc.getStatefulentity());
//			} else if (exsc.getType().equals(LoggerDeviceListStateExchange.class)) {
//				toSend = (LoggerDeviceListStateExchange) this.ocRegistry.getState(LoggerDeviceListStateExchange.class, exsc.getStatefulentity());
//			} else if (exsc.getType().equals(LoggerPriceAndLimitStateExchange.class)) {
//				toSend = (LoggerPriceAndLimitStateExchange) this.ocRegistry.getState(LoggerPriceAndLimitStateExchange.class, exsc.getStatefulentity());
//			} else if (exsc.getType().equals(LoggerWaterStorageStateExchange.class)) {
//				//prevent two chps to interleave data. This solution is not very clever, but implemented in 5 lines
//				if (chpsource == null) {
//					chpsource = exsc.getStatefulentity();
//				}
//				if (chpsource.equals(exsc.getStatefulentity())) {
//					toSend = (LoggerWaterStorageStateExchange) this.ocRegistry.getState(LoggerWaterStorageStateExchange.class, exsc.getStatefulentity());
//				}
//			}
//        } else {
//            toSend = event;
//        }

        if (toSend != null) {
//			updateUnit(new GenericAbstractExchangeHALWrapper(getUUID(), toSend.getTimestamp(), toSend));
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
    }
}
