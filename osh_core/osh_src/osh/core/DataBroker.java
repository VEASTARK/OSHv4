package osh.core;

import osh.OSH;
import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.Exchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;
import osh.registry.Registry;
import osh.registry.interfaces.IEventTypeReceiver;

import java.util.*;

/**
 * @author Sebastian Kramer, Ingo Mauser
 */
public class DataBroker extends OSHComponent implements ILifeCycleListener, IEventTypeReceiver {

    private final UUID uuid;

    private final Map<Class<? extends Exchange>, List<UUIDRegistryPair>> dataMapping
            = new HashMap<>();

    private Registry comRegistry;
    private Registry ocRegistry;
    private Registry driverRegistry;

    public DataBroker(UUID uuid, IOSH theOrganicSmartHome) {
        super(theOrganicSmartHome);

        this.uuid = uuid;
    }

    @Override
    public void onSystemIsUp() {
        OSH osh = (OSH) this.getOSH();
        this.comRegistry = osh.getComRegistry();
        this.ocRegistry = osh.getOCRegistry();
        this.driverRegistry = osh.getDriverRegistry();
    }

    public void registerDataReachThroughState(UUID receiver, Class<? extends StateExchange> type,
                                              RegistryType source, RegistryType drain) throws OSHException {

        List<UUIDRegistryPair> typeList = this.dataMapping.get(type);

        if (typeList == null) {
            typeList = new ArrayList<>();
            this.dataMapping.put(type, typeList);
        } else {
            if (!typeList.stream().allMatch(e -> e.source == source)) {
                throw new OSHException("data custodian does not support reach-through "
                        + "for multiple source registries for the same stateExchange");
            }
        }
        typeList.add(new UUIDRegistryPair(receiver, drain, source));

        Registry toRegister = this.getRegistryFromType(source);

        try {
            toRegister.registerStateChangeListener(type, this);
        } catch (OSHException e) {
            // nop. happens.
            this.getGlobalLogger().logError("should not happen", e);
        }
    }

    public void registerDataReachThroughEvent(UUID receiver, Class<? extends EventExchange> type,
                                              RegistryType source, RegistryType drain) {

        List<UUIDRegistryPair> typeList = this.dataMapping.computeIfAbsent(type, k -> new ArrayList<>());

        typeList.add(new UUIDRegistryPair(receiver, drain, source));

        Registry toRegister = this.getRegistryFromType(source);

        try {
            toRegister.register(type, this);
        } catch (OSHException e) {
            // nop. happens.
            this.getGlobalLogger().logError("should not happen", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) {

        if (event instanceof StateChangedExchange) {

            Class<? extends StateExchange> stateType = ((StateChangedExchange) event).getType();

            List<UUIDRegistryPair> listForType = this.dataMapping.get(stateType);

            if (listForType != null && !listForType.isEmpty()) {

                for (UUIDRegistryPair pair : listForType) {

                    Object state = this.getRegistryFromType(pair.source).getState(stateType, ((StateChangedExchange) event).getStatefulEntity());

                    StateExchange test = stateType.cast(state);
                    test.setSender(pair.identifier);

                    this.getRegistryFromType(pair.drain).setStateOfSender(stateType.asSubclass(StateExchange.class), test);

                    //					getRegistryFromType(pair.drain).setStateOfSender(stateType, stateType.cast(test));
                }
            }
        } else {
            List<UUIDRegistryPair> listForType = this.dataMapping.get(type);

            if (listForType != null && !listForType.isEmpty()) {

                for (UUIDRegistryPair pair : listForType) {

                    T ex = (T) event.clone();
                    ex.setSender(pair.identifier);
                    this.getRegistryFromType(pair.drain).sendEvent(type, ex);

                }
            }
        }
    }


    private Registry getRegistryFromType(RegistryType type) {
        switch (type) {
            case COM:
                return this.comRegistry;
            case OC:
                return this.ocRegistry;
            case DRIVER:
                return this.driverRegistry;
            default:
                return null;
        }
    }

    @Override
    public Object getSyncObject() {
        return this;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void onSystemRunning() {
        //NOTHING
    }

    @Override
    public void onSystemShutdown() {
        //NOTHING
    }

    @Override
    public void onSystemHalt() {
        //NOTHING
    }

    @Override
    public void onSystemResume() {
        //NOTHING
    }

    @Override
    public void onSystemError() {
        //NOTHING
    }

    private static class UUIDRegistryPair {
        public final UUID identifier;
        public final RegistryType drain;
        public final RegistryType source;

        public UUIDRegistryPair(UUID identifier, RegistryType type, RegistryType source) {
            this.identifier = identifier;
            this.drain = type;
            this.source = source;
        }
    }
}
