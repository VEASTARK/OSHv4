package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.registry.StateExchange;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author inspired by Ingo Mauser
 */
public class GUIStatesComExchange extends CALComExchange {

    private final boolean ocMode;
    private Set<Class<? extends StateExchange>> types;
    private Map<UUID, ? extends StateExchange> states;
    private Class<? extends StateExchange> driverStateType;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public GUIStatesComExchange(
            UUID deviceID,
            Long timestamp,
            Set<Class<? extends StateExchange>> types,
            Map<UUID, ? extends StateExchange> states) {
        super(deviceID, timestamp);

        this.ocMode = true;
        if (types == null) {
            this.types = new HashSet<>();
        } else {
            synchronized (types) {
                @SuppressWarnings("unchecked")
                Class<? extends StateExchange>[] dte = (Class<? extends StateExchange>[]) types.toArray(new Class<?>[0]);

                Set<Class<? extends StateExchange>> clonedTypes = new HashSet<>();

                Collections.addAll(clonedTypes, dte);

                this.types = clonedTypes;
            }
        }

        if (states == null) {
            this.states = new HashMap<>();
        } else {
            synchronized (states) {
                @SuppressWarnings("unchecked")
                Entry<UUID, ? extends StateExchange>[] dte = (Entry<UUID, ? extends StateExchange>[]) states.entrySet().toArray(new Entry<?, ?>[0]);

                Map<UUID, StateExchange> clonedStates = new HashMap<>();

                for (Entry<UUID, ? extends StateExchange> e : dte) {
                    clonedStates.put(e.getKey(), e.getValue().clone()); //no cloning for key
                }

                this.states = clonedStates;
            }
        }
    }

    public GUIStatesComExchange(
            UUID deviceID,
            Long timestamp,
            Class<? extends StateExchange> driverStateType) {
        super(deviceID, timestamp);

        this.ocMode = false;
        this.driverStateType = driverStateType; //cloning not possible
    }

    public Set<Class<? extends StateExchange>> getTypes() {
        return this.types;
    }

    public Map<UUID, ? extends StateExchange> getStates() {
        return this.states;
    }

    public Class<? extends StateExchange> getDriverStateType() {
        return this.driverStateType;
    }

    public boolean isOcMode() {
        return this.ocMode;
    }

}
