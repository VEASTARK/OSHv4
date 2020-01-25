package osh.registry.interfaces;

import osh.eal.time.TimeExchange;

/**
 * Interface that privides the onListen method for the time-registry
 *
 * @author Sebastian Kramer
 */
public interface ITimeRegistryListener {

    /**
     * Listening method that will be called from the registry if any subscribed-to exchange is published.
     *
     * @param exchange a copy (or the original of the object is immutable) of the published exchange object the
     *                 implementing class is subscribed to
     * @param <T> the type of the exchange
     */
    <T extends TimeExchange> void onTimeExchange(T exchange);
}
