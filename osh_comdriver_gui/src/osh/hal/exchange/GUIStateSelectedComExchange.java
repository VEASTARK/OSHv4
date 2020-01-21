package osh.hal.exchange;

import osh.cal.CALExchange;
import osh.datatypes.registry.AbstractExchange;

import java.util.UUID;


/**
 * @author Till Schuberth
 */
public class GUIStateSelectedComExchange extends CALExchange {

    private final Class<? extends AbstractExchange> cls;


    /**
     * CONSTRUCTOR
     */
    public GUIStateSelectedComExchange(UUID deviceID, Long timestamp, Class<? extends AbstractExchange> cls) {
        super(deviceID, timestamp);
        this.cls = cls;
    }

    public Class<? extends AbstractExchange> getSelected() {
        return this.cls;
    }

}
