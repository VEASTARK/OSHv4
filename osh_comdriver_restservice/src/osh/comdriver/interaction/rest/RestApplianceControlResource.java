package osh.comdriver.interaction.rest;

import osh.comdriver.HttpRestInteractionProviderBusDriver;
import osh.comdriver.interaction.datatypes.RestDevice;
import osh.comdriver.interaction.datatypes.RestDeviceList;
import osh.comdriver.interaction.datatypes.RestDeviceMetaDetails;
import osh.comdriver.interaction.datatypes.fzi.appliancecontrol.RestApplianceControlApplianceStatusDetails;
import osh.eal.hal.HALRealTimeDriver;
import osh.mgmt.commanager.HttpRestInteractionBusManager;
import osh.registry.Registry.DriverRegistry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
@Path("/applianceControl/")
public class RestApplianceControlResource {

    private final HALRealTimeDriver timer;
    private final UUID restUUID;
    private final DriverRegistry registry;
    private final HttpRestInteractionProviderBusDriver interactionDriver;
    @SuppressWarnings("unused")
    private final HttpRestInteractionBusManager interactionComManager;

    /**
     * CONSTRUCTOR
     *
     * @param comMgr
     * @param driver
     */
    public RestApplianceControlResource(HttpRestInteractionBusManager comMgr,
                                        HttpRestInteractionProviderBusDriver driver,
                                        HALRealTimeDriver timer, UUID restUUID, DriverRegistry registry) {
        super();

        this.interactionComManager = comMgr;
        this.interactionDriver = driver;
        this.timer = timer;
        this.restUUID = restUUID;
        this.registry = registry;
    }


    @GET
    @Path("/{idString}/")
    @Produces({"application/xml", "application/json"})
    public RestApplianceControlApplianceStatusDetails getApplianceStatus(
            @PathParam("idString") String idString) {

        Map<UUID, RestDevice> stateMap = this.interactionDriver
                .getRestStateDetails();

        List<String> types = new ArrayList<>();
        types.add("applianceDetails");

        List<UUID> uuids = new ArrayList<>();
        List<String> locations = new ArrayList<>();
        List<String> deviceClassifications = new ArrayList<>();
        List<String> deviceTypes = new ArrayList<>();

        if (idString == null) {
            return null;
        }

        switch (idString) {
            case "InductionHob": {
                UUID uuid = UUID.fromString("484f4c4c-4755-4755-4842-000000000001");
                uuids.add(uuid);
                break;
            }
            case "GasHob": {
                UUID uuid = UUID.fromString("484f4c4c-4547-4547-4748-000000000001");
                uuids.add(uuid);
                break;
            }
            case "ElectricOven": {
                UUID uuid = UUID.fromString("484f4c4c-5354-5354-4f56-000000000001");
                uuids.add(uuid);
                break;
            }
            default:
                return null;
        }

        RestDeviceList rdl = this.getDeviceStates(types, uuids, locations,
                deviceClassifications, deviceTypes);

        int state = rdl.getDeviceList().get(0).getApplianceDetails().getState()
                .getStateInt();

        RestApplianceControlApplianceStatusDetails d = new RestApplianceControlApplianceStatusDetails();
        d.setOn(state != 1);

        return d;
    }


    // HELPER METHODS

    private RestDeviceList getDeviceStates(List<String> types,
                                           List<UUID> uuids, List<String> locations,
                                           List<String> deviceClassifications, List<String> deviceTypes) {
        RestDeviceList list = new RestDeviceList();
        List<String> typeNames = null;

        if (!types.isEmpty()) {

            typeNames = new ArrayList<>(types);
        } /* else: typesLC = null */

        Map<UUID, RestDevice> stateMap = this.interactionDriver
                .getRestStateDetails();

        for (Map.Entry<UUID, RestDevice> ent : stateMap.entrySet()) {
            RestDevice dev = ent.getValue();

            // only states for a list of uuids
            if (uuids != null && !uuids.isEmpty()) {
                if (!uuids.contains(ent.getKey())) {
                    continue;
                }
            }

            if (dev.hasDeviceMetaDetails()) {
                RestDeviceMetaDetails rdmd = dev.getDeviceMetaDetails();
                if (!locations.isEmpty()
                        && !locations.contains(rdmd.getLocation()))
                    continue;
                if (!deviceClassifications.isEmpty()
                        && !deviceClassifications.contains(rdmd
                        .getDeviceClassification().name()))
                    continue;
                if (!deviceTypes.isEmpty()
                        && !deviceTypes.contains(rdmd.getDeviceType().name()))
                    continue;
            } else {
                if (!locations.isEmpty() || !deviceTypes.isEmpty())
                    continue;
            }

            if (typeNames != null) {
                RestDevice clone = dev.cloneOnly(typeNames);
                if (clone != null)
                    list.add(clone);
            } else
                list.add(dev);
        } // for every device

        return list;
    }

}
