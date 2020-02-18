package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;


/**
 * @author inspired by Ingo Mauser
 */
public class GUIStatesComExchange extends CALComExchange {

    private final boolean ocMode;
    private Set<Class<? extends AbstractExchange>> types;
    private Map<UUID, ? extends AbstractExchange> states;
    private Class<? extends AbstractExchange> driverStateType;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public GUIStatesComExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            Set<Class<? extends AbstractExchange>> types,
            Map<UUID, ? extends AbstractExchange> states) {
        super(deviceID, timestamp);

        this.ocMode = true;
        if (types == null) {
            this.types = new HashSet<>();
        } else {
            synchronized (types) {
                @SuppressWarnings("unchecked")
                Class<? extends AbstractExchange>[] dte = new Class[types.size()];
                dte = types.toArray(dte);

                Set<Class<? extends AbstractExchange>> clonedTypes = new HashSet<>();

                Collections.addAll(clonedTypes, dte);

                this.types = clonedTypes;
            }
        }

        if (states == null) {
            this.states = new HashMap<>();
        } else {
            synchronized (states) {
                @SuppressWarnings("unchecked")
                Entry<UUID, ? extends AbstractExchange>[] dte = new Entry[states.size()];
                dte = states.entrySet().toArray(dte);

                Map<UUID, StateExchange> clonedStates = new HashMap<>();

                for (Entry<UUID, ? extends AbstractExchange> e : dte) {
                    clonedStates.put(e.getKey(), (StateExchange) e.getValue().clone()); //no cloning for key
                }

                this.states = clonedStates;
            }
        }
    }

    public GUIStatesComExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            Class<? extends AbstractExchange> driverStateType) {
        super(deviceID, timestamp);

        this.ocMode = false;
        this.driverStateType = driverStateType; //cloning not possible
    }

    public Set<Class<? extends AbstractExchange>> getTypes() {
        return this.types;
    }

    public Map<UUID, ? extends AbstractExchange> getStates() {
        return this.states;
    }

    public Class<? extends AbstractExchange> getDriverStateType() {
        return this.driverStateType;
    }

    public boolean isOcMode() {
        return this.ocMode;
    }

}
