package osh.core.oc;

import osh.cal.ICALExchange;

/**
 * the observer in the design pattern
 *
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public interface IOCCALDataSubscriber {

    void onDataFromOcComponent(ICALExchange exchangeObject);

}
