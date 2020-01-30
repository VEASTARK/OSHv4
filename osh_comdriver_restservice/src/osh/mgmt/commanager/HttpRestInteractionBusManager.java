package osh.mgmt.commanager;

import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.commands.SwitchCommandExchange;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.driver.details.energy.ElectricPowerDriverDetails;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.datatypes.registry.oc.state.MieleDofStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.hal.exchange.HttpRestInteractionComManagerExchange;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * get stuff from O/C-registry
 *
 * @author Kaibin Bao
 */
public class HttpRestInteractionBusManager extends BusManager implements IDataRegistryListener {

    public HttpRestInteractionBusManager(
            IOSHOC osh,
            UUID uuid) {
        super(osh, uuid);
    }

    public boolean setSwitchDetails(UUID element, SwitchRequest sd) {
        SwitchCommandExchange swcmd = new SwitchCommandExchange(
                this.getUUID(),
                element,
                this.getTimeDriver().getCurrentTime(),
                sd.getTurnOn());
        this.getOCRegistry().publish(SwitchCommandExchange.class, swcmd);

        return true;
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        ArrayList<Class<? extends StateExchange>> stateTypesPushedToDriver = new ArrayList<>();
        stateTypesPushedToDriver.add(ElectricPowerDriverDetails.class);
        stateTypesPushedToDriver.add(CommodityPowerStateExchange.class);
        stateTypesPushedToDriver.add(ExpectedStartTimeExchange.class);

        this.initializeStatePushToDriver(stateTypesPushedToDriver);
    }

    // push states to com driver {

    private void initializeStatePushToDriver(ArrayList<Class<? extends StateExchange>> stateTypesPushedToDriver) {
        // register to future state changes
        for (Class<? extends StateExchange> type : stateTypesPushedToDriver) {
            this.getOCRegistry().subscribe(type, this);
        }

        // push current states to driver
        for (Class<? extends StateExchange> type : stateTypesPushedToDriver) {
            for (Entry<UUID, ? extends AbstractExchange> ent : this.getOCRegistry().getData(type).entrySet()) {
                if (ent.getValue() instanceof StateExchange) {
                    HttpRestInteractionComManagerExchange toDriverExchange = new HttpRestInteractionComManagerExchange(
                            this.getUUID(), this.getTimeDriver().getCurrentTime(), (StateExchange) ent.getValue());

                    this.updateOcDataSubscriber(toDriverExchange);
                }
            }
        }
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof StateExchange) {
            HttpRestInteractionComManagerExchange toDriverExchange = new HttpRestInteractionComManagerExchange(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentTime(),
                    (StateExchange) exchange);

            this.updateOcDataSubscriber(toDriverExchange);
        }
    }

    // }

    public MieleDofStateExchange getDof(UUID uuid) {
        return (MieleDofStateExchange) this.getOCRegistry().getData(MieleDofStateExchange.class, uuid);
    }

    @Override
    public void onDriverUpdate(IHALExchange exchangeObject) {
        //NOTHING
    }

}
