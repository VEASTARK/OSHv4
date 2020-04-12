package osh.comdriver.logging;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.cal.ObjectWrapperCALExchange;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.logging.LoggingConfigurationStateExchange;
import osh.datatypes.logging.electrical.DetailedPowerLogObject;
import osh.datatypes.logging.electrical.H0LogObject;
import osh.datatypes.logging.general.PowerLimitSignalLogObject;
import osh.datatypes.logging.general.PriceSignalLogObject;
import osh.datatypes.logging.general.SimulationResultsLogObject;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.details.energy.AncillaryMeterStateExchange;
import osh.datatypes.registry.oc.details.energy.CostConfigurationStateExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.registry.interfaces.IDataRegistryListener;
import osh.simulation.OSHSimulationResults;
import osh.utils.CostReturnType;
import osh.utils.CostReturnType.SingleStepCostReturnType;
import osh.utils.costs.RegularCostFunction;
import osh.utils.dataStructures.Enum2DoubleMap;
import osh.utils.physics.PhysicalConstants;
import osh.utils.time.TimeConversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;

/**
 * Represents a comdriver that handles logging all the relevant energy-information.
 *
 * @author Sebastian Kramer
 */
public class EnergyLoggingComDriver extends CALComDriver implements IDataRegistryListener {

    private final OSHSimulationResults simulationResults = new OSHSimulationResults();
    private ZonedDateTime lastLoggedMeterState;

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimits;

    //logging intervals
    private boolean logToDatabase;
    private List<Long[]> loggingIntervals;
    private ZonedDateTime[] timestampForInterval;
    private long[] relativeIntervalStart;
    private boolean logDetailedPower;
    private boolean logEpsPls;
    private boolean logH0;
    private boolean logIntervals;

    private OSHSimulationResults[] intervalResults;

    //saved power
    private AncillaryCommodityLoadProfile loadProfile;

    private EnumMap<AncillaryCommodity, PriceSignal> loggingPriceSignals;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> loggingPowerLimits;
    private boolean epsPlsChanged = false;

    //saved array for H0
    private double[][][] aggrH0ResultsWeekdays;
    private int[][] h0ResultsCounter;
    private double[][] aggrH0ResultsDays;
    private int[] h0ResultsCounterDays;
    private double aggrActiveConsumption = 0.0;
    private double aggrReactiveConsumption = 0.0;

    private long simStart;

    private RegularCostFunction costFunction = null;

    /**
     * Constructs this comdriver with the given OSH entitiy, the unique identifier and the parameter configuration.
     *
     * @param entity the OSH entitiy
     * @param deviceID the unique identifier
     * @param driverConfig the parameter configuration
     */
    public EnergyLoggingComDriver(
            IOSH entity, UUID deviceID, OSHParameterCollection driverConfig) {
        super(entity, deviceID, driverConfig);
    }

    /**
     * Based on the received logging configuration this will prepare the data storage objects to confirm to this.
     */
    private void prepareLoggingObjects() {

        if (this.logH0) {
            int daysOfYear = TimeConversion.getNumberOfDaysInYearFromTime(this.getTimeDriver().getCurrentTime());
            this.aggrH0ResultsWeekdays = new double[7][1440][2];
            this.h0ResultsCounter = new int[7][1440];
            this.aggrH0ResultsDays = new double[daysOfYear][2];
            this.h0ResultsCounterDays = new int[daysOfYear];

            for (int i = 0; i < this.aggrH0ResultsWeekdays.length; i++) {
                for (int j = 0; j < this.aggrH0ResultsWeekdays[i].length; j++) {
                    this.h0ResultsCounter[i][j] = 0;
                    Arrays.fill(this.aggrH0ResultsWeekdays[i][j], 0.0);
                }
            }
            for (int i = 0; i < this.aggrH0ResultsDays.length; i++) {
                Arrays.fill(this.aggrH0ResultsDays[i], 0.0);
                this.h0ResultsCounterDays[i] = 0;
            }
        }

        if (this.logIntervals) {
            this.intervalResults = new OSHSimulationResults[this.loggingIntervals.size()];
            this.timestampForInterval = new ZonedDateTime[this.loggingIntervals.size()];
            this.relativeIntervalStart = new long[this.loggingIntervals.size()];
            for (int i = 0; i < this.loggingIntervals.size(); i++) {
                this.intervalResults[i] = new OSHSimulationResults();
                this.timestampForInterval[i] = null;
                this.relativeIntervalStart[i] = 0;
            }
        }

        if (this.logEpsPls) {
            this.loggingPriceSignals = new EnumMap<>(AncillaryCommodity.class);
            this.loggingPowerLimits = new EnumMap<>(AncillaryCommodity.class);
        }

        if (this.logDetailedPower) {
            this.loadProfile = new AncillaryCommodityLoadProfile();
        }

        this.lastLoggedMeterState = this.getTimeDriver().getTimeAtStart();
    }

