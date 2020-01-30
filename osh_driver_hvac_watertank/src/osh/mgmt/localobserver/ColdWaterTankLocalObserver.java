package osh.mgmt.localobserver;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.ColdWaterTankObserverExchange;
import osh.mgmt.ipp.ColdWaterTankNonControllableIPP;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ColdWaterTankLocalObserver
        extends WaterTankLocalObserver {

    private static final Duration NEW_IPP_AFTER = Duration.ofHours(1); // at least send new IPP every hour
    private ZonedDateTime lastTimeIPPSent;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public ColdWaterTankLocalObserver(IOSHOC osh) {
        super(osh);

        this.currentMinTemperature = 10;
        this.currentMaxTemperature = 15;
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();

        if (this.lastTimeIPPSent == null || now.isAfter(this.lastTimeIPPSent.plus(NEW_IPP_AFTER))) {
            ColdWaterTankNonControllableIPP ex;
            ex = new ColdWaterTankNonControllableIPP(
                    this.getUUID(),
                    this.getGlobalLogger(),
                    now,
                    this.currentTemperature,
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

    @Override
    public void onDeviceStateUpdate() {
        // receive new state from driver
        IHALExchange _ihal = this.getObserverDataObject();

        if (_ihal instanceof ColdWaterTankObserverExchange) {
            ColdWaterTankObserverExchange ox = (ColdWaterTankObserverExchange) _ihal;

            this.currentTemperature = ox.getTopTemperature();

            if (Math.abs(this.temperatureInLastIPP - this.currentTemperature) >= 0.1) {
                ColdWaterTankNonControllableIPP ex;
                ex = new ColdWaterTankNonControllableIPP(
                        this.getUUID(),
                        this.getGlobalLogger(),
                        this.getTimeDriver().getCurrentTime(),
                        this.currentTemperature,
                        this.compressionType,
                        this.compressionValue);
                this.getOCRegistry().publish(
                        InterdependentProblemPart.class,
                        this,
                        ex);
                this.temperatureInLastIPP = this.currentTemperature;
                this.lastTimeIPPSent = this.getTimeDriver().getCurrentTime();
            }

            // save current state in OCRegistry (for e.g. GUI)
            WaterStorageOCSX sx = new WaterStorageOCSX(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentTime(),
                    this.currentTemperature,
                    this.currentMinTemperature,
                    this.currentMaxTemperature,
                    ox.getColdWaterDemand(),
                    ox.getColdWaterSupply(),
                    this.getUUID());
            this.getOCRegistry().publish(
                    WaterStorageOCSX.class,
                    this,
                    sx);
        } else if (_ihal instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }
}
