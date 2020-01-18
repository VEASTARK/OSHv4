package osh.comdriver.interaction.rest;

import osh.comdriver.HttpRestInteractionProviderBusDriver;
import osh.comdriver.interaction.datatypes.RestDevice;
import osh.comdriver.interaction.datatypes.RestDeviceList;
import osh.comdriver.interaction.datatypes.RestDeviceMetaDetails;
import osh.comdriver.interaction.datatypes.RestSwitchCommand;
import osh.mgmt.commanager.HttpRestInteractionBusManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
@Path("/")
public class RestDeviceListResource {

    private final HttpRestInteractionBusManager interactionComManager;
    private final HttpRestInteractionProviderBusDriver interactionDriver;

    /**
     * CONSTRUCTOR
     *
     * @param comMgr
     * @param driver
     */
    public RestDeviceListResource(HttpRestInteractionBusManager comMgr,
                                  HttpRestInteractionProviderBusDriver driver) {
        super();

        this.interactionComManager = comMgr;
        this.interactionDriver = driver;
    }

    private RestDeviceList getDeviceStates(
            List<String> types,
            List<UUID> uuids,
            List<String> locations,
            List<String> deviceClassifications,
            List<String> deviceTypes) {
        RestDeviceList list = new RestDeviceList();
        List<String> typeNames = null;

        if (!types.isEmpty()) {

            typeNames = new ArrayList<>(types);
        } /* else: typesLC = null */

        Map<UUID, RestDevice> stateMap = this.interactionDriver.getRestStateDetails();

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
                        && !deviceClassifications.contains(rdmd.getDeviceClassification().name()))
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

    @GET
    @Produces({"application/xml", "application/json"})
    public RestDeviceList getAssignedDevices(
            @QueryParam("type") List<String> types,
            @QueryParam("uuid") List<UUID> uuids,
            @QueryParam("location") List<String> locations,
            @QueryParam("deviceclass") List<String> deviceClassifications,
            @QueryParam("devicetype") List<String> deviceTypes) {
        return this.getDeviceStates(types, uuids, locations, deviceClassifications, deviceTypes);
    }

    @POST
    @Path("/{uuid}/")
    @Consumes({"application/xml", "application/json"})
    public Response switchCommand(
            @PathParam("uuid") String sUUID,
            RestSwitchCommand command) {

        UUID uuid;
        try {
            uuid = UUID.fromString(sUUID);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }

        this.interactionDriver.sendSwitchRequest(uuid, command.isTurnOn());

        return Response.ok().build();
    }

    @GET
    @Path("/{uuid}/do")
    @Consumes({"application/xml", "application/json"})
    public Response doAction(
            @PathParam("uuid") String sUUID,
            @QueryParam("action") String action) {

        UUID uuid;
        try {
            uuid = UUID.fromString(sUUID);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }

        if ("start".equals(action)) {
            this.interactionDriver.sendStartRequest(uuid);
        }
        if ("stop".equals(action)) {
            this.interactionDriver.sendStopRequest(uuid);
        }
        if ("switchOn".equals(action)) {
            this.interactionDriver.sendSwitchRequest(uuid, true);
        }
        if ("switchOff".equals(action)) {
            this.interactionDriver.sendSwitchRequest(uuid, false);
        }

        return Response.ok().build();
    }

}
