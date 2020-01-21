package osh.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.OSH;
import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.registry.DataRegistry;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.*;

/**
 * Acts as a data broker for exchange objects as normally actors in differnent layers of the organic architecture
 * cannot communicate with each other unless it's their direct counterpart.
 *
 * @author Sebastian Kramer, Ingo Mauser
 */
public class DataBroker extends OSHComponent implements ILifeCycleListener, IDataRegistryListener {

    private final UUID uuid;

    private final Map<Class<? extends AbstractExchange>, List<UUIDRegistryPair>> dataMapping
            = new Object2ObjectOpenHashMap<>();

    private DataRegistry comRegistry;
    private DataRegistry ocRegistry;
    private DataRegistry driverRegistry;

    /**
     * Generates this data broker with the given the organic management entity and the given identifer
     *
     * @param entity the organic management entity
     * @param uuid the identifier of this registry
     */
    public DataBroker(IOSH entity, UUID uuid) {
        super(entity);

        this.uuid = uuid;
    }

    /**
     * Registers a data reach-through for the given mapping consisting of a source and a drain registry, the exchange
     * object identifier, the receiver identifier and a flag if the sender of the newly published exchange object
     * should be set to the receiver identifier.
     *
     * Currently does not allow multiple source registries for the same exchange object identifier.
     *
     * @param receiver the receiver identifier
     * @param type the exchange object identifier
     * @param source the source registry for the data reach-through
     * @param drain the target registry for the data reach-through
     * @param setSenderToIdentifier the flag for the change of sender
     * @throws OSHException if for the same exchange object identifier multiple source registries are registered
     */
    public void registerDataReachThrough(UUID receiver, Class<? extends AbstractExchange> type,
                                  RegistryType source, RegistryType drain, boolean setSenderToIdentifier) throws OSHException {
        Objects.requireNonNull(receiver);
        Objects.requireNonNull(type);

        List<UUIDRegistryPair> typeList = this.dataMapping.computeIfAbsent(type, k -> new ArrayList<>());

        if (typeList.stream().anyMatch(e -> e.source != source)) {
            throw new OSHException("data custodian does not support reach-through "
                    + "for multiple source registries for the same exchange");
        }
        typeList.add(new UUIDRegistryPair(receiver, drain, source, setSenderToIdentifier));

        this.getRegistryFromType(source).subscribe(type, this);
    }

    public void registerDataReachThrough(UUID receiver, Class<? extends AbstractExchange> type,
            RegistryType source, RegistryType drain) throws OSHException {
        this.registerDataReachThrough(receiver, type, source, drain, true);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        List<UUIDRegistryPair> listForType = this.dataMapping.get(exchange.getClass());

        if (listForType != null && !listForType.isEmpty()) {

            for (UUIDRegistryPair pair : listForType) {

                if (pair.setSenderToIdentifier) exchange.setSender(pair.identifier);
                this.getRegistryFromType(pair.drain).publish(exchange.getClass(), exchange);
            }
        }
    }

    /**
     * Returns the registry as identified by the {@link RegistryType} enum.
     *
     * @param type the type of the registry
     * @return the registry corresponding to the given type
     */
    private DataRegistry getRegistryFromType(RegistryType type) {
        switch (type) {
            case COM:
                return this.comRegistry;
            case OC:
                return this.ocRegistry;
            default:
                return this.driverRegistry;
        }
    }

    @Override
    public void onSystemRunning() {

    }

    @Override
    public void onSystemShutdown() {

    }

    @Override
    public void onSystemIsUp() {
        OSH osh = (OSH) this.getOSH();
        this.comRegistry = osh.getComRegistry();
        this.ocRegistry = osh.getOCRegistry();
        this.driverRegistry = osh.getDriverRegistry();
    }

    @Override
    public void onSystemHalt() {

    }

    @Override
    public void onSystemResume() {

    }

    @Override
    public void onSystemError() {

    }

    /**
     * Data-container for information about how to handle the data reach-through.
     */
    private static class UUIDRegistryPair {
        public final UUID identifier;
        public final RegistryType drain;
        public final RegistryType source;

        /**
         * flag if the sender of the exchange object should be set to the identifier of this object before
         * re-publishing in the drain-registry
         */
        public final boolean setSenderToIdentifier;

        /**
         * Generates this data-container with the given receiver identifier, registry source, registry target and the
         * given flag
         *
         * @param identifier the receiver identifier
         * @param type the source registry
         * @param source the target registry
         * @param setSenderToIdentifier the flag if the sender should be edited before publishing
         */
        public UUIDRegistryPair(UUID identifier, RegistryType type, RegistryType source, boolean setSenderToIdentifier) {
            this.identifier = identifier;
            this.drain = type;
            this.source = source;
            this.setSenderToIdentifier = setSenderToIdentifier;
        }
    }
}
