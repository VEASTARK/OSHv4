package osh.mgmt.localobserver;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ColdWaterTankObserverExchange;
import osh.mgmt.ipp.ColdWaterTankNonControllableIPP;
import osh.registry.interfaces.IHasState;

import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ColdWaterTankLocalObserver
        extends WaterTankLocalObserver
        implements IHasState {

    private static final long NEW_IPP_AFTER = 3600; // at least send new IPP every hour
    private long lastTimeIPPSent = Long.MIN_VALUE;

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

        this.getTimer().registerComponent(this, 1);
    }


    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        long now = this.getTimer().getUnixTime();

        if (now > this.lastTimeIPPSent + NEW_IPP_AFTER) {
            ColdWaterTankNonControllableIPP ex;
            ex = new ColdWaterTankNonControllableIPP(
                    this.getDeviceID(),
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
                        this.getDeviceID(),
                        this.getGlobalLogger(),
                        this.getTimer().getUnixTime(),
                        this.currentTemperature,
                        this.compressionType,
                        this.compressionValue);
                this.getOCRegistry().publish(
                        InterdependentProblemPart.class,
                        this,
                        ex);
                this.temperatureInLastIPP = this.currentTemperature;
                this.lastTimeIPPSent = this.getTimer().getUnixTime();
            }

            // save current state in OCRegistry (for e.g. GUI)
            WaterStorageOCSX sx = new WaterStorageOCSX(
                    this.getDeviceID(),
                    this.getTimer().getUnixTime(),
                    this.currentTemperature,
                    this.currentMinTemperature,
                    this.currentMaxTemperature,
                    ox.getColdWaterDemand(),
                    ox.getColdWaterSupply(),
                    this.getDeviceID());
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

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }

}
