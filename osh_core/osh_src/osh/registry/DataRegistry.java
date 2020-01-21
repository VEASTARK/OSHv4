package osh.registry;

import org.reflections.Reflections;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class DataRegistry extends AbstractRegistry<Class<? extends AbstractExchange>, IDataRegistryListener> {

    /**
     *
     */
    private static final Reflections reflections = new Reflections(AbstractExchange.class.getPackageName());

    /**
     * Generates a registry with the given organic management entity and the flag if this registry supports a
     * simulation or real-world use
     *
     * @param entity       the organic management entity
     * @param isSimulation flag if this registry is run in a simulation
     */
    public DataRegistry(IOSH entity, boolean isSimulation) {
        super(entity, isSimulation);
    }

    @Override
    AbstractListenerWrapper<IDataRegistryListener> convertCallback(IDataRegistryListener listener) {
        return new DataListenerWrapper(listener);
    }

    /**
     * Specifies the publishing behaviour to automatically set the sender of a command exchange to it's intended 
     * recipient. Otherwise will behave exactly as {@link AbstractRegistry#publish(Object, AbstractExchange)}.
     *
     * @param identifier the identifier under wich the exchange object will be published
     * @param exchange the exchange object
     */
    @Override
    public void publish(Class<? extends AbstractExchange> identifier, AbstractExchange exchange) {
        if (CommandExchange.class.isAssignableFrom(identifier)) {
            super.publish(identifier, ((CommandExchange) exchange).getReceiver(), exchange);
        } else {
            super.publish(identifier, exchange);
        }
    }

    /**
     * Specifies the subscribing behaviour to automatically reject subscribing to all command exchanges as these are 
     * only intended for a specific recipient.
     *
     * Additionally will also subscripe for all subclasses of the given identifier.
     * 
     * @param identifier the given identifier
     * @param listener the given listener interface
     */
    @Override
    public void subscribe(Class<? extends AbstractExchange> identifier, IDataRegistryListener listener) {
        if (CommandExchange.class.isAssignableFrom(identifier)) {
            throw new IllegalArgumentException("it is not possible to subscribe to all command exchanges, please " +
                    "provide a sender UUID");
        }
        for (Class<? extends AbstractExchange> subType : reflections.getSubTypesOf(identifier)) {
            super.subscribe(subType, listener);
        }
        super.subscribe(identifier, listener);
    }

    /**
     * Specifies the subscribing behaviour to also subscripe for all subclasses of the given identifier that are
     * published as the given sender.
     *
     * @param identifier the identifier of the exchange
     * @param sender the sender of the exchange
     * @param listener the listener interface
     */
    @Override
    public void subscribe(Class<? extends AbstractExchange> identifier, UUID sender, IDataRegistryListener listener) {
        for (Class<? extends AbstractExchange> subType : reflections.getSubTypesOf(identifier)) {
            super.subscribe(subType, sender, listener);
        }
        super.subscribe(identifier, sender, listener);
    }

    /**
     * Registry for the exchange of objects in the Com-layer of the organic architecture
     */
    public static class ComRegistry extends DataRegistry {

        /**
         * Generates a registry with the given organic management entity and the flag if this registry supports a
         * simulation or real-world use
         *
         * @param entity       the organic management entity
         * @param isSimulation flag if this registry is run in a simulation
         */
        public ComRegistry(IOSH entity, boolean isSimulation) {
            super(entity, isSimulation);
        }
    }

    /**
     * Registry for the exchange of objects in the EAL-layer of the organic architecture
     */
    public static class DriverRegistry extends DataRegistry {

        /**
         * Generates a registry with the given organic management entity and the flag if this registry supports a
         * simulation or real-world use
         *
         * @param entity       the organic management entity
         * @param isSimulation flag if this registry is run in a simulation
         */
        public DriverRegistry(IOSH entity, boolean isSimulation) {
            super(entity, isSimulation);
        }
    }

    /**
     * Registry for the exchange of objects in the OC-layer of the organic architecture
     */
    public static class OCRegistry extends DataRegistry {

        /**
         * Generates a registry with the given organic management entity and the flag if this registry supports a
         * simulation or real-world use
         *
         * @param entity       the organic management entity
         * @param isSimulation flag if this registry is run in a simulation
         */
        public OCRegistry(IOSH entity, boolean isSimulation) {
            super(entity, isSimulation);
        }
    }
}
