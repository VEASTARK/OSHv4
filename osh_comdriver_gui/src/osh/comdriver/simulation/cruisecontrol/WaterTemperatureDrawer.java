package osh.comdriver.simulation.cruisecontrol;

import java.util.UUID;


/**
 * @author Till Schuberth, Ingo Mauser, Sebastian Kramer
 */
class WaterTemperatureDrawer extends AbstractWaterDrawer {

    public WaterTemperatureDrawer(UUID id) {
        super("WaterTemperature " + id.toString().substring(0, 6), true);
    }
}