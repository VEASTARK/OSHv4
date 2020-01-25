package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.time.Activation;
import osh.datatypes.time.ActivationList;
import osh.driver.chp.ChpOperationMode;
import osh.driver.chp.model.GenericChpModel;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.ChpControllerExchange;
import osh.mgmt.ipp.DachsChpIPP;
import osh.mgmt.mox.DachsChpMOX;
import osh.registry.interfaces.IDataRegistryListener;
import osh.utils.physics.ComplexPowerUtil;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Ingo Mauser, Sebastian Kramer, Jan Mueller
 */
public class DachsChpLocalController
        extends LocalController
        implements IDataRegistryListener {

    // quasi static values
    private ChpOperationMode operationMode = ChpOperationMode.UNKNOWN;
    private int typicalActivePower = Integer.MIN_VALUE;
    private int typicalReactivePower = Integer.MIN_VALUE;
    private int typicalGasPower = Integer.MIN_VALUE;
    private int typicalThermalPower = Integer.MIN_VALUE;
    private int rescheduleAfter;
    private long newIPPAfter;
    private int relativeHorizonIPP;
    private double currentHotWaterStorageMinTemp;
    private double currentHotWaterStorageMaxTemp;
    private double forcedOnHysteresis;

    private double fixedCostPerStart;
    private double forcedOnOffStepMultiplier;
    private int forcedOffAdditionalCost;
    private double chpOnCervisiaStepSizeMultiplier;
    private int minRuntime;
    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    // ### variables ###

    // scheduling
    private long lastTimeReschedulingTriggered;
    private long lastTimeIppSent;

    private List<Activation> startTimes;
    private Activation currentActivation;

    // current values
    private double currentWaterTemperature = Double.MIN_VALUE;
    private boolean currentState;
    private boolean lastState;
    private long runningSince;
    private long stoppedSince;
    private int lastThermalPower;
    private int currentRemainingRunningTime;
    private int currentActivePower = Integer.MIN_VALUE;
    private int currentReactivePower = Integer.MIN_VALUE;
    private int currentThermalPower = Integer.MIN_VALUE;
    private int currentGasPower = Integer.MIN_VALUE;

    //TODO move to config file
    private int keepAliveTime;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public DachsChpLocalController(IOSHOC osh) {
        super(osh);

    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

//		// init before registering for timer messages
//		createNewEaPart(false, 0);

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        this.getOCRegistry().subscribe(EASolutionCommandExchange.class, this.getUUID(), this);

        ZonedDateTime start = this.getTimeDriver().getTimeAtStart();
        this.lastTimeIppSent = start.minusDays(1).toEpochSecond();
        this.lastTimeReschedulingTriggered = start.minusDays(1).toEpochSecond();

        if (this.getOSH().getOSHStatus().isSimulation()) {
            this.keepAliveTime = 0;
        } else {
            this.keepAliveTime = 10 * 60;
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        final long now = exchange.getEpochSecond();

//        this.getGlobalLogger().logDebug("controller called");

        // get new Mox
        DachsChpMOX mox = (DachsChpMOX) this.getDataFromLocalObserver();
        double tempGradient = (mox.getWaterTemperature() - this.currentWaterTemperature) / 1.0; // Kelvin/sec
        this.currentWaterTemperature = mox.getWaterTemperature();
        this.currentState = mox.isRunning();
        this.currentActivePower = mox.getActivePower();
        this.currentThermalPower = mox.getThermalPower();
        this.currentGasPower = mox.getGasPower();
        this.currentRemainingRunningTime = mox.getRemainingRunningTime();

        this.typicalActivePower = mox.getTypicalActivePower();
        this.typicalReactivePower = mox.getTypicalReactivePower();
        this.typicalGasPower = mox.getTypicalGasPower();
        this.typicalThermalPower = mox.getTypicalThermalPower();

        this.rescheduleAfter = mox.getRescheduleAfter();
        this.newIPPAfter = mox.getNewIPPAfter();
        this.relativeHorizonIPP = mox.getRelativeHorizonIPP();
        this.currentHotWaterStorageMinTemp = mox.getCurrentHotWaterStorageMinTemp();
        this.currentHotWaterStorageMaxTemp = mox.getCurrentHotWaterStorageMaxTemp();
        this.forcedOnHysteresis = mox.getForcedOnHysteresis();

        this.fixedCostPerStart = mox.getFixedCostPerStart();
        this.forcedOnOffStepMultiplier = mox.getForcedOnOffStepMultiplier();
        this.forcedOffAdditionalCost = mox.getForcedOffAdditionalCost();
        this.chpOnCervisiaStepSizeMultiplier = mox.getChpOnCervisiaStepSizeMultiplier();
        this.minRuntime = mox.getMinRuntime();

        this.compressionType = mox.getCompressionType();
        this.compressionValue = mox.getCompressionValue();

        if (this.currentState)
            this.lastThermalPower = this.currentThermalPower;

        // do control...
        ChpControllerExchange cx = null;

        if (this.currentWaterTemperature <= this.currentHotWaterStorageMinTemp) {
            // calculate expected running time (currently not used, legacy code)
            int expectedRunningTime = (int) (
                    (this.currentHotWaterStorageMinTemp - this.currentWaterTemperature + this.forcedOnHysteresis)
                            / tempGradient);

            // remove old start times... (sanity)
            while (this.startTimes != null
                    && !this.startTimes.isEmpty()
                    && this.startTimes.get(0).startTime + this.startTimes.get(0).duration < now) {
                this.startTimes.remove(0);
            }

            //update information before sending IPP
            if (!this.currentState) {
//				System.out.println("[" + now + "] Forced running request");
                this.runningSince = now;
                this.getGlobalLogger().logDebug("CHP forced on at " + now);
            }

            this.createNewEaPart(
                    true, //ON
                    this.typicalActivePower,
                    this.typicalReactivePower,
                    this.typicalThermalPower,
                    this.typicalGasPower,
                    now,
                    !this.currentState, // toBeScheduled // should be true
                    expectedRunningTime);

            // force on
            cx = new ChpControllerExchange(
                    this.getUUID(),
                    now,
                    false,
                    false,
                    true, //ON
                    expectedRunningTime);
        } else if (this.currentWaterTemperature > this.currentHotWaterStorageMinTemp
                && this.currentWaterTemperature <= this.currentHotWaterStorageMinTemp + this.forcedOnHysteresis
                && this.currentState) {
            //is on...well...stay at least on until a certain temperature is reached...

            // remove old start times... (sanity)
            while (this.startTimes != null
                    && !this.startTimes.isEmpty()
                    && this.startTimes.get(0).startTime + this.startTimes.get(0).duration < now) {
                this.startTimes.remove(0);
            }

            // expectedRunningTime = minimum running time
            int expectedRunningTime = (int) (
                    (this.currentHotWaterStorageMinTemp + this.forcedOnHysteresis - this.currentWaterTemperature)
                            / tempGradient);

            this.createNewEaPart(
                    true, //ON
                    this.typicalActivePower,
                    this.typicalReactivePower,
                    this.typicalThermalPower,
                    this.typicalGasPower,
                    now,
                    false, // toBeScheduled
                    expectedRunningTime);

            // force on (still...CHP should be running anyway)
            cx = new ChpControllerExchange(
                    this.getUUID(),
                    now,
                    false,
                    false,
                    true,
                    expectedRunningTime);

        } else if ((this.currentWaterTemperature > this.currentHotWaterStorageMinTemp
                && this.currentWaterTemperature <= this.currentHotWaterStorageMinTemp + this.forcedOnHysteresis
                && !this.currentState)
                || (this.currentWaterTemperature > this.currentHotWaterStorageMinTemp + this.forcedOnHysteresis
                && this.currentWaterTemperature <= this.currentHotWaterStorageMaxTemp)) {
            // it's in the normal temperature zone

            // remove old start times... (sanity)
            while (this.startTimes != null
                    && !this.startTimes.isEmpty()
                    && this.startTimes.get(0).startTime + this.startTimes.get(0).duration < now) {
                this.startTimes.remove(0);
            }

            // check whether to reschedule...
            long diff = now - this.lastTimeReschedulingTriggered;
            long ipp_diff = now - this.lastTimeIppSent;
            //don't reschedule too often let's wait first for the solution
            if (diff >= this.rescheduleAfter && ipp_diff > 10) {
                // force rescheduling
                this.createNewEaPart(
                        this.currentState,
                        this.typicalActivePower,
                        this.typicalReactivePower,
                        this.typicalThermalPower,
                        this.typicalGasPower,
                        now,
                        true, // toBeScheduled
                        this.currentRemainingRunningTime);
            } else if (ipp_diff >= this.newIPPAfter) {
                // just update...
                this.createNewEaPart(
                        this.currentState,
                        this.typicalActivePower,
                        this.typicalReactivePower,
                        this.typicalThermalPower,
                        this.typicalGasPower,
                        now,
                        false, // toBeScheduled
                        this.currentRemainingRunningTime);
            }

            if (this.startTimes != null
                    && !this.startTimes.isEmpty()
                    && this.startTimes.get(0).startTime <= now) {
                // switch on

                int scheduledRuntime;

                // switch on
                if (this.startTimes.get(0) == null) {
                    this.getGlobalLogger().logError("starttimes == null || starttimes.isEmpty() || starttimes.get(0) == null -> " + (this.startTimes != null ? this.startTimes.size() : null));
                } else {
                    scheduledRuntime = (int) ((this.startTimes.get(0).startTime + this.startTimes.get(0).duration) - now);
                    this.currentActivation = this.startTimes.get(0);
                    long scheduledDuration = this.startTimes.get(0).duration;
                    this.startTimes.remove(0);

                    //update Information before sending IPP
                    if (!this.currentState) {
//						System.out.println("[" + now + "] Scheduled start");
                        this.getGlobalLogger().logDebug("CHP scheduled start at " + now + " for: " + scheduledDuration);
                        this.runningSince = now;
                    }

                    this.createNewEaPart(
                            true,
                            this.typicalActivePower,
                            this.typicalReactivePower,
                            this.typicalThermalPower,
                            this.typicalGasPower,
                            now,
                            false,
                            scheduledRuntime);

                    cx = new ChpControllerExchange(
                            this.getUUID(),
                            now,
                            false,
                            false,
                            true,
                            scheduledRuntime);
                }
            } else if ((this.currentActivation != null && this.currentActivation.startTime + this.currentActivation.duration - this.keepAliveTime < now)
                    || (this.currentActivation == null && this.startTimes == null)
                    || (this.currentActivation == null && this.startTimes.isEmpty())
                    || (this.currentActivation == null && this.startTimes.get(0).startTime > now)) {
                // switch off (has only been on because of forced on or scheduled runtime is over)
                cx = new ChpControllerExchange(
                        this.getUUID(),
                        now,
                        false,
                        false,
                        false,
                        0);
                this.currentActivation = null;

                if (this.currentState) {
//					System.out.println("[" + now + "] Scheduled stop");
                    this.getGlobalLogger().logDebug("CHP scheduled stop at " + now);
                    this.stoppedSince = now;
                }

                if (this.currentState) {
                    //send IPP because of switch off
                    this.createNewEaPart(
                            false,
                            this.typicalActivePower,
                            this.typicalReactivePower,
                            this.typicalThermalPower,
                            this.typicalGasPower,
                            now,
                            false,
                            0);
                }
            }  //Nothing to do... (current activation is active)

        } else if (this.currentWaterTemperature > this.currentHotWaterStorageMaxTemp) {
//			int expectedRunningTime = (int) ((currentWaterTemperature - currentHotWaterStorageMaxTemp) / tempGradient);

            // remove old start times... (sanity)
            while (this.startTimes != null
                    && !this.startTimes.isEmpty()
                    && this.startTimes.get(0).startTime + this.startTimes.get(0).duration < now) {
                this.startTimes.remove(0);
            }

            //update information before sending IPP
            if (this.currentState) {
//				System.out.println("[" + now + "] forced stop");
                this.getGlobalLogger().logDebug("CHP forced off at " + now);
                this.stoppedSince = now;
            }

            this.createNewEaPart(
                    false,
                    this.typicalActivePower,
                    this.typicalReactivePower,
                    this.typicalThermalPower,
                    this.typicalGasPower,
                    now,
                    this.currentState, // toBeScheduled
                    0);

            // force off
            cx = new ChpControllerExchange(
                    this.getUUID(),
                    now,
                    true,
                    false,
                    false,
                    0);
        } else {
            this.getGlobalLogger().logError("SHOULD NEVER HAPPEN");
        }

        if (cx != null) {
            this.updateOcDataSubscriber(cx);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof EASolutionCommandExchange) {
            EASolutionCommandExchange<ActivationList> exs = ((EASolutionCommandExchange<ActivationList>) exchange);
            this.startTimes = exs.getPhenotype().getList();

            long now = this.getTimeDriver().getCurrentEpochSecond();

            //check if currently running and should be shutdown by the optimization
            if (this.currentActivation != null) {
                if (this.startTimes != null && !this.startTimes.isEmpty() && this.startTimes.get(0).startTime < now && this.startTimes.get(0).startTime + this.startTimes.get(0).duration > now) {
                    this.currentActivation = this.startTimes.get(0);
                    this.startTimes.remove(0);
                } else {
                    this.currentActivation = null;
                }
            }

            StringBuilder builder = new StringBuilder();

            builder.append("starttimes: {");
            boolean first = true;
            for (Activation a : this.startTimes) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(a.startTime).append(" for ").append(a.duration);
                first = false;
            }
            builder.append("}");

            this.getGlobalLogger().logDebug(builder.toString());

            // scheduling has been triggered by some other device...
            this.lastTimeReschedulingTriggered = exs.getTimestamp();
        }
    }


    private void createNewEaPart(
            boolean currentState,
            int typicalActivePower,
            int typicalReactivePower,
            int typicalThermalPower,
            int typicalGasPower,
            long now,
            boolean toBeScheduled,
            long remainingRunningTime) {

        DachsChpIPP ex;
//		long remaining = remainingRunningTime;
//		if (currentState == false) {
//			remaining = 0; // CHP was shut down because the water is too hot, no remaining time
//		}

        GenericChpModel chpModel = null;
        try {
            chpModel = new GenericChpModel(
                    typicalActivePower,
                    typicalReactivePower,
                    typicalThermalPower,
                    typicalGasPower,
                    ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(typicalActivePower, typicalReactivePower), currentState,
                    this.lastThermalPower,
                    this.runningSince,
                    this.stoppedSince);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ex = new DachsChpIPP(
                this.getUUID(),
                this.getGlobalLogger(),
                now,
                toBeScheduled,
                currentState,
                this.minRuntime,
                chpModel,
//				- Math.abs(typicalActivePower),
//				Math.abs(typicalGasPower),
                this.relativeHorizonIPP,
                this.currentHotWaterStorageMinTemp,
                this.currentHotWaterStorageMaxTemp,
                this.forcedOnHysteresis,
                70.0,
                this.fixedCostPerStart,
                this.forcedOnOffStepMultiplier,
                this.forcedOffAdditionalCost,
                this.chpOnCervisiaStepSizeMultiplier,
                this.compressionType,
                this.compressionValue); //initial tank temperature for optimization
        this.lastTimeIppSent = now;
        this.getOCRegistry().publish(
                InterdependentProblemPart.class, this.getUUID(), ex);


        // IMPORTANT: update of variable is done via onQueueEventTypeReceived()
//		if ( toBeScheduled ) {
//			this.lastTimeReschedulingTriggered = now;
//		}
    }
}
