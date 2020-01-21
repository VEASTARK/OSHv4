package osh.mgmt.localobserver;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.hal.exchange.HotWaterTankObserverExchange;
import osh.mgmt.ipp.HotWaterTankNonControllableIPP;
import osh.mgmt.ipp.watertank.HotWaterTankPrediction;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.IHasState;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class HotWaterTankLocalObserver
        extends WaterTankLocalObserver
        implements IHasState, IDataRegistryListener {

    private final double defaultPunishmentFactorPerWsPowerLost = 6.0 / 3600000.0;
    TreeMap<Long, Double> temperaturePrediction = new TreeMap<>();
    private long NEW_IPP_AFTER;
    private double TRIGGER_IPP_IF_DELTA_TEMP_BIGGER;
    private long lastTimeIPPSent = Long.MIN_VALUE;
    private Double lastKnownGasPrice;
    private double tankCapacity = 100;
    private double tankDiameter = 1.0;
    private double ambientTemperature = 20.0;
    private boolean lastMinuteViolated;
    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


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

        this.getTimer().registerComponent(this, 1);
        this.getOCRegistry().subscribe(EAPredictionCommandExchange.class, this.getDeviceID(),this);
        this.getOCRegistry().subscribe(EpsStateExchange.class, this.getDeviceID(),this);
    }


    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        long now = this.getTimer().getUnixTime();

        if (now > this.lastTimeIPPSent + this.NEW_IPP_AFTER) {
            HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
                    this.getDeviceID(),
                    this.getGlobalLogger(),
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
        } else if (now % 60 == 0 && this.temperaturePrediction != null) {
            Entry<Long, Double> predEntry = this.temperaturePrediction.floorEntry(now);
            if (predEntry != null
                    && Math.abs(predEntry.getValue() - this.currentTemperature) > 2.5
                    //if pred is too old don't pay attention to it
                    && (this.temperaturePrediction.ceilingEntry(now) != null || (now - predEntry.getKey()) < 3600)) {
                if (this.lastMinuteViolated) {
                    this.getGlobalLogger().logDebug("Temperature prediction was wrong by >2.5 degree for two consecutive minutes, reschedule");
                    HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
                            this.getDeviceID(),
                            this.getGlobalLogger(),
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
                } else {
                    this.lastMinuteViolated = true;
                }
            } else {
                this.lastMinuteViolated = false;
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

            if (Math.abs(this.temperatureInLastIPP - this.currentTemperature) >= this.TRIGGER_IPP_IF_DELTA_TEMP_BIGGER) {

                this.tankCapacity = ox.getTankCapacity();
                this.tankDiameter = ox.getTankDiameter();
                this.ambientTemperature = ox.getAmbientTemperature();

                HotWaterTankNonControllableIPP ex;
                ex = new HotWaterTankNonControllableIPP(
                        this.getDeviceID(),
                        this.getGlobalLogger(),
                        this.getTimer().getUnixTime(),
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
                this.lastTimeIPPSent = this.getTimer().getUnixTime();
                this.temperatureInLastIPP = this.currentTemperature;
            }

            // save current state in OCRegistry (for e.g. GUI)
            WaterStorageOCSX sx = new WaterStorageOCSX(
                    this.getDeviceID(),
                    this.getTimer().getUnixTime(),
                    this.currentTemperature,
                    this.currentMinTemperature,
                    this.currentMaxTemperature,
                    ox.getHotWaterDemand(),
                    ox.getHotWaterSupply(),
                    this.getDeviceID());
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

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof EpsStateExchange) {
            EpsStateExchange eee = (EpsStateExchange) exchange;

            long now = this.getTimer().getUnixTime();
            double firstPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(now);
            double lastPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(
                    eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPriceUnknownAtAndAfter() - 1);

            this.lastKnownGasPrice = (firstPrice + lastPrice) / 2.0;
        }

        if (exchange instanceof EAPredictionCommandExchange) {
            EAPredictionCommandExchange<HotWaterTankPrediction> exs = ((EAPredictionCommandExchange<HotWaterTankPrediction>) exchange);
            this.temperaturePrediction = exs.getPrediction().getTemperatureStates();
        }
    }

}
