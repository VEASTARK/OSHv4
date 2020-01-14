package osh.comdriver.interaction.rest;

import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import osh.comdriver.HttpRestInteractionProviderBusDriver;
import osh.comdriver.interaction.datatypes.DevicePowerHistory;
import osh.comdriver.interaction.datatypes.DevicePowerHistoryList;
import osh.comdriver.interaction.datatypes.config.RestHomeConfigElement;
import osh.comdriver.interaction.datatypes.config.RestHomeConfigElementDetails;
import osh.comdriver.interaction.datatypes.config.RestHomeConfiguration;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.mgmt.commanager.HttpRestInteractionBusManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Kaibin Bao
 */
@Path("/")
public class RestConfResource {

    private final HttpRestInteractionBusManager interactionComManager;
    private final HttpRestInteractionProviderBusDriver interactionDriver;
    private final EntityManagerFactory emf;

    /**
     * CONSTRUCTOR
     *
     * @param comMgr assigned com manager of this resource
     * @param driver assigned com driver of this resource
     */
    public RestConfResource(
            HttpRestInteractionBusManager comMgr,
            HttpRestInteractionProviderBusDriver driver) {
        super();

        this.interactionComManager = comMgr;
        this.interactionDriver = driver;

        this.emf = Persistence.createEntityManagerFactory("FZIHomeConfig");
    }

    @GET
    @Produces({"application/xml", "application/json"})
    public RestHomeConfiguration getHomeConfiguration() {
        // only relevant with manual database manipulation
//		emf.getCache().evictAll();

        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();

        //Query q = em.createQuery("SELECT hc FROM RestHomeConfiguration hc WHERE hc.id=1");
        Query q = em.createQuery("SELECT hc FROM RestHomeConfiguration hc ORDER BY hc.id DESC").setMaxResults(1);
        RestHomeConfiguration hc;

        if (q.getResultList().isEmpty()) {
            hc = new RestHomeConfiguration();

            hc.id = 1L;

            /*RestHomeConfigElement element;

            element = new RestHomeConfigElement();
            element.setType("sh_room1");
            element.setX(100);
            element.setY(200);
            element.setW(300);
            element.setH(400);
            hc.getElements().add(element);

            element = new RestHomeConfigElement();
            element.setType("sh_room2");
            element.setX(100);
            element.setY(200);
            element.setW(300);
            element.setH(400);
            hc.getElements().add(element);*/

            em.persist(hc);
        } else {
            hc = (RestHomeConfiguration) q.getSingleResult();
        }

        em.getTransaction().commit();
        em.clear();
        em.close();

        return hc;
    }

    @POST
    @Produces({"application/xml", "application/json"})
    public Response setHomeConfiguration(RestHomeConfiguration hc) {

        //TODO: add UUID

        // only relevant with manual database manipulation
        //emf.getCache().evictAll();
        //hc.id = 1L;

        // Delete ConfigElementDetails, which are not used any more
        StringBuilder ids = new StringBuilder();
        int n = 0;
        for (RestHomeConfigElement el : hc.elements) {
            if (n > 0) {
                ids.append(",");
            }
            ids.append(el.id);
            n++;
        }

        EntityManager em2 = this.emf.createEntityManager();
        em2.getTransaction().begin();
        Query q = em2.createQuery("DELETE FROM RestHomeConfigElementDetails WHERE NOT id IN (" + ids + ")");
        q.executeUpdate();
        em2.getTransaction().commit();
        em2.clear();
        em2.close();

        // HomeConfiguration persistieren
        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(hc);
        em.getTransaction().commit();
        em.clear();
        em.close();

        return Response.ok().build();
    }

    @GET
    @Path("/detail/{id}/")
    @Produces({"application/xml", "application/json"})
    public RestHomeConfigElementDetails getHomeConfigElementDetails(@PathParam("id") Long id) {
        // only relevant with manual database manipulation
//		emf.getCache().evictAll();

        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();

        //Query q = em.createQuery("SELECT hc FROM RestHomeConfiguration hc WHERE hc.id=1");
        Query q = em.createQuery("SELECT e FROM RestHomeConfigElementDetails e WHERE e.id=" + id);
        RestHomeConfigElementDetails e;

        if (q.getResultList().isEmpty()) {
            e = new RestHomeConfigElementDetails();

            e.id = id;

            /*RestHomeConfigElement element;

            element = new RestHomeConfigElement();
            element.setType("sh_room1");
            element.setX(100);
            element.setY(200);
            element.setW(300);
            element.setH(400);
            hc.getElements().add(element);

            element = new RestHomeConfigElement();
            element.setType("sh_room2");
            element.setX(100);
            element.setY(200);
            element.setW(300);
            element.setH(400);
            hc.getElements().add(element);*/

            em.persist(e);
        } else {
            e = (RestHomeConfigElementDetails) q.getSingleResult();
        }

        em.getTransaction().commit();
        em.clear();
        em.close();

        return e;
    }

    @POST
    @Path("/detail/{id}/")
    @Consumes({"application/xml", "application/json"})
    public Response setElementDetails(@PathParam("id") String id, RestHomeConfigElementDetails detailElement) {
//		UUID uuid;
//		try {
//			uuid = UUID.fromString(sUUID);
//		} catch (IllegalArgumentException e) {
//			throw new NotFoundException();
//		}
//		
//		interactionDriver.sendSwitchRequest(uuid, command.isTurnOn());

        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(detailElement);
        em.getTransaction().commit();
        em.clear();
        em.close();

        return Response.ok().build();
    }

