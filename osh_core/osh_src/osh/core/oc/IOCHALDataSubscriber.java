package osh.core.oc;

import osh.eal.hal.exchange.IHALExchange;

/**
 * the observer in the design pattern
 *
 * @author Florian Allerding
 */
public interface IOCHALDataSubscriber {

    void onDataFromOcComponent(IHALExchange exchangeObject);

}
