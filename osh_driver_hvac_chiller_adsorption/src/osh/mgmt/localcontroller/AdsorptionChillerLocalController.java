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
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.ChillerControllerExchange;
import osh.mgmt.ipp.ChillerIPP;
import osh.mgmt.mox.AdsorptionChillerMOX;
import osh.registry.interfaces.IDataRegistryListener;
import osh.utils.time.TimeUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class AdsorptionChillerLocalController
        extends LocalController
        implements IDataRegistryListener {

    private static final Duration NEW_IPP_AFTER = Duration.ofMinutes(30); //30 min
    // static values / constants
    private final Duration RESCHEDULE_AFTER = Duration.ofHours(4); // 4 hours
    private final double minColdWaterTemp = 10.0; // [°C]
    private final double maxColdWaterTemp = 15.0;    // [°C]
    private final double hysteresis = 1.0;

    private final double minHotWaterTemp = 55.0; // [°C]
    private final double maxHotWaterTemp = 80.0; // [°C]

    // scheduling
    private ZonedDateTime lastTimeReschedulingTriggered;
    private ZonedDateTime lastTimeIppSent;

    private List<Activation> startTimes;
    private Activation currentActivation;

    // current values
    private double currentHotWaterTemperature = Double.MIN_VALUE;
    private double currentColdWaterTemperature = Double.MIN_VALUE;
    private boolean currentState;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public AdsorptionChillerLocalController(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        this.getOCRegistry().subscribe(EASolutionCommandExchange.class, this.getUUID(), this);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        final ZonedDateTime now = exchange.getTime();

        // get new Mox
        AdsorptionChillerMOX mox = (AdsorptionChillerMOX) this.getDataFromLocalObserver();
        double tempGradient = (mox.getColdWaterTemperature() - this.currentColdWaterTemperature) / 1.0;
        this.currentColdWaterTemperature = mox.getColdWaterTemperature();
        this.currentHotWaterTemperature = mox.getHotWaterTemperature();
        this.currentState = mox.isRunning();

        this.compressionType = mox.getCompressionType();
        this.compressionValue = mox.getCompressionValue();

        Map<Long, Double> temperaturePrediction = mox.getTemperatureMap();

        if (exchange.getTimeEvents().contains(TimeSubscribeEnum.HOUR)) {
            this.getGlobalLogger().logDebug("Cold Water Temperature: " + this.currentColdWaterTemperature);
            this.getGlobalLogger().logDebug("Hot Water Temperature : " + this.currentHotWaterTemperature);
        }


        //build CX
        ChillerControllerExchange cx = null;

        if (this.currentHotWaterTemperature < this.minHotWaterTemp) {
            this.createNewEaPart(
                    false,
                    temperaturePrediction,
                    now,
                    false,
                    0);

            //TURN OFF Adsorption Chiller
            cx = new ChillerControllerExchange(
                    this.getUUID(),
                    now,
                    true,
                    false,
                    0);
        } else if (this.currentColdWaterTemperature < this.minColdWaterTemp) {

            this.createNewEaPart(
                    false,
                    temperaturePrediction,
                    now,
                    this.currentState,
                    0);

            //TURN OFF Adsorption Chiller
            cx = new ChillerControllerExchange(
                    this.getUUID(),
                    now,
                    true,
                    false,
                    0);
        } else if (this.currentState
                && this.currentColdWaterTemperature < this.maxColdWaterTemp
                && this.currentColdWaterTemperature > this.maxColdWaterTemp - this.hysteresis) {
            // keep on running...

            // remove old start times... (sanity)
            while (this.startTimes != null
                    && !this.startTimes.isEmpty()
                    && now.isBefore(this.startTimes.get(0).startTime.plus(this.startTimes.get(0).duration))) {
                this.startTimes.remove(0);
            }

            int expectedRunningTime = (int) (
                    (this.currentColdWaterTemperature - this.maxColdWaterTemp + this.hysteresis)
                            / tempGradient);

            this.createNewEaPart(
                    true,
                    temperaturePrediction,
                    now,
                    false,
                    expectedRunningTime);

            //TURN ON Adsorption Chiller
            cx = new ChillerControllerExchange(
                    this.getUUID(),
                    now,
                    false,
                    true,
                    expectedRunningTime);
        } else if (this.currentColdWaterTemperature > this.maxColdWaterTemp
                && !this.currentState) {

            // remove old start times... (sanity)
            while (this.startTimes != null
                    && !this.startTimes.isEmpty()
                    && now.isBefore(this.startTimes.get(0).startTime.plus(this.startTimes.get(0).duration))) {
                this.startTimes.remove(0);
            }

            int expectedRunningTime = (int) (
                    (this.currentColdWaterTemperature - this.maxColdWaterTemp + this.hysteresis)
                            / tempGradient);

            //CHECK WHETER MIN AND MAX TEMPERATURE IS VALID
            if (this.currentHotWaterTemperature <= this.maxHotWaterTemp
                    && this.currentHotWaterTemperature >= this.minHotWaterTemp) {

                this.createNewEaPart(
                        true,
                        temperaturePrediction,
                        now,
                        true,
                        expectedRunningTime);

                //TURN ON Adsorption Chiller
                cx = new ChillerControllerExchange(
                        this.getUUID(),
                        now,
                        false,
                        true,
                        expectedRunningTime);
            }
        } else {
            // check whether to reschedule...
            if (!now.isAfter(this.lastTimeReschedulingTriggered.plus(this.RESCHEDULE_AFTER))) {
                this.createNewEaPart(
                        this.currentState,
                        temperaturePrediction,
                        now,
                        true,
                        0);
            } else if (!now.isAfter(this.lastTimeIppSent.plus(NEW_IPP_AFTER))) {
                this.createNewEaPart(
                        this.currentState,
                        temperaturePrediction,
                        now,
                        false,
                        0);
            }

            if (this.startTimes == null
                    || (this.startTimes.isEmpty() && this.currentActivation == null)
                    || (this.currentActivation != null && now.isBefore(this.currentActivation.startTime.plus(this.currentActivation.duration)))) {
                cx = new ChillerControllerExchange(
                        this.getUUID(),
                        now,
                        false,
                        false,
                        0);
                this.currentActivation = null;
            } else if (!this.startTimes.isEmpty()
                    && TimeUtils.isBeforeEquals(this.startTimes.get(0).startTime, now)) {
                // set on

                // remove old start times... (sanity)
                while (this.startTimes != null
                        && !this.startTimes.isEmpty()
                        && now.isBefore(this.startTimes.get(0).startTime.plus(this.startTimes.get(0).duration))) {
                    this.startTimes.remove(0);
                }

//				try {
//					starttimes.get(0);
//				}
//				catch (Exception e) {
//					@SuppressWarnings("unused")
//					int debug = 0;
//				}

                int scheduledRuntime = 0;

                //turn on
                if (this.startTimes == null || this.startTimes.isEmpty() || this.startTimes.get(0) == null) {
                    this.getGlobalLogger().logError("starttimes.get(0) == null)");
//					System.exit(0);
                } else {
                    this.currentActivation = this.startTimes.get(0);
                    this.startTimes.remove(0);
                    cx = new ChillerControllerExchange(
                            this.getUUID(),
                            now,
                            false,
                            true,
                            0);
                }
            } else if (this.currentActivation == null) {
                // no CHP is required...but do not send new CX (with starting times)
                @SuppressWarnings("unused")
                int debug = 0;
            } else {
                //should not happen..
//				throw new OSHException("BAD!");
//				getGlobalLogger().logDebug("nothing to do..");
            }
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
            this.lastTimeReschedulingTriggered = this.getTimeDriver().getCurrentTime();
        }
    }

    private void createNewEaPart(
            boolean currentState,
            Map<Long, Double> temperaturePrediction,
            ZonedDateTime now,
            boolean toBeScheduled,
            long expectedRunningTime) {

        if (toBeScheduled) {
            System.out.println("NO");
        }

        ChillerIPP ex;

        ex = new ChillerIPP(
                this.getUUID(),
                this.getGlobalLogger(),
                now,
                toBeScheduled,
                currentState,
                temperaturePrediction,
                this.compressionType,
                this.compressionValue);

        this.getOCRegistry().publish(
                InterdependentProblemPart.class, this.getUUID(), ex);
        this.lastTimeIppSent = this.getTimeDriver().getCurrentTime();
        if (toBeScheduled) {
            this.lastTimeReschedulingTriggered = this.getTimeDriver().getCurrentTime();
        }
    }
}