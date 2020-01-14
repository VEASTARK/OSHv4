package osh.cal;

/**
 * the class means the subject in the observer pattern for the HAL
 *
 * @author Florian Allerding
 */
public interface IComDataPublisher {
    void setComDataSubscriber(IComDataSubscriber monitorObject);

    void removeComDataSubscriber(IComDataSubscriber monitorObject);

    void updateComDataSubscriber(ICALExchange halExchange);
}
