package osh.eal.hal.exchange.compression;

import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.exchange.HALObserverExchange;

import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class StaticCompressionExchange
        extends HALObserverExchange {

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    public StaticCompressionExchange(UUID deviceID, long timestamp) {
        super(deviceID, timestamp);
    }

    public StaticCompressionExchange(UUID deviceID, long timestamp, LoadProfileCompressionTypes compressionType,
                                     int compressionValue) {
        super(deviceID, timestamp);
        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
    }

    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }

    public void setCompressionType(LoadProfileCompressionTypes compressionType) {
        this.compressionType = compressionType;
    }

    public int getCompressionValue() {
        return this.compressionValue;
    }

    public void setCompressionValue(int compressionValue) {
        this.compressionValue = compressionValue;
    }
}
