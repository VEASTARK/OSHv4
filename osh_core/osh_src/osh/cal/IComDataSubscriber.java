package osh.cal;

/**
 * the observer in the design pattern
 *
 * @author Florian Allerding
 */
public interface IComDataSubscriber {
    void onDataFromCALDriver(ICALExchange exchangeObject);
}