    @GET
    @Path("/powerhistory/{location}/{type}/{interval}/")
    @Produces({"application/json"})
    //@Produces({"application/json", "application/xml"})
    public DevicePowerHistoryList getPowerHistory(
            @PathParam("location") String location,
            @PathParam("type") String type,
            @PathParam("interval") Long interval) {

//		Map<UUID,DeviceMetaDriverDetails> map = interactionDriver.getDriverRegistry().getStates(DeviceMetaDriverDetails.class);

        if ("sum".equals(type)) {
            // get sum of all devices

            //TODO
            return null;
        } else if (type != null) {
            String fileNameExt = "3";

            if (interval == 3
                    || interval != 24
                    || interval != 7
                    || interval != 31
                    || interval != 365) {
                fileNameExt = "" + interval;
            }

            String[] uuidsFolders;

            File file = new File("data/");
            uuidsFolders = file.list((current, name) -> new File(current, name).isDirectory());
            System.out.println(Arrays.toString(uuidsFolders));

            assert uuidsFolders != null;
            UUID[] uuids = new UUID[uuidsFolders.length];
            for (int i = 0; i < uuidsFolders.length; i++) {
                try {
                    uuids[i] = UUID.fromString(uuidsFolders[i]);
                } catch (Exception e) {
                    //dont care...
                }
            }

            String[] names = new String[uuidsFolders.length];

            // get device names for UUIDs
            for (int i = 0; i < uuidsFolders.length; i++) {
                if (uuids[i] != null) {
                    DeviceMetaDriverDetails meta = this.interactionDriver.getDriverRegistry().getState(DeviceMetaDriverDetails.class, uuids[i]);

                    String devLocation;
                    if (meta == null) {
                        devLocation = "missing location";
                    } else {
                        devLocation = meta.getLocation();
                    }
                    if (location != null && (location.equals("all") || (location.equals(devLocation)))) {
                        assert meta != null;
                        names[i] = meta.getName();
                    } else {
                        uuids[i] = null;
                    }
                }
            }

            int minTimeDiff = 30;

            long start;
            long end = this.interactionDriver.getTimer().getUnixTime() - minTimeDiff;
            String rrdPath;


            List<DevicePowerHistory> list = new ArrayList<>();

            for (int i = 0; i < uuidsFolders.length; i++) {
                if (uuids[i] != null) {
                    // 1Week
                    // rrdPath = "data/device_" + device + "/data1w.rrd";
                    rrdPath = "data/" + uuidsFolders[i] + "/data" + fileNameExt + ".rrd";

                    RrdDb rrdDb;

                    FetchData fetchData = null;
                    try {
                        rrdDb = RrdDb.of(rrdPath);

                        switch (fileNameExt) {
                            case "3":
                                start = end - 3 * 3600 - minTimeDiff;
                                break;
                            case "24":
                                start = end - 24 * 3600 - minTimeDiff;
                                break;
                            case "7":
                                start = end - 7 * 24 * 3600 - minTimeDiff;
                                break;
                            case "31":
                                start = end - 31 * 24 * 3600 - minTimeDiff;
                                break;
                            default:
                                start = end - 365 * 24 * 3600 - minTimeDiff;
                                break;
                        }

                        FetchRequest request = rrdDb.createFetchRequest(ConsolFun.MAX, start, end);
                        fetchData = request.fetchData();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (fetchData == null) {
                        return null;
                    }
                    long[] timestamps = fetchData.getTimestamps();
                    double[] datas = fetchData.getValues(0);


                    DevicePowerHistory h1 = new DevicePowerHistory();
                    h1.setLabel(names[i]);

                    Long[][] arr = new Long[timestamps.length][2];
                    for (int j = 0; j < timestamps.length; j++) {
                        arr[j][0] = timestamps[j] * 1000;
                        arr[j][1] = Math.round(datas[j]);
                    }
                    h1.setDataLong(arr);

                    list.add(h1);
                }
            }


            DevicePowerHistoryList l = new DevicePowerHistoryList();
            l.setListDevicePowerHistory(list);


            return l;
        } else {
            return null;
        }

    }

    @GET
    @Path("/powercurrent/{location}/{interval}/")
    @Produces({"application/json"})
    //@Produces({"application/json", "application/xml"})
    public DevicePowerHistoryList getPowerCurrent(@PathParam("location") Long location, @PathParam("interval") Long interval) {

        DevicePowerHistory h1 = new DevicePowerHistory();
        h1.setLabel("Kühlschrank");

        Long[][] arr = {{1382392800000L, 77L}};
        h1.setDataLong(arr);

        List<DevicePowerHistory> list = new ArrayList<>();
        list.add(h1);

        DevicePowerHistory h2 = new DevicePowerHistory();
        h2.setLabel("Wasserkocher");

        Long[][] arr2 = {{1382392800000L, 800L}};
        h2.setDataLong(arr2);

        list.add(h2);

        DevicePowerHistoryList l = new DevicePowerHistoryList();
        l.setListDevicePowerHistory(list);

        return l;
    }

}
