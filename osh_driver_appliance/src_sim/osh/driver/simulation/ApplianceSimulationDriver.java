package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.screenplay.ScreenplayType;
import osh.utils.csv.CSVImporter;
import osh.utils.slp.IH0Profile;
import osh.utils.string.StringConversions;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public abstract class ApplianceSimulationDriver
        extends DeviceSimulationDriver {

    // screenplayType specific variables
    // screenplayType = DYNAMIC

    // DYNAMIC SCREENPLAY

    /**
     * Number of avg daily runs for screenplay generation
     */
    private double avgYearlyRuns;

    /**
     * Correction factor for different probabilities per day (get it from H0)
     */
    private double[] correctionFactorDay;

    /**
     * Probability for run at a specific time of the day <br>
     * [d0] = weekday, [d1] = hour of day
     */
    private double[][] probabilityPerHourOfWeekday;

    /**
     * Probability for run at a specific time of the day <br>
     * [d0] = weekday, [d1] = hour of day
     */
    private double[][] probabilityPerHourOfWeekdayCumulativeDistribution;

    /**
     * Calculated probabilities per day
     */
    private double avgDailyRuns;

    /**
     * Shares of configurations for screenplay generation
     */
    private Double[] configurationShares;


    // TEMPORAL DEGREE OF FREEDOM

    /**
     * Max 1stTemporalDoF in ticks for generation of TDoF (initial optimization)
     */
    private int deviceMax1stTDof;

    /**
     * Max 2ndTemporalDoF in ticks for generation of TDoF (rescheduling)
     */
    @SuppressWarnings("unused")
    @Deprecated
    private int deviceMax2ndTDof;

    private ZonedDateTime lastTimeScreenplayGenerated;


    /**
     * CONSTRUCTOR
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ApplianceSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        // if DeviceClassification.APPLIANCE (but info at this point not yet available!)
        // all conditions after first && should NOT be necessary (but remain for safety reasons)
        if (driverConfig.getParameter("screenplaytype") != null) {

            ScreenplayType screenplayType = ScreenplayType.fromValue(driverConfig.getParameter("screenplaytype"));

            if (screenplayType == ScreenplayType.STATIC) {
                // screenplay is loaded from file...
            } else if (screenplayType == ScreenplayType.DYNAMIC) {
                // TEMPORAL DEGREE OF FREEDOM
                {
                    // max 1st tDoF
                    String deviceMax1stDofString = driverConfig.getParameter("devicemax1stdof");
                    if (deviceMax1stDofString != null) {
                        this.deviceMax1stTDof = Integer.parseInt(deviceMax1stDofString);
                    } else {
                        throw new RuntimeException("variable \"screenplaytype\" = DYNAMIC : missing parameter (devicemax1stdof)!");
                    }
                }
                {
                    // average yearly runs for dynamic daily screenplay
                    String avgYearlyRunsString = driverConfig.getParameter("averageyearlyruns");
                    if (avgYearlyRunsString != null) {
                        this.avgYearlyRuns = Double.parseDouble(avgYearlyRunsString);
                    } else {
                        throw new RuntimeException("Parameter missing: averageyearlyruns");
                    }
                }

                IH0Profile h0Profile;

                // H0-PROFILE
                {
                    String h0ProfileFileName = driverConfig.getParameter("h0filename");
                    String h0ProfileClass = driverConfig.getParameter("h0classname");
                    if (h0ProfileFileName != null) {
                        try {
                            Class h0Class = Class.forName(h0ProfileClass);

                            h0Profile = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
                                    .newInstance(this.getTimeDriver().getCurrentTime().getYear(),
                                            h0ProfileFileName,
                                            1000);

                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }

                        this.correctionFactorDay = h0Profile.getCorrectionFactorForDay();
                    } else {
                        throw new RuntimeException("variable \"screenplaytype\" = DYNAMIC : missing parameter (h0filename)!");
                    }
                }
                // Probability per hour of weekday
                {
                    String probabiltyFileName = driverConfig.getParameter("probabilityfilename");
                    if (probabiltyFileName != null) {
                        double[][] probabilityPerHourOfWeekdayTemp = CSVImporter.readDouble2DimArrayFromFile(probabiltyFileName, ";");
                        this.probabilityPerHourOfWeekday = new double[7][24];
                        // transpose (d0 <-> d1)
                        for (int d0 = 0; d0 < probabilityPerHourOfWeekdayTemp.length; d0++) {
                            for (int d1 = 0; d1 < probabilityPerHourOfWeekdayTemp[d0].length; d1++) {
                                this.probabilityPerHourOfWeekday[d1][d0] = probabilityPerHourOfWeekdayTemp[d0][d1];
                            }
                        }
                        //calculate cumulative distribution
                        this.probabilityPerHourOfWeekdayCumulativeDistribution = new double[this.probabilityPerHourOfWeekday.length][];
                        for (int d0 = 0; d0 < this.probabilityPerHourOfWeekday.length; d0++) {
                            this.probabilityPerHourOfWeekdayCumulativeDistribution[d0] = new double[this.probabilityPerHourOfWeekday[d0].length];

                            double temp = 0;
                            for (int d1 = 0; d1 < this.probabilityPerHourOfWeekday[d0].length; d1++) {
                                if (d1 == this.probabilityPerHourOfWeekday[d0].length - 1) {
                                    temp = 1;
                                } else {
                                    temp += this.probabilityPerHourOfWeekday[d0][d1];
                                }
                                this.probabilityPerHourOfWeekdayCumulativeDistribution[d0][d1] = temp;
                            }
                        }
                    } else {
                        throw new RuntimeException("variable \"screenplaytype\" = DYNAMIC : missing parameter (probabiltyFileName)!");
                    }
                }

                {
                    // shares of different program configurations
                    String configurationsharesString = driverConfig.getParameter("configurationshares");
                    if (configurationsharesString != null) {
                        this.configurationShares = StringConversions.fromStringToDoubleArray(configurationsharesString);
                    } else {
                        throw new RuntimeException("Parameter missing: configurationshares");
                    }
                }

                // calculate average daily runs
                this.avgDailyRuns = (this.avgYearlyRuns / 365.0);
            } else {
                throw new RuntimeException("variable \"screenplaytype\" " + screenplayType + " : not implemented!");
            }
        }
    }

    /**
     * Get random hour of day according to specific probability
     * . Used for dynamic screenplay<br>
     */
    private int getRandomHourToRunBasedOnProbabilityMap(
            long timestamp,
            OSHRandomGenerator randomGen) {
        double randomDouble = randomGen.getNextDouble();
        int weekday = TimeConversion.convertUnixTime2CorrectedDayOfWeek(timestamp);
        int hour = 0;
        double[] probability = this.probabilityPerHourOfWeekdayCumulativeDistribution[weekday];
        for (int i = 0; i < probability.length; i++) {
            if (probability[i] > randomDouble) {
                hour = i;
                break;
            }
        }
        return hour;
    }


    /**
     * Get random starting time of device
     */
    public long getRandomTimestampForRunToday(
            long timestamp,
            int middleOfPowerConsumption,
            double randomValue,
            OSHRandomGenerator randomGen) {
        int randomHour = this.getRandomHourToRunBasedOnProbabilityMap(timestamp, randomGen);

        double deviation = randomValue;
        if (randomGen.getNextBoolean()) {
            deviation = (-1) * deviation;
        }
        double deviatedHour = randomHour + 0.5 + deviation - (middleOfPowerConsumption / 3600.0);
        if (deviatedHour < 0) {
            deviatedHour += 24;
        } else if (deviatedHour >= 24) {
            deviatedHour -= 24;
        }
        return (long) (this.getTimeDriver().getCurrentEpochSecond() + deviatedHour * 3600.0);
    }


    /**
     * has to be implemented by every device...
     */
    protected abstract void generateDynamicDailyScreenplay() throws OSHException;


    /**
     * in case of use please override
     */
    protected abstract int generateNewDof(boolean useRandomDof, int noActions, long applianceActionTimeTick, OSHRandomGenerator randomGen, int maxDof, int maxPossibleDof);


    @Override
    public final void triggerSubject() {
        super.triggerSubject();

        //if dynamic screenplay then generate daily screenplay
        if (this.getSimulationEngine().getScreenplayType() == ScreenplayType.DYNAMIC) {
            if (this.lastTimeScreenplayGenerated == null ||
                    this.getTimeDriver().getCurrentTime().getDayOfYear() != this.lastTimeScreenplayGenerated.getDayOfYear()) {
                try {
                    this.generateDynamicDailyScreenplay();
                } catch (OSHException e) {
                    e.printStackTrace();
                }
                this.lastTimeScreenplayGenerated = this.getTimeDriver().getCurrentTime();
            }
        }
    }


    /**
     * A: Please don't ask why it works...but it should be fine!<br>
     * B: No, it did not work, now it does ...
     */
    protected int calcMax1stTDof(int actionCount, int availableTime, int maxProgramDuration) {
        int maxDof = this.deviceMax1stTDof;

        // + 100 to be safe...
        while (Math.floor(availableTime / (double) (maxDof + maxProgramDuration + 100)) < actionCount) {
            maxDof *= 0.9;

            //no dof can be found, we need to rely on our run correction
            if (maxDof < 10) {
                return 0;
            }
        }

        // run on last day immediately
        if (!this.getTimeDriver().getTimeAtStart().plusSeconds(this.getSimulationEngine().getSimulationDuration()).minusDays(1)
                .isAfter(this.getTimeDriver().getCurrentTime())) {
            maxDof = 0;
        }

        return maxDof;
    }

    // used only within this class
    protected int getDeviceMax1stDof() {
        return this.deviceMax1stTDof;
    }

    // used by MieleAppliance
    protected double getAvgYearlyRuns() {
        return this.avgYearlyRuns;
    }

    // used by FutureAppliance
    protected double getAvgDailyRuns() {
        return this.avgDailyRuns;
    }

    // used by FutureAppliance
    protected Double[] getConfigurationShares() {
        return this.configurationShares;
    }

    // used by FutureAppliance
    protected double[] getCorrectionFactorDay() {
        return this.correctionFactorDay;
    }

}