    @Override
    public void onSystemIsUp() {
        this.getComRegistry().subscribe(AncillaryMeterStateExchange.class, this);
        this.getComRegistry().subscribe(LoggingConfigurationStateExchange.class, this);
    }

    @Override
    public void onSystemRunning() {
        this.simStart = System.currentTimeMillis();
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        long simFinish = System.currentTimeMillis();
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        if (this.logToDatabase) {
            if (this.loggingIntervals != null && this.logIntervals) {
                PriceSignalLogObject pslo = new PriceSignalLogObject(this.getUUID(), now, this.loggingPriceSignals);
                PowerLimitSignalLogObject plslo = new PowerLimitSignalLogObject(this.getUUID(), now, this.loggingPowerLimits);

                this.getComRegistry().publish(PriceSignalLogObject.class, this.getUUID(), pslo);
                this.getComRegistry().publish(PowerLimitSignalLogObject.class, this.getUUID(), plslo);
            }
            if (this.logH0) {
                double[][][] avgWeekDays = new double[7][1440][2];
                double[][] avgDays = new double[365][2];

                for (int i = 0; i < avgWeekDays.length; i++) {
                    for (int j = 0; j < avgWeekDays[i].length; j++) {
                        double factor = (double) this.h0ResultsCounter[i][j] * PhysicalConstants.factor_wsToKWh;
                        for (int k = 0; k < avgWeekDays[i][j].length; k++) {
                            avgWeekDays[i][j][k] = this.aggrH0ResultsWeekdays[i][j][k] / factor;
                        }
                    }
                }
                for (int i = 0; i < avgDays.length; i++) {
                    double factor = ((double) this.h0ResultsCounterDays[i] / 1440.0) * PhysicalConstants.factor_wsToKWh;
                    for (int j = 0; j < avgDays[i].length; j++) {
                        avgDays[i][j] = this.aggrH0ResultsDays[i][j] / factor;
                    }
                }
                H0LogObject h0lo = new H0LogObject(this.getUUID(), now, avgWeekDays, avgDays);
                this.getComRegistry().publish(H0LogObject.class, this.getUUID(), h0lo);
            }

            long simDuration = Duration.between(this.getTimeDriver().getTimeAtStart(), now).toSeconds();

            SimulationResultsLogObject srlo =
                    new SimulationResultsLogObject(this.getOSH().getOSHStatus().getHhUUID(),now,
                            this.simulationResults.clone(), 0, simDuration,
                            (System.currentTimeMillis() - this.simStart) / 1000);
            this.getComRegistry().publish(SimulationResultsLogObject.class, srlo);
        }


        String logDir = this.getOSH().getOSHStatus().getLogDir();
        if (!logDir.isEmpty()) {
            try {
                this.simulationResults.logCurrentStateToFile(new File(logDir + "/simResults.csv"),
                        (simFinish - this.simStart) / 1000);
            } catch (FileNotFoundException e) {
                throw new OSHException(e);
            }
        }
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        if (exchangeObject instanceof ObjectWrapperCALExchange) {
            Object data = ((ObjectWrapperCALExchange<?>) exchangeObject).getData();

            if (data instanceof CostConfigurationStateExchange) {
                this.costFunction = new RegularCostFunction(
                        ((CostConfigurationStateExchange) data).getCostConfigurationContainer(), this.priceSignals,
                        this.powerLimits);
            } else if (data instanceof EpsStateExchange) {
                this.priceSignals = ((EpsStateExchange) data).getPriceSignals();
                this.epsPlsChanged = true;
            } else if (data instanceof PlsStateExchange) {
                this.powerLimits = ((PlsStateExchange) data).getPowerLimitSignals();
                this.epsPlsChanged = true;
            }

            if (this.epsPlsChanged && this.costFunction != null) {
                this.costFunction.updateSignals(this.priceSignals, this.powerLimits);
            }
        }
    }

