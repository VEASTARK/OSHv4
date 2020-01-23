package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.registry.oc.details.DeviceMetaOCDetails;
import osh.datatypes.registry.oc.details.SwitchOCDetails;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.hal.exchange.SmartPlugObserverExchange;

import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class SmartPlugLocalObserver extends LocalObserver {


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public SmartPlugLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onDeviceStateUpdate() {
        SmartPlugObserverExchange plugObserverExchange = (SmartPlugObserverExchange) this.getObserverDataObject();

        UUID uuid = plugObserverExchange.getDeviceID();
        long timestamp = plugObserverExchange.getTimestamp();

        if (plugObserverExchange.getDeviceType() == DeviceTypes.SWITCHPLUG
                || plugObserverExchange.getDeviceType() == DeviceTypes.METERSWITCHPLUG) {
            SwitchOCDetails switchDetails = new SwitchOCDetails(uuid, timestamp);
            switchDetails.setOn(plugObserverExchange.isOn());
            this.getOCRegistry().publish(SwitchOCDetails.class, this, switchDetails);
        }

        CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                uuid,
                timestamp,
                DeviceTypes.METERSWITCHPLUG);
        cpse.addPowerState(Commodity.ACTIVEPOWER, plugObserverExchange.getActivePower());
        cpse.addPowerState(Commodity.REACTIVEPOWER, plugObserverExchange.getReactivePower());
        this.getOCRegistry().publish(CommodityPowerStateExchange.class, this, cpse);

        DeviceMetaOCDetails metaDetails = new DeviceMetaOCDetails(uuid, timestamp);
        metaDetails.setName(plugObserverExchange.getName());
        metaDetails.setLocation(plugObserverExchange.getLocation());
        metaDetails.setDeviceType(plugObserverExchange.getDeviceType());
        metaDetails.setDeviceClassification(plugObserverExchange.getDeviceClassification());
        metaDetails.setConfigured(plugObserverExchange.isConfigured());
        this.getOCRegistry().publish(DeviceMetaOCDetails.class, this, metaDetails);
    }

    @Override
    public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
        return null;
    }
}
