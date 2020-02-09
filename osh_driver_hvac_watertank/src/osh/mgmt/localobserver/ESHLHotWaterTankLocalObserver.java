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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class ESHLHotWaterTankLocalObserver
        extends WaterTankLocalObserver
        implements IDataRegistryListener {

    private final double defaultPunishmentFactorPerWsPowerLost = 6.0 / 3600000.0;
    TreeMap<Long, Double> temperaturePrediction = new TreeMap<>();
    private Duration NEW_IPP_AFTER = Duration.ZERO;
    private double TRIGGER_IPP_IF_DELTA_TEMP_BIGGER;
    private ZonedDateTime lastTimeIPPSent;
    private double tankCapacity = 100;
    private double tankDiameter = 1.0;
    private double ambientTemperature = 20.0;
    private Double lastKnownGasPrice;
    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public ESHLHotWaterTankLocalObserver(IOSHOC osh) {
        super(osh);

        this.currentMinTemperature = 60;
        this.currentMaxTemperature = 80;
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        if (this.NEW_IPP_AFTER.toSeconds() % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }

        this.getOCRegistry().subscribe(EAPredictionCommandExchange.class,this.getUUID(), this);
        this.getOCRegistry().subscribe(EpsStateExchange.class, this.getUUID(), this);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();

        //TODO: change to sending as soon as as lasttime+new_ipp_after is reached not the next tick when the next
        // backwards-compatibility breaking update is released
        if (this.lastTimeIPPSent == null || now.isAfter(this.lastTimeIPPSent.plus(this.NEW_IPP_AFTER))) {
            HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
                    this.getUUID(),
                    now,
                    this.currentTemperature,
                    this.tankCapacity,
                    this.tankDiameter,
                    this.ambientTemperature,
                    (this.lastKnownGasPrice == null ? this.defaultPunishmentFactorPerWsPowerLost : (this.lastKnownGasPrice) / this.kWhToWsDivisor),
                    false,
                    this.compressionType,
                    this.compressionValue);
            this.getOCRegistry().publish(
                    InterdependentProblemPart.class,
                    this,
                    ex);
            this.lastTimeIPPSent = now;
            this.temperatureInLastIPP = this.currentTemperature;
        } else if (exchange.getTimeEvents().contains(TimeSubscribeEnum.MINUTE) && this.temperaturePrediction != null) {
            Entry<Long, Double> predEntry = this.temperaturePrediction.floorEntry(exchange.getEpochSecond());
            if (predEntry != null && Math.abs(predEntry.getValue() - this.currentTemperature) > 2) {
                HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
                        this.getUUID(),
                        now,
                        this.currentTemperature,
                        this.tankCapacity,
                        this.tankDiameter,
                        this.ambientTemperature,
                        (this.lastKnownGasPrice == null ? this.defaultPunishmentFactorPerWsPowerLost : (this.lastKnownGasPrice) / this.kWhToWsDivisor),
                        true,
                        this.compressionType,
                        this.compressionValue);
                this.getOCRegistry().publish(
                        InterdependentProblemPart.class,
                        this,
                        ex);
                this.lastTimeIPPSent = now;
                this.temperatureInLastIPP = this.currentTemperature;
            }
        }
    }

    @Override
    public void onDeviceStateUpdate() {

//		System.out.println("state changed at: " + getTimer().getUnixTime());

        // receive new state from driver
        IHALExchange _ihal = this.getObserverDataObject();

        if (_ihal instanceof HotWaterTankObserverExchange) {
            HotWaterTankObserverExchange ox = (HotWaterTankObserverExchange) _ihal;

            this.currentTemperature = ox.getTopTemperature();

            if (Math.abs(this.temperatureInLastIPP - this.currentTemperature) >= this.TRIGGER_IPP_IF_DELTA_TEMP_BIGGER) {

                this.tankCapacity = ox.getTankCapacity();
                this.tankDiameter = ox.getTankDiameter();
                this.ambientTemperature = ox.getAmbientTemperature();

                HotWaterTankNonControllableIPP ex;
                ex = new HotWaterTankNonControllableIPP(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentTime(),
                        this.currentTemperature,
                        this.tankCapacity,
                        this.tankDiameter,
                        this.ambientTemperature,
                        (this.lastKnownGasPrice == null ? this.defaultPunishmentFactorPerWsPowerLost : (this.lastKnownGasPrice) / this.kWhToWsDivisor),
                        false,
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
                    0,
                    0,
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
            this.NEW_IPP_AFTER = _ise.getNewIppAfter();
            this.TRIGGER_IPP_IF_DELTA_TEMP_BIGGER = _ise.getTriggerIfDeltaX();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof EpsStateExchange) {
                EpsStateExchange eee = (EpsStateExchange) exchange;

                long now = this.getTimeDriver().getCurrentEpochSecond();
                double firstPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(now);
                double lastPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(
                        eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPriceUnknownAtAndAfter() - 1);

                this.lastKnownGasPrice = (firstPrice + lastPrice) / 2.0;

        } else if (exchange instanceof EAPredictionCommandExchange) {
            EAPredictionCommandExchange<TemperaturePrediction> exs = ((EAPredictionCommandExchange<TemperaturePrediction>) exchange);
            this.temperaturePrediction = exs.getPrediction().getTemperatureStates();
        }
    }
}
