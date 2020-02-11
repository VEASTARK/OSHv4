package constructsimulation.configuration.general;

import osh.datatypes.power.LoadProfileCompressionTypes;

import java.time.Duration;

/**
 * Static container for default configuration values for devices.
 *
 * @author Sebastian Kramer
 */
public class GeneralConfig {

    public static final LoadProfileCompressionTypes compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
    public static final int compressionValue = 100;

    public static final LoadProfileCompressionTypes hvacCompressionType = LoadProfileCompressionTypes.TIMESLOTS;
    public static final int hvacCompressionValue = 300;

    //ipp sending
    public static final Duration newIppAfter = Duration.ofHours(1);
    public static final Duration rescheduleAfter = Duration.ofHours(4);
    public static final Duration relativeHorizon = Duration.ofHours(24);

    //predictions
    public static final int pastDaysForPrediction = 14;
    public static final float weightForOtherWeekdays = 1.0f;
    public static final float weightForSameWeekday = 5.0f;

    //signals
    public static final Duration newSignalAfter = Duration.ofHours(12);
    public static final Duration signalPeriod = Duration.ofHours(36);
}
