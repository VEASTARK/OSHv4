package osh.eal.hal;

import osh.eal.hal.exceptions.HALDriverException;
import osh.eal.hal.exchange.IHALExchange;

/**
 * the class means the subject in the observer pattern for the HAL
 *
 * @author Florian Allerding
 */
public interface IDriverDataPublisher {
    void setOcDataSubscriber(IDriverDataSubscriber monitorObject);

    void removeOcDataSubscriber(IDriverDataSubscriber monitorObject);

    void updateOcDataSubscriber(IHALExchange halExchange) throws HALDriverException;
}
