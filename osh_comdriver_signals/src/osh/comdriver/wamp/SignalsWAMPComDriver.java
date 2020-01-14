package osh.comdriver.wamp;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.hal.exchange.EpsComExchange;
import osh.hal.exchange.PlsComExchange;

import java.util.Map;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class SignalsWAMPComDriver extends CALComDriver implements Runnable {

    SignalsWAMPDispatcher signalsWampDispatcher;
    private Map<AncillaryCommodity, PriceSignal> priceSignals;
    private Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public SignalsWAMPComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.signalsWampDispatcher = new SignalsWAMPDispatcher(this.getGlobalLogger());

        new Thread(this, "push proxy of EPS/PLS signals driver to WAMP").start();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this.signalsWampDispatcher) {
                try { // wait for new data
                    this.signalsWampDispatcher.wait();
                } catch (InterruptedException e) {
                    this.getGlobalLogger().logError("should not happen", e);
                    break;
                }
            }
        }
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        if (exchangeObject instanceof EpsComExchange) {
            this.priceSignals = ((EpsComExchange) exchangeObject).getPriceSignals();
            this.signalsWampDispatcher.sendEPS(this.priceSignals);
        } else if (exchangeObject instanceof PlsComExchange) {
            this.powerLimitSignals = ((PlsComExchange) exchangeObject).getPowerLimitSignals();
            this.signalsWampDispatcher.sendPLS(this.powerLimitSignals);
        }
    }
}
