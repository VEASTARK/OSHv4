package osh.hal.interfaces.chp;

import java.time.Duration;

/**
 * @author Ingo Mauser
 */
public interface IHALChpDetails {
    boolean isRunning();

    boolean isHeatingRequest();

    boolean isElectricityRequest();

    Duration getMinRuntimeRemaining();
}
