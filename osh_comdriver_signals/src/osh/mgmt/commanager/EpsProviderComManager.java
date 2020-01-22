package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.hal.exchange.EpsComExchange;

import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class EpsProviderComManager
        extends ComManager {


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param uuid
     */
    public EpsProviderComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }


    /**
     * Receive data from ComDriver
     */
    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {

        // receive signal from ComDriver as OX-object
        if (exchangeObject instanceof EpsComExchange) {

            this.getGlobalLogger().logInfo("SmartHome received new EPS signal...");
            EpsComExchange ox = (EpsComExchange) exchangeObject;

            // set states in oc registry
            EpsStateExchange priceDetails = new EpsStateExchange(
                    this.getUUID(),
                    ox.getTimestamp(),
                    ox.causeScheduling());

            for (Entry<AncillaryCommodity, PriceSignal> e : ox.getPriceSignals().entrySet()) {
                priceDetails.setPriceSignal(e.getKey(), e.getValue());
            }

            this.getOCRegistry().publish(EpsStateExchange.class, this.getUUID(), priceDetails);
        } else {
            try {
                throw new OSHException("Signal unknown");
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }
    }

}
