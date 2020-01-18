package osh.datatypes.hal.interfaces;

import java.util.HashMap;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public interface ITemperatureDetails {
    double getTemperature();

    HashMap<String, Double> getAuxiliaryTemperatures();
}
