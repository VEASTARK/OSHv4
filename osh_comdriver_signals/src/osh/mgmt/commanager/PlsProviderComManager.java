package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.hal.exchange.PlsComExchange;
import osh.registry.interfaces.IHasState;

import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class PlsProviderComManager
        extends ComManager
        implements IHasState {


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param uuid
     */
    public PlsProviderComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }


    /**
     * Receive data from ComDriver
     */
    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {

        // receive signal from ComDriver as OX-object
        if (exchangeObject instanceof PlsComExchange) {

            this.getGlobalLogger().logInfo("SmartHome received new PLS signal...");
            PlsComExchange ox = (PlsComExchange) exchangeObject;


            // set states in oc registry
            PlsStateExchange priceDetails = new PlsStateExchange(
                    this.getUUID(),
                    ox.getTimestamp());

            for (Entry<AncillaryCommodity, PowerLimitSignal> e : ox.getPowerLimitSignals().entrySet()) {
                priceDetails.setPowerLimitSignal(e.getKey(), e.getValue());
            }

            this.getOCRegistry().publish(PlsStateExchange.class, this, priceDetails);
        } else {
            try {
                throw new OSHException("Signal unknown");
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }
    }

}
