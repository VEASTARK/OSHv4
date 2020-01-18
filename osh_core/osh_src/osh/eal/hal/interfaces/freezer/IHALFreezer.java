package osh.eal.hal.interfaces.freezer;

/**
 * @author Ingo Mauser
 */
public interface IHALFreezer {

    double getSetTemperature();

    double getMinTemperature();

    double getMaxTemperature();

    double getCurrentTemperature();

    int getControlSignal();

    int getControlSignalCorrectionValue();

    long getLastControlSignalCorrectionValueChange();

    int getTicksSinceLastCooling();

    boolean getCoolingOn();

    boolean getFanOn();

    boolean getSuperFrostOn();

    int getSuperFrostTicks();

    boolean getNoFrostOn();
}
