package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.dof.DofStateExchange;
import osh.hal.exchange.DofComExchange;

import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class SimpleDofComManager extends ComManager {

    public SimpleDofComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        if (exchangeObject instanceof DofComExchange) {
            DofComExchange dce = (DofComExchange) exchangeObject;


            long now = this.getTimer().getUnixTime();

            for (Entry<UUID, Integer> en : dce.getDevice1stDegreeOfFreedom().entrySet()) {

                Integer FirstDegree = en.getValue();
                Integer SecondDegree = dce.getDevice2ndDegreeOfFreedom().get(en.getKey());

                if (FirstDegree == null || SecondDegree == null) {
                    this.getGlobalLogger().logError("Received invalid DOF (null value) for: " + en.getKey());
                } else {
                    DofStateExchange dse = new DofStateExchange(en.getKey(), now);
                    dse.setDevice1stDegreeOfFreedom(FirstDegree);
                    dse.setDevice2ndDegreeOfFreedom(SecondDegree);

                    this.getOCRegistry().publish(DofStateExchange.class, dse);
                }
            }
        }
    }
}
