package osh.hal.exchange;

import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.exchange.HALObserverExchange;

import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class BaseloadStaticExchange
        extends HALObserverExchange {

    private final LoadProfileCompressionTypes compressionType;
    private final int compressionValue;


    public BaseloadStaticExchange(UUID deviceID, Long timestamp, LoadProfileCompressionTypes compressionType,
                                  int compressionValue) {
        super(deviceID, timestamp);
        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
    }


    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }


    public int getCompressionValue() {
        return this.compressionValue;
    }

}
