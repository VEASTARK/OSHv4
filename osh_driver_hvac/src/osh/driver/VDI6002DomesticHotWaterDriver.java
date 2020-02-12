package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.thermal.VDI6002DomesticHotWaterStatistics;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.HotWaterDemandObserverExchange;
import osh.hal.exchange.prediction.VDI6002WaterDemandPredictionExchange;
import osh.simulation.exception.SimulationSubjectException;
import osh.utils.csv.CSVImporter;
import osh.utils.string.ParameterConstants;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

/**
 * @author Sebastian Kramer, Ingo Mauser, Jan Mueller
 */
public class VDI6002DomesticHotWaterDriver extends HALDeviceDriver {

    private String weekDayHourProbabilitiesFile;
    //d0 = hour, d1 = weekday
    private double[][] weekDayHourProbabilities;
    private double[][] cumulativeWeekDayHourProbabilities;

    private final String drawOffTypesFile;
    //d0 = hour, d1 = weekday
    private double[][] drawOffTypes;
    private double[] cumulativeProfileProbabilities;

    private double avgYearlyRuns;

    private double avgYearlyDemand;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    private SparseLoadProfile dayProfile;
    private int lastSentValue = Integer.MIN_VALUE;

    private int[] profileLength;


    /**
     * CONSTRUCTOR
     */
    public VDI6002DomesticHotWaterDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) throws SimulationSubjectException {
        super(osh, deviceID, driverConfig);

        this.drawOffTypesFile = driverConfig.getParameter(ParameterConstants.WaterDemand.drawOffFile);
        if (this.drawOffTypesFile == null) {
            throw new SimulationSubjectException("Parameter for Thermal VDI6002 Simulation missing!");
        }

        this.weekDayHourProbabilitiesFile = driverConfig.getParameter(ParameterConstants.WaterDemand.probabilitiesFile);
        if (this.weekDayHourProbabilitiesFile == null) {
            throw new SimulationSubjectException("Parameter for Thermal VDI6002 Simulation missing!");
        }

        try {
            this.avgYearlyDemand =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.WaterDemand.averageYearlyDemand));
        } catch (Exception e) {
            this.avgYearlyDemand = 700;
            this.getGlobalLogger().logWarning("Can't get avgYearlyDemand, using the default value: " + this.avgYearlyDemand);
        }

        try {
            this.compressionType = LoadProfileCompressionTypes.valueOf(this.getDriverConfig().getParameter(ParameterConstants.Compression.compressionType));
        } catch (Exception e) {
            this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
            this.getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
        }

        try {
            this.compressionValue =
                    Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.Compression.compressionValue));
        } catch (Exception e) {
            this.compressionValue = 100;
            this.getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
        }

        this.weekDayHourProbabilities = CSVImporter.readAndTransposeDouble2DimArrayFromFile(this.weekDayHourProbabilitiesFile, ";");
        this.drawOffTypes = CSVImporter.readAndTransposeDouble2DimArrayFromFile(this.drawOffTypesFile, ";");

        this.generateRunsAndProbabilities();
    }


    private void generateRunsAndProbabilities() {

        double[] sumOfDrawOffProfiles = new double[this.drawOffTypes.length];
        double[] profileProbabilities = new double[this.drawOffTypes.length];

        //calculate the total kWh of every drawOffType
        for (int d0 = 0; d0 < sumOfDrawOffProfiles.length; d0++) {
            sumOfDrawOffProfiles[d0] = Arrays.stream(this.drawOffTypes[d0]).sum() / 3600.0;
            profileProbabilities[d0] = 1 / sumOfDrawOffProfiles[d0];
        }

        double nonNormalizedProbabilitySum = Arrays.stream(profileProbabilities).sum();

        //probability for every profile is the normalized inverted sum of energy usage
        for (int d0 = 0; d0 < sumOfDrawOffProfiles.length; d0++) {
            profileProbabilities[d0] /= nonNormalizedProbabilitySum;
        }

        double avgEnergySumPerRun = 0.0;
        for (int d0 = 0; d0 < sumOfDrawOffProfiles.length; d0++) {
            avgEnergySumPerRun += sumOfDrawOffProfiles[d0] * profileProbabilities[d0];
        }
        this.cumulativeProfileProbabilities = new double[profileProbabilities.length];

        for (int d0 = 0; d0 < profileProbabilities.length; d0++) {
            if (d0 == 0)
                this.cumulativeProfileProbabilities[d0] = profileProbabilities[d0];
            else if (d0 == profileProbabilities.length - 1)
                this.cumulativeProfileProbabilities[d0] = 1;
            else
                this.cumulativeProfileProbabilities[d0] = this.cumulativeProfileProbabilities[d0 - 1] + profileProbabilities[d0];
        }


        this.avgYearlyRuns = this.avgYearlyDemand / avgEnergySumPerRun;

        this.cumulativeWeekDayHourProbabilities = new double[this.weekDayHourProbabilities.length][this.weekDayHourProbabilities[0].length];

        for (int d0 = 0; d0 < this.weekDayHourProbabilities.length; d0++) {
            for (int d1 = 0; d1 < this.weekDayHourProbabilities[d0].length; d1++) {
                if (d1 == 0)
                    this.cumulativeWeekDayHourProbabilities[d0][d1] = this.weekDayHourProbabilities[d0][d1];
                else if (d1 == this.weekDayHourProbabilities[d0].length - 1)
                    this.cumulativeWeekDayHourProbabilities[d0][d1] = 1;
                else
                    this.cumulativeWeekDayHourProbabilities[d0][d1] = this.cumulativeWeekDayHourProbabilities[d0][d1 - 1] + this.weekDayHourProbabilities[d0][d1];
            }
        }

        this.profileLength = new int[this.drawOffTypes.length];

        for (int d0 = 0; d0 < this.drawOffTypes.length; d0++) {
            int lastValue = 0;
            for (int d1 = 0; d1 < this.drawOffTypes[d0].length; d1++) {
                if (this.drawOffTypes[d0][d1] != 0)
                    lastValue = d1;
            }
            this.profileLength[d0] = lastValue + 1;
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);

        //initially give LocalObserver load data of past days
        VDI6002WaterDemandPredictionExchange _pred = new VDI6002WaterDemandPredictionExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime(),
                VDI6002DomesticHotWaterStatistics.monthlyCorrection,
                VDI6002DomesticHotWaterStatistics.dayOfWeekCorrection,
                this.weekDayHourProbabilities,
                this.avgYearlyDemand);
        this.notifyObserver(_pred);

        StaticCompressionExchange _stat = new StaticCompressionExchange(this.getUUID(), this.getTimeDriver().getCurrentTime(),
                this.compressionType, this.compressionValue);
        this.notifyObserver(_stat);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        ZonedDateTime now = exchange.getTime();

        if (this.dayProfile == null || exchange.getTimeEvents().contains(TimeSubscribeEnum.DAY)) {
            if (this.dayProfile == null)
                this.dayProfile = new SparseLoadProfile();
            long initialNumber = this.getRandomGenerator().getNextLong();
            OSHRandomGenerator newRandomGen = new OSHRandomGenerator(new Random(initialNumber));
            this.generateDailyDemandProfile(now, newRandomGen);
        }

        int power = this.dayProfile.getLoadAt(Commodity.DOMESTICHOTWATERPOWER, exchange.getEpochSecond());

        if (power != this.lastSentValue) {

            this.setPower(Commodity.DOMESTICHOTWATERPOWER, power);

            HotWaterDemandObserverExchange ox =
                    new HotWaterDemandObserverExchange(
                            this.getUUID(),
                            now,
                            power);
            this.notifyObserver(ox);

            this.lastSentValue = power;
        }
    }


    private void generateDailyDemandProfile(ZonedDateTime now, OSHRandomGenerator randomGen) {

        int month = TimeConversion.getCorrectedMonth(now);
        int weekDay = TimeConversion.getCorrectedDayOfWeek(now);
        long midnightToday = TimeConversion.getStartOfDay(now).toEpochSecond();

        int runsToday;
        double avgRunsToday = (this.avgYearlyRuns / 365.0) * VDI6002DomesticHotWaterStatistics.monthlyCorrection[month] * VDI6002DomesticHotWaterStatistics.dayOfWeekCorrection[weekDay];
        int runsFloor = (int) Math.floor(avgRunsToday);
        int runsCeil = (int) Math.ceil(avgRunsToday);

        if (randomGen.getNextDouble() < (avgRunsToday - Math.floor(avgRunsToday)))
            runsToday = runsCeil;
        else
            runsToday = runsFloor;

        SparseLoadProfile[] newDayProfiles = new SparseLoadProfile[runsToday];

        boolean lastDay = false;

        for (int i = 0; i < runsToday; i++) {

            newDayProfiles[i] = new SparseLoadProfile();

            double randomProfileNumber = randomGen.getNextDouble();
            int profileID = 0;

            for (int d0 = 0; d0 < this.cumulativeProfileProbabilities.length; d0++) {
                if (randomProfileNumber < this.cumulativeProfileProbabilities[d0]) {
                    profileID = d0;
                    break;
                }
            }
            //get random hour to start drawOff based on provided hour probabilities
            int hour = this.getRandomHourBasedOnProbabilities(randomGen, weekDay);
            //

            int randomSeconds = (int) (randomGen.getNextDouble() * 3600.0);

            //start of draw
            long startOfDrawOff = midnightToday + hour * 3600 + randomSeconds;

            //correct start time by half of the profile length (only when we not shift drawoff to the past)
            if (hour * 3600 + randomSeconds >= this.profileLength[profileID])
                startOfDrawOff -= Math.round(this.profileLength[profileID] * 0.5);

            for (int d1 = 0; d1 < this.profileLength[profileID]; d1++) {
                //profile is in kW, we use W
                newDayProfiles[i].setLoad(Commodity.DOMESTICHOTWATERPOWER, startOfDrawOff + d1, (int) Math.round(this.drawOffTypes[profileID][d1] * 1000.0));
            }
            newDayProfiles[i].setLoad(Commodity.DOMESTICHOTWATERPOWER, startOfDrawOff + this.profileLength[profileID], 0);
            newDayProfiles[i].setEndingTimeOfProfile(startOfDrawOff + this.profileLength[profileID] + 1);
        }
        //maybe loads are scheduled into this day (due to the randomSeconds deviation so merge them to be sure
        this.dayProfile = this.dayProfile.cloneAfter(midnightToday);

        for (int i = 0; i < runsToday; i++) {
            this.dayProfile = this.dayProfile.merge(newDayProfiles[i], 0);
        }

        //making sure the profile is long enough
        this.dayProfile.setEndingTimeOfProfile(midnightToday + 86400 + 3600);
        //remove duplicate Values
//		dayProfile = dayProfile.getCompressedProfileByDiscontinuities(1);
        this.dayProfile = this.dayProfile.getProfileWithoutDuplicateValues();
//		SparseLoadProfile control = dayProfile.getCompressedProfileByDiscontinuities(1);
    }

    private int getRandomHourBasedOnProbabilities(OSHRandomGenerator randomGen, int weekDay) {
        double randomNumber = randomGen.getNextDouble();
        int hour = 0;
        for (int d1 = 0; d1 < this.cumulativeWeekDayHourProbabilities[weekDay].length; d1++) {
            if (this.cumulativeWeekDayHourProbabilities[weekDay][d1] > randomNumber) {
                hour = d1;
                break;
            }
        }
        return hour;
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        //NOTHING
    }

}
