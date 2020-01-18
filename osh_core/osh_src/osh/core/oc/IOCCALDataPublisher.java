package osh.core.oc;

import osh.cal.ICALExchange;
import osh.core.exceptions.OSHException;

/**
 * the class means the subject in the observer pattern for the CAL
 *
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public interface IOCCALDataPublisher {
    void setOcDataSubscriber(IOCCALDataSubscriber monitorObject);

    void removeOcDataSubscriber(IOCCALDataSubscriber monitorObject);

    void updateOcDataSubscriber(ICALExchange calExchange) throws OSHException;
}
