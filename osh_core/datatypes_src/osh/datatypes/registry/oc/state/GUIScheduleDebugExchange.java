package osh.datatypes.registry.oc.state;

import osh.datatypes.registry.EventExchange;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;


/**
 * @author Till Schuberth
 */
public class GUIScheduleDebugExchange extends EventExchange {

    /**
     *
     */
    private static final long serialVersionUID = 2002327995008392436L;
    private final Map<UUID, String> eaPartStrings = new TreeMap<>();

    public GUIScheduleDebugExchange(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public void addString(UUID device, String eaPartString) {
        this.eaPartStrings.put(device, eaPartString);
    }

    public Map<UUID, String> getEaPartString() {
        return this.eaPartStrings;
    }
}