    @Override
    public <T extends AbstractExchange> void onExchange(final T exchange) {
        if (exchange instanceof AncillaryMeterStateExchange) {
            this.logMeterState(((AncillaryMeterStateExchange) exchange).getMeterState());
        } else if (exchange instanceof LoggingConfigurationStateExchange) {
            LoggingConfigurationStateExchange lcse = (LoggingConfigurationStateExchange) exchange;

            this.logToDatabase = lcse.isLogToDatabase();
            this.loggingIntervals = lcse.getLoggingIntervals();
            this.logDetailedPower = lcse.isLogDetailedPower();
            this.logEpsPls = lcse.isLogEpsPls();
            this.logH0 = lcse.isLogH0();
            this.logIntervals = lcse.isLogIntervalls();

            this.prepareLoggingObjects();
        }
    }

    /**
     * Extracts cost and specified logging information from the received ancillary meter state.
     *
     * @param ancillaryMeterState the new ancillary meter state
     */
    private void logMeterState(AncillaryMeterState ancillaryMeterState) {
        if (ancillaryMeterState != null && this.priceSignals != null) {

            double currentActivePowerConsumption = 0;

            //extract specific power information from the meter
            double currentActivePowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
            if (currentActivePowerExternal > 0) {
                currentActivePowerConsumption = currentActivePowerExternal;
            }

            double currentActivePowerPvAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);
            double currentActivePowerPvFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN);

            double currentActivePowerPv = currentActivePowerPvFeedIn + currentActivePowerPvAutoConsumption;
            currentActivePowerConsumption += Math.abs(currentActivePowerPvAutoConsumption);


