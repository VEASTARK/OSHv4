package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandom;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.logging.thermal.ThermalLoggingObject;
import osh.datatypes.logging.thermal.ThermalSupplyLogObject;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.thermal.ThermalDemandData;
import osh.eal.hal.exceptions.HALException;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.HotWaterDemandObserverExchange;
import osh.hal.exchange.prediction.WaterDemandPredictionExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.database.DatabaseLoggerThread;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.PhysicalConstants;
import osh.utils.string.ParameterConstants;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Sebastian Kramer, Ingo Mauser
 */
public abstract class ThermalDemandSimulationDriver
        extends DeviceSimulationDriver {

    private final String inputSourceFile;
    private final ThermalDemandData demandData;

    private int pastDaysPrediction;
    private float weightForOtherWeekday;
    private float weightForSameWeekday;

    private final Commodity hotWaterType;

    private boolean log;

    private double[][] avgWeekDayLoad;
    private int[][] avgWeekDayLoadCounter;

    private double[] avgDayLoad;
    private int[] avgDayLoadCounter;


    /**
     * CONSTRUCTOR
     */
    public ThermalDemandSimulationDriver(IOSH osh,
                                         UUID deviceID, OSHParameterCollection driverConfig,
                                         Commodity hotWaterType)
            throws SimulationSubjectException, HALException {
        super(osh, deviceID, driverConfig);

        this.inputSourceFile = driverConfig.getParameter(ParameterConstants.WaterDemand.sourceFile);
        if (this.inputSourceFile == null) {
            throw new SimulationSubjectException("Parameter for Thermal ESHL Simulation missing!");
        }

        this.demandData = new ThermalDemandData(this.inputSourceFile, hotWaterType);

        try {
            this.pastDaysPrediction = Integer.parseInt(driverConfig.getParameter(ParameterConstants.Prediction.pastDaysPrediction));
        } catch (Exception e) {
            this.pastDaysPrediction = 14;
            this.getGlobalLogger().logWarning("Can't get pastDaysPrediction, using the default value: " + this.pastDaysPrediction);
        }

        try {
            this.weightForOtherWeekday = Float.parseFloat(driverConfig.getParameter(ParameterConstants.Prediction.weightForOtherWeekday));
        } catch (Exception e) {
            this.weightForOtherWeekday = 1.0f;
            this.getGlobalLogger().logWarning("Can't get weightForOtherWeekday, using the default value: " + this.weightForOtherWeekday);
        }

        try {
            this.weightForSameWeekday = Float.parseFloat(driverConfig.getParameter(ParameterConstants.Prediction.weightForSameWeekday));
        } catch (Exception e) {
            this.weightForSameWeekday = 5.0f;
            this.getGlobalLogger().logWarning("Can't get weightForSameWeekday, using the default value: " + this.weightForSameWeekday);
        }

        this.hotWaterType = hotWaterType;
    }


    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();
        //initially give LocalObserver load data of past days
        ZonedDateTime timeAtStart = this.getTimeDriver().getTimeAtStart();

        List<SparseLoadProfile> predictions = new LinkedList<>();

        //starting in reverse so that the oldest profile is at index 0 in the list
        for (int i = this.pastDaysPrediction; i >= 1; i--) {
            int day = TimeConversion.getCorrectedDayOfYear(timeAtStart.minusDays(i));
            //profile only provides for 365 days, so we have to go further back for leap-years
            if (day > 364) day = 364;

            predictions.add(this.demandData.getProfileForDayOfYear(day).getProfileWithoutDuplicateValues());
        }

        WaterDemandPredictionExchange _ox = new WaterDemandPredictionExchange(this.getUUID(),
                this.getTimeDriver().getCurrentTime(),
                predictions, this.pastDaysPrediction, this.weightForOtherWeekday, this.weightForSameWeekday);
        this.notifyObserver(_ox);

        if (DatabaseLoggerThread.isLogThermal()) {
            this.avgWeekDayLoad = new double[7][1440];
            this.avgWeekDayLoadCounter = new int[7][1440];

            for (int i = 0; i < this.avgWeekDayLoadCounter.length; i++) {
                Arrays.fill(this.avgWeekDayLoad[i], 0.0);
                Arrays.fill(this.avgWeekDayLoadCounter[i], 0);
            }

            int daysInYear = TimeConversion.getNumberOfDaysInYearFromTime(timeAtStart);
            this.avgDayLoad = new double[daysInYear];
            this.avgDayLoadCounter = new int[daysInYear];
            Arrays.fill(this.avgDayLoad, 0.0);
            Arrays.fill(this.avgDayLoadCounter, 0);

            this.log = true;
        }
    }


    @Override
    public void onNextTimeTick() {
        OSHRandom rand = this.getRandomDistributor().getRandomGenerator(this.getUUID(), this.getClass());

        int randomHourShift = 2; // % 2 == 0

        // get new values
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();
        if (this.getTimeDriver().getCurrentTimeEvents().contains(TimeSubscribeEnum.HOUR)) {
//			double demand = 0;
            int randomNumber = rand.getNextInt(randomHourShift + 1); // randomHourShift + 1 exclusive!! --> max == randomHourShift
            double demand = (0.5 + rand.getNextDouble()) * this.demandData.getTotalThermalDemand(now, randomNumber
                    , randomHourShift);
//			demand += 0.25 * demandData.getTotalThermalDemand(now - 3600, 0, 0);
//			demand += 0.5 * demandData.getTotalThermalDemand(now, 0, 0);
//			demand += 0.25 * demandData.getTotalThermalDemand(now + 3600, 0, 0);

            // demand: month correction
            demand *= this.getMonthlyCorrection(TimeConversion.getCorrectedMonth(now));

            // demand: day of week correction
            demand *= this.getDayOfWeekCorrection(TimeConversion.getCorrectedDayOfWeek(now));

            // demand: general correction value
            demand *= this.getGeneralCorrection();

            this.setPower(this.hotWaterType, (int) Math.round(demand));

            HotWaterDemandObserverExchange ox =
                    new HotWaterDemandObserverExchange(
                            this.getUUID(),
                            now,
                            (int) demand);
            this.notifyObserver(ox);
        }

        if (this.log) {
            int power = this.getPower(this.hotWaterType);
            int weekDay = TimeConversion.getCorrectedDayOfWeek(now);
            int minute = now.getMinute();
            int dayOfYear = now.getDayOfYear();
            this.avgWeekDayLoad[weekDay][minute] += power;
            this.avgWeekDayLoadCounter[weekDay][minute]++;
            this.avgDayLoad[dayOfYear] += power;
            this.avgDayLoadCounter[dayOfYear]++;
        }
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

        if (this.getOSH().getOSHStatus().isSimulation() && this.log) {

            for (int d0 = 0; d0 < this.avgWeekDayLoad.length; d0++) {
                for (int d1 = 0; d1 < this.avgWeekDayLoad[d0].length; d1++) {
                    double factor = (this.avgWeekDayLoadCounter[d0][d1] / 60.0) * PhysicalConstants.factor_wsToKWh;
                    this.avgWeekDayLoad[d0][d1] /= factor;
                }
            }

            for (int d0 = 0; d0 < this.avgDayLoad.length; d0++) {
                double factor = (this.avgDayLoadCounter[d0] / 86400.0) * PhysicalConstants.factor_wsToKWh;
                this.avgDayLoad[d0] /= factor;
            }

            this.getDriverRegistry().publish(ThermalLoggingObject.class, new ThermalLoggingObject(this.getUUID(),
                    this.getTimeDriver().getCurrentTime(), this.avgWeekDayLoad, this.avgDayLoad, this.hotWaterType));
        }
    }


    protected abstract double getGeneralCorrection();

    protected abstract double getDayOfWeekCorrection(int convertUnixTime2CorrectedWeekdayInt);

    protected abstract double getMonthlyCorrection(int convertUnixTime2MonthInt);

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
