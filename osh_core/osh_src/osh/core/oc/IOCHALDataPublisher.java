package osh.core.oc;

import osh.eal.hal.exchange.IHALExchange;

/**
 * the class means the subject in the observer pattern for the HAL
 *
 * @author Florian Allerding
 */
public interface IOCHALDataPublisher {
    void setOcDataSubscriber(IOCHALDataSubscriber monitorObject);

    void removeOcDataSubscriber(IOCHALDataSubscriber monitorObject);

    void updateOcDataSubscriber(IHALExchange halExchange);
}