            double currentActivePowerChpAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);
            double currentActivePowerChpFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);

            double currentActivePowerChp = currentActivePowerChpFeedIn + currentActivePowerChpAutoConsumption;
            currentActivePowerConsumption += Math.abs(currentActivePowerChpAutoConsumption);


            double currentActivePowerBatteryCharging = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION);
            double currentActivePowerBatteryAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION);
            double currentActivePowerBatteryFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN);

            double currentActivePowerBatteryDischarging = currentActivePowerBatteryFeedIn + currentActivePowerBatteryAutoConsumption;

            //if battery is charging it's power is already contained in the activePowerExternal
            if (currentActivePowerBatteryAutoConsumption < 0)
                currentActivePowerConsumption += Math.abs(currentActivePowerBatteryAutoConsumption);

            double currentReactivePowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL);

            double currentGasPowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL);

            ZonedDateTime now = this.getTimeDriver().getCurrentTime();

            //calculate the costs since the last meter state received
            Enum2DoubleMap<CostReturnType.SingleStepCostReturnType> costMap = this.costFunction.calculateSingleStepCosts(
                    ancillaryMeterState, now.toEpochSecond(),
                    Duration.between(this.lastLoggedMeterState, now).toSeconds());

            this.lastLoggedMeterState = now;

            //save all this information to the simulation results
            this.simulationResults.addActivePowerConsumption(
                    currentActivePowerConsumption);

            this.simulationResults.addActivePowerPV(
                    currentActivePowerPv);
            this.simulationResults.addActivePowerPVAutoConsumption(
                    currentActivePowerPvAutoConsumption);
            this.simulationResults.addActivePowerPVFeedIn(
                    currentActivePowerPvFeedIn);


            this.simulationResults.addActivePowerCHP(
                    currentActivePowerChp);
            this.simulationResults.addActivePowerCHPAutoConsumption(
                    currentActivePowerChpAutoConsumption);
            this.simulationResults.addActivePowerCHPFeedIn(
                    currentActivePowerChpFeedIn);

            this.simulationResults.addActivePowerBatteryCharging(
                    currentActivePowerBatteryCharging);
            this.simulationResults.addActivePowerBatteryDischarging(
                    currentActivePowerBatteryDischarging);
            this.simulationResults.addActivePowerBatteryAutoConsumption(
                    currentActivePowerBatteryAutoConsumption);
            this.simulationResults.addActivePowerBatteryFeedIn(
                    currentActivePowerBatteryFeedIn);

            this.simulationResults.addActivePowerExternal(
                    currentActivePowerExternal);

            this.simulationResults.addReactivePowerExternal(
                    currentReactivePowerExternal);

            this.simulationResults.addGasPowerExternal(
                    currentGasPowerExternal);

            this.simulationResults.addEpsCostsToEpsCosts(costMap.get(SingleStepCostReturnType.EPS));
            this.simulationResults.addPlsCostsToPlsCosts(costMap.get(SingleStepCostReturnType.PLS));
            this.simulationResults.addCostsToTotalCosts(
                    costMap.get(SingleStepCostReturnType.EPS)
                            + costMap.get(SingleStepCostReturnType.PLS)
                            + costMap.get(SingleStepCostReturnType.GAS)
                            + costMap.get(SingleStepCostReturnType.FEED_IN_PV)
                            + costMap.get(SingleStepCostReturnType.FEED_IN_CHP)
                            + costMap.get(SingleStepCostReturnType.AUTO_CONSUMPTION));
            this.simulationResults.addGasCostsToGasCosts(costMap.get(SingleStepCostReturnType.GAS));
            this.simulationResults.addFeedInCostsToFeedInCostsPV(costMap.get(SingleStepCostReturnType.FEED_IN_PV));
            this.simulationResults.addFeedInCostsToFeedInCostsCHP(costMap.get(SingleStepCostReturnType.FEED_IN_CHP));
            this.simulationResults.addAutoConsumptionCostsToAutoConsumptionCosts(costMap.get(SingleStepCostReturnType.AUTO_CONSUMPTION));

            //check if we have to commit an interval logging
            if (this.logToDatabase && this.logIntervals) {
                //interval logging of values
                if (!this.loggingIntervals.isEmpty()) {
                    for (int i = 0; i < this.loggingIntervals.size(); i++) {
                        //set up the next timestamps at start of the simulation
                        if (this.timestampForInterval[i] == null) {
                            Long[] interval = this.loggingIntervals.get(i);
                            if (interval[0] > 0) {
                                this.timestampForInterval[i] =
                                        TimeConversion.getStartOfMonth(now.plusMonths(interval[0]));
                            } else if (interval[1] > 0) {
                                this.timestampForInterval[i] =
                                        TimeConversion.getStartOfWeek(now.plusWeeks(interval[1]));
                            } else {
                                this.timestampForInterval[i] =
                                        TimeConversion.getStartOfDay(now.plusDays(interval[2]));
                            }
                        } else if (!this.timestampForInterval[i].isAfter(now)) {
                            Long[] interval = this.loggingIntervals.get(i);
                            OSHSimulationResults newBase = this.simulationResults.clone();
                            OSHSimulationResults toLog = this.intervalResults[i];
                            this.intervalResults[i] = newBase;
                            toLog.generateDiffToOtherResult(this.simulationResults);
                            this.getComRegistry().publish(SimulationResultsLogObject.class,
                                    new SimulationResultsLogObject(this.getUUID(), now,
                                    toLog, this.relativeIntervalStart[i], now.toEpochSecond(), 0L));

                            this.relativeIntervalStart[i] = now.toEpochSecond() + 1;
                            if (interval[0] > 0) {
                                this.timestampForInterval[i] =
                                        TimeConversion.getStartOfMonth(now.plusMonths(interval[0]));
                            } else if (interval[1] > 0) {
                                this.timestampForInterval[i] =
                                        TimeConversion.getStartOfWeek(now.plusWeeks(interval[1]));
                            } else {
                                this.timestampForInterval[i] =
                                        TimeConversion.getStartOfDay(now.plusDays(interval[2]));
                            }
                        }
                    }
                }
            }

            if (this.logToDatabase && this.logEpsPls) {
                //new eps/pls, so save the past and update the saved one
                if (this.epsPlsChanged) {
                    //handle eps
                    Map<AncillaryCommodity, PriceSignal> toLogEps = new EnumMap<>(AncillaryCommodity.class);
                    for (Entry<AncillaryCommodity, PriceSignal> en : this.priceSignals.entrySet()) {
                        PriceSignal oldPs = this.loggingPriceSignals.get(en.getKey());

                        if (oldPs == null) {
                            this.loggingPriceSignals.put(en.getKey(), en.getValue().clone());
                        } else {
                            oldPs.extendAndOverride(en.getValue().clone());
                            //log past
                            toLogEps.put(en.getKey(), oldPs.cloneBefore(now.toEpochSecond()));
                            //only keep the future
                            PriceSignal nowAndFuture = oldPs.cloneAfter(now.toEpochSecond());
                            this.loggingPriceSignals.put(en.getKey(), nowAndFuture);
                        }
                    }
                    PriceSignalLogObject pslo = new PriceSignalLogObject(this.getUUID(), now, toLogEps);
                    this.getComRegistry().publish(PriceSignalLogObject.class, this.getUUID(), pslo);

                    //handle pls
                    Map<AncillaryCommodity, PowerLimitSignal> toLogPls = new EnumMap<>(AncillaryCommodity.class);
                    for (Entry<AncillaryCommodity, PowerLimitSignal> en : this.powerLimits.entrySet()) {
                        PowerLimitSignal oldPls = this.loggingPowerLimits.get(en.getKey());

                        if (oldPls == null) {
                            this.loggingPowerLimits.put(en.getKey(), en.getValue().clone());
                        } else {
                            oldPls.extendAndOverride(en.getValue().clone());
                            //log past
                            toLogPls.put(en.getKey(), oldPls.cloneBefore(now.toEpochSecond()));
                            //only keep the future
                            PowerLimitSignal nowAndFuture = oldPls.cloneAfter(now.toEpochSecond());
                            this.loggingPowerLimits.put(en.getKey(), nowAndFuture);
                        }
                    }
                    PowerLimitSignalLogObject plslo = new PowerLimitSignalLogObject(this.getUUID(), now, toLogPls);
                    this.getComRegistry().publish(PowerLimitSignalLogObject.class, this.getUUID(), plslo);

                    this.epsPlsChanged = false;
                }
            }

            if (this.logToDatabase && this.logDetailedPower) {

                this.loadProfile.setLoad(AncillaryCommodity.ACTIVEPOWEREXTERNAL, now.toEpochSecond(),
                        (int) Math.round(currentActivePowerExternal));
                this.loadProfile.setLoad(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, now.toEpochSecond(), (int) Math.round(currentActivePowerChpAutoConsumption));
                this.loadProfile.setLoad(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, now.toEpochSecond(), (int) Math.round
                        (currentActivePowerChpFeedIn));
                this.loadProfile.setLoad(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, now.toEpochSecond(), (int) Math.round(currentActivePowerPvAutoConsumption));
                this.loadProfile.setLoad(AncillaryCommodity.PVACTIVEPOWERFEEDIN, now.toEpochSecond(), (int) Math.round
                        (currentActivePowerPvFeedIn));

                if (this.getTimeDriver().getCurrentTimeEvents().contains(TimeSubscribeEnum.HALF_DAY)) {
                    AncillaryCommodityLoadProfile tmpLoadProfile = this.loadProfile;
                    this.loadProfile = tmpLoadProfile.cloneAfter(now.toEpochSecond());

                    tmpLoadProfile = tmpLoadProfile.getProfileWithoutDuplicateValues();
                    this.getComRegistry().publish(DetailedPowerLogObject.class, this.getUUID(),
                            new DetailedPowerLogObject(this.getUUID(), now, tmpLoadProfile));
                }
            }

            if (this.logToDatabase && this.logH0) {
                this.aggrActiveConsumption += currentActivePowerConsumption;
                this.aggrReactiveConsumption += currentReactivePowerExternal;

                if (this.getTimeDriver().getCurrentTimeEvents().contains(TimeSubscribeEnum.MINUTE)) {
                    int dOfWeek = TimeConversion.getCorrectedDayOfWeek(now);
                    int dOfYear = TimeConversion.getCorrectedDayOfYear(now);
                    this.aggrH0ResultsWeekdays[dOfWeek][now.getMinute() % 1440][0] += this.aggrActiveConsumption;
                    this.aggrH0ResultsWeekdays[dOfWeek][now.getMinute() % 1440][1] += this.aggrReactiveConsumption;
                    this.h0ResultsCounter[dOfWeek][now.getMinute() % 1440]++;
                    this.aggrH0ResultsDays[dOfYear % 365][0] += this.aggrActiveConsumption;
                    this.aggrH0ResultsDays[dOfYear % 365][1] += this.aggrReactiveConsumption;
                    this.h0ResultsCounterDays[dOfYear % 365]++;

                    this.aggrActiveConsumption = 0;
                    this.aggrReactiveConsumption = 0;
                }
            }
        }
    }
}
