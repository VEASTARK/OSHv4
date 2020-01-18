package osh.eal.hal.interfaces.hvac;

/**
 * @author Ingo Mauser
 */
public interface IHALAdsorptionChillerDetails {
    boolean isRunning();

    int getMinRuntimeRemaining();
}
