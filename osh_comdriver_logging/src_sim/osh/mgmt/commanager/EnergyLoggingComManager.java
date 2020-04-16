package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.cal.ObjectWrapperCALExchange;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.oc.details.energy.CostConfigurationStateExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a com-manager that subscribes to certain emitted exchanges and updates it's com-driver about them.
 *
 * @author Sebastian Kramer
 */
public class EnergyLoggingComManager extends ComManager implements IDataRegistryListener {

    private final Set<Class<? extends StateExchange>> logClasses = new HashSet<>();

    /**
     * Constructs this com-manager with the given osh entity and it's unique identifier
     *
     * @param entity the OSH entity
     * @param uuid the unique identifier
     */
    public EnergyLoggingComManager(IOSHOC entity, UUID uuid) {
        super(entity, uuid);
    }

    @Override
    public void onDriverUpdate(final ICALExchange exchangeObject) {
        //nothing
    }

    @Override
    public void onSystemIsUp() {
        this.logClasses.add(EpsStateExchange.class);
        this.logClasses.add(PlsStateExchange.class);
        this.logClasses.add(CostConfigurationStateExchange.class);

        for (Class<? extends StateExchange> cl : this.logClasses) {
            this.getOCRegistry().subscribe(cl, this);
        }
    }

    @Override
    public <T extends AbstractExchange> void onExchange(final T exchange) {
        if (exchange instanceof StateExchange) {
            if (this.logClasses.contains(((StateExchange) exchange).getClass())) {
                this.updateOcDataSubscriber(new ObjectWrapperCALExchange<>(this.getUUID(),
                        this.getTimeDriver().getCurrentTime(), (StateExchange) exchange));
            }
        }
    }
}
