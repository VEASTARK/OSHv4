package constructsimulation.datatypes;

import osh.simulation.screenplay.ScreenplayType;

/**
 * @author Ingo Mauser
 */
public class ScreenplayWrapper {

    public final ScreenplayType screenplayType;

    public final String iDeviceScreenplayDirectory;

    public final long spsDuration;
    public final int chosenPriceCurve;

    public ScreenplayWrapper(
            ScreenplayType screenplayType,
            String iDeviceScreenplayDirectory,
            long spsDuration,
            int chosenPriceCurve) {

        this.screenplayType = screenplayType;

        this.iDeviceScreenplayDirectory = iDeviceScreenplayDirectory;

        this.spsDuration = spsDuration;
        this.chosenPriceCurve = chosenPriceCurve;
    }

}
