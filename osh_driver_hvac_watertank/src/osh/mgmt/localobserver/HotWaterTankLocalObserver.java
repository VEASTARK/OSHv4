package osh.mgmt.localobserver;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.ea.TemperaturePrediction;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.HotWaterTankObserverExchange;
import osh.mgmt.ipp.HotWaterTankNonControllableIPP;
import osh.registry.interfaces.IDataRegistryListener;
import osh.utils.physics.PhysicalConstants;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Ingo Mauser
 */
public class HotWaterTankLocalObserver
        extends WaterTankLocalObserver
        implements IDataRegistryListener {

    private final double defaultPunishmentFactorPerWsPowerLost = 6.0 / PhysicalConstants.factor_wsToKWh;

    private double triggerIppIfDeltaTempBigger;
    private Duration newIppAfter;
    private double rescheduleIfViolatedTemperature;
    private Duration rescheduleIfViolatedDuration;
    private ZonedDateTime lastTimeIPPSent;
    private ZonedDateTime predictionViolatedSince;

    private Double lastKnownGasPrice;
    private double tankCapacity = 100;
    private double tankDiameter = 1.0;
    private double standingHeatLossFactor = 1.0;
    private double ambientTemperature = 20.0;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    TreeMap<Long, Double> temperaturePrediction = new TreeMap<>();

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public HotWaterTankLocalObserver(IOSHOC osh) {
        super(osh);

        this.currentMinTemperature = 60;
        this.currentMaxTemperature = 80;
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        if (this.newIppAfter != null && this.newIppAfter.toSeconds() % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }

        this.getOCRegistry().subscribe(EAPredictionCommandExchange.class, this.getUUID(),this);
        this.getOCRegistry().subscribe(EpsStateExchange.class, this.getUUID(),this);
    }


    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();
        long nowSeconds = exchange.getEpochSecond();

        //TODO: change to sending as soon as as lasttime+new_ipp_after is reached not the next tick when the next
        // backwards-compatibility breaking update is released
        if (now.isAfter(this.lastTimeIPPSent.plus(this.newIppAfter))) {
            HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
                    this.getUUID(),
                    now,
                    false,
                    this.currentTemperature,
                    this.tankCapacity,
                    this.tankDiameter,
                    this.standingHeatLossFactor,
                    this.ambientTemperature,
                    (this.lastKnownGasPrice == null ? this.defaultPunishmentFactorPerWsPowerLost :
                            (this.lastKnownGasPrice) / PhysicalConstants.factor_wsToKWh),
                    this.compressionType,
                    this.compressionValue);
            this.getOCRegistry().publish(
                    InterdependentProblemPart.class,
                    this,
                    ex);
            this.lastTimeIPPSent = now;
            this.temperatureInLastIPP = this.currentTemperature;
        } else if (exchange.getTimeEvents().contains(TimeSubscribeEnum.MINUTE) && this.temperaturePrediction != null) {
            Entry<Long, Double> predEntry = this.temperaturePrediction.floorEntry(nowSeconds);
            if (predEntry != null
                    && Math.abs(predEntry.getValue() - this.currentTemperature) > this.rescheduleIfViolatedTemperature
                    //if pred is too old don't pay attention to it
                    && (this.temperaturePrediction.ceilingEntry(nowSeconds) != null || (nowSeconds - predEntry.getKey()) < 3600)) {
                if (this.predictionViolatedSince != null && Duration.between(this.predictionViolatedSince, now).compareTo(this.rescheduleIfViolatedDuration) >= 0) {
                    this.getGlobalLogger().logDebug("Temperature prediction was wrong by >" + this.rescheduleIfViolatedTemperature
                            + " degree for " + this.rescheduleIfViolatedDuration.toSeconds() + " seconds, reschedule");
                    HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
                            this.getUUID(),
                            now,
                            true,
                            this.currentTemperature,
                            this.tankCapacity,
                            this.tankDiameter,
                            this.standingHeatLossFactor,
                            this.ambientTemperature,
                            (this.lastKnownGasPrice == null ? this.defaultPunishmentFactorPerWsPowerLost : (this.lastKnownGasPrice) / PhysicalConstants.factor_wsToKWh),
                            this.compressionType,
                            this.compressionValue);
                    this.getOCRegistry().publish(
                            InterdependentProblemPart.class,
                            this,
                            ex);
                    this.lastTimeIPPSent = now;
                    this.temperatureInLastIPP = this.currentTemperature;
                    this.predictionViolatedSince = null;
                } else if (this.predictionViolatedSince == null){
                    //this may seem counter-intuitive but we count the violation as beginning directly after the last
                    // time it was within the temperature borders, so it has been on-going for a minute now
                    this.predictionViolatedSince = now.minusMinutes(1);
                }
            } else {
                this.predictionViolatedSince = null;
            }
        }

    }


    @Override
    public void onDeviceStateUpdate() {

//		getGlobalLogger().logDebug("state changed at: " + getTimer().getUnixTime());

        // receive new state from driver
        IHALExchange _ihal = this.getObserverDataObject();

        if (_ihal instanceof HotWaterTankObserverExchange) {
            HotWaterTankObserverExchange ox = (HotWaterTankObserverExchange) _ihal;

            this.currentTemperature = ox.getTopTemperature();

            this.tankCapacity = ox.getTankCapacity();
            this.tankDiameter = ox.getTankDiameter();
            this.ambientTemperature = ox.getAmbientTemperature();
            this.standingHeatLossFactor = ox.getStandingHeatLossFactor();
            this.rescheduleIfViolatedTemperature = ox.getRescheduleIfViolatedTemperature();

            if (!ox.getRescheduleIfViolatedDuration().equals(this.rescheduleIfViolatedDuration)) {
                this.predictionViolatedSince = null;
                this.rescheduleIfViolatedDuration = ox.getRescheduleIfViolatedDuration();
            }

            if (ox.isSendNewIpp() || Math.abs(this.temperatureInLastIPP - this.currentTemperature) >= this.triggerIppIfDeltaTempBigger) {

                HotWaterTankNonControllableIPP ex;
                ex = new HotWaterTankNonControllableIPP(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentTime(),
                        ox.isForceRescheduling(),
                        this.currentTemperature,
                        this.tankCapacity,
                        this.tankDiameter,
                        this.standingHeatLossFactor,
                        this.ambientTemperature,
                        (this.lastKnownGasPrice == null ? this.defaultPunishmentFactorPerWsPowerLost : (this.lastKnownGasPrice) / PhysicalConstants.factor_wsToKWh),
                        this.compressionType,
                        this.compressionValue);
                this.getOCRegistry().publish(
                        InterdependentProblemPart.class,
                        this,
                        ex);
                this.lastTimeIPPSent = this.getTimeDriver().getCurrentTime();
                this.temperatureInLastIPP = this.currentTemperature;
            }

            // save current state in OCRegistry (for e.g. GUI)
            WaterStorageOCSX sx = new WaterStorageOCSX(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentTime(),
                    this.currentTemperature,
                    this.currentMinTemperature,
                    this.currentMaxTemperature,
                    ox.getHotWaterDemand(),
                    ox.getHotWaterSupply(),
                    this.getUUID());
            this.getOCRegistry().publish(
                    WaterStorageOCSX.class,
                    this,
                    sx);
        } else if (_ihal instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        } else if (_ihal instanceof IPPSchedulingExchange) {
            IPPSchedulingExchange _ise = (IPPSchedulingExchange) _ihal;
            this.newIppAfter = _ise.getNewIppAfter();
            this.triggerIppIfDeltaTempBigger = _ise.getTriggerIfDeltaX();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof EpsStateExchange) {
            EpsStateExchange eee = (EpsStateExchange) exchange;

            long now = this.getTimeDriver().getCurrentEpochSecond();
            double firstPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(now);
            double lastPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(
                    eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPriceUnknownAtAndAfter() - 1);

            this.lastKnownGasPrice = (firstPrice + lastPrice) / 2.0;
        }

        if (exchange instanceof EAPredictionCommandExchange) {
            EAPredictionCommandExchange<TemperaturePrediction> exs = ((EAPredictionCommandExchange<TemperaturePrediction>) exchange);
            this.temperaturePrediction = exs.getPrediction().getTemperatureStates();
        }
    }

}
