package osh.hal.interfaces.chp;

/**
 * @author Ingo Mauser
 */
public interface IHALChpDetails {
    boolean isRunning();

    boolean isHeatingRequest();

    boolean isElectricityRequest();

    int getMinRuntimeRemaining();
}
