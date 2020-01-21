package osh.comdriver;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import osh.comdriver.interaction.datatypes.*;
import osh.comdriver.interaction.rest.RestApplianceControlResource;
import osh.comdriver.interaction.rest.RestDeviceListResource;
import osh.comdriver.interaction.rest.RestPutBLEResource;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.*;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDofDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceProgramDriverDetails;
import osh.datatypes.registry.driver.details.appliance.miele.MieleApplianceDriverDetails;
import osh.datatypes.registry.driver.details.energy.ElectricPowerDriverDetails;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.HALDriver;
import osh.eal.hal.exchange.IHALExchange;
import osh.hal.exchange.HttpRestInteractionComManagerExchange;
import osh.mgmt.commanager.HttpRestInteractionBusManager;
import osh.registry.DataRegistry.DriverRegistry;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
public abstract class HttpRestInteractionProviderBusDriver extends HALBusDriver implements IDataRegistryListener {

    private int m_port = 8080;
    private Server m_jetty;

    private Map<UUID, RestDevice> restStateDetails;

    // CONSTRUCTORS

    private HttpRestInteractionProviderBusDriver(int port) {
        super(null, null, null);
        this.m_port = port;
    }

    public HttpRestInteractionProviderBusDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        String sPort = driverConfig.getParameter("port");
        if (sPort != null) {
            try {
                int port = Integer.parseInt(sPort);
                if (port > 0 && port < 65536)
                    this.m_port = port;
            } catch (NumberFormatException e) {
                // NOP => use default port 8080
            }
        }

        this.restStateDetails = new HashMap<>();

    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        HttpRestInteractionBusManager busManager = (HttpRestInteractionBusManager) this.getAssignedBusManager();

        this.getGlobalLogger().logDebug("starting web server on port " + this.m_port);

        // initialize web server
        this.m_jetty = new Server();

        QueuedThreadPool p = (QueuedThreadPool) this.m_jetty.getThreadPool();
        p.setMinThreads(100);
        p.setMaxThreads(500);

        // configure web server
        //m_jetty.setAttribute("maxIdleTimeMs", 60000);

        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
        servletContext.setContextPath("/");
        //servletContext.setResourceBase("./www/");

        ResourceConfig rcRS = new ResourceConfig();
//        rcRS.register(Jackson1Feature.class);
//        rcRS.register(JacksonFeature.class);
        rcRS.registerInstances(new RestDeviceListResource(busManager, this));
//        rcRS.registerInstances(new RestTestResource());
//      rcRS.registerInstances(new RestChilliiResource(comManager, this));
//      rcRS.registerInstances(new RestHabiteqResource(comManager, this));
        ServletHolder shRS = new ServletHolder(new ServletContainer(rcRS));

        ResourceConfig rcAC = new ResourceConfig();
//        rcAC.register(Jackson1Feature.class);
//        rcAC.register(JacksonFeature.class);
        rcAC.registerInstances(new RestApplianceControlResource(
                busManager,
                this,
                this.getTimer(),
                this.getDeviceID(),
                this.getDriverRegistry()));
        ServletHolder shAC = new ServletHolder(new ServletContainer(rcAC));

        {
            //FZI config resource
//        	ResourceConfig rcCONF = new ResourceConfig();
//        	rcCONF.register(Jackson1Feature.class);
//            rcCONF.registerInstances(new RestConfResource(comManager, this));
//            ServletHolder shCONF = new ServletHolder( new ServletContainer( rcCONF ) );
//            
//            servletContext.addServlet(shCONF, "/conf/*");
        }

        {
            ResourceConfig bleConf = new ResourceConfig();
//        	bleConf.register(Jackson1Feature.class);
            bleConf.registerInstances(new RestPutBLEResource());
            ServletHolder bleHolder = new ServletHolder(new ServletContainer(bleConf));

            servletContext.addServlet(bleHolder, "/ble/*");
        }


        //ServletHolder shPHP = new ServletHolder( new CGI() );
        //shPHP.setInitParameter("commandPrefix", "/usr/bin/php-cgi");
        //shPHP.setInitParameter("ENV_REDIRECT_STATUS", "200");

        //servletContext.addServlet(shPHP, "*.php");
        servletContext.addServlet(shRS, "/rs/*");
        servletContext.addServlet(shAC, "/restcontrol/*");


        //???
        servletContext.setInitParameter("maxCachedFiles", "0");

        ResourceHandler staticResources = new ResourceHandler();
        staticResources.setResourceBase("./www/" + this.getEnvironment() + "/");
        staticResources.setCacheControl("no-cache");
        staticResources.setMinAsyncContentLength(1); // IMPORTANT: otherwise no PNG, no CSS files (DELAY, TIMEOUT)

        HandlerList handlers = new HandlerList();
        handlers.addHandler(servletContext);
        handlers.addHandler(staticResources);
        handlers.addHandler(new DefaultHandler());
        this.m_jetty.setHandler(handlers);

        HttpConfiguration http_config = new HttpConfiguration();
//        http_config.setOutputBufferSize(131072);
//        http_config.setResponseHeaderSize(8192);
//        http_config.setRequestHeaderSize(8192);
        //http_config.setSendServerVersion(true);
        //http_config.setSendDateHeader(true);

        ServerConnector http = new ServerConnector(this.m_jetty, new HttpConnectionFactory(http_config));
        http.setPort(this.m_port);
        http.setIdleTimeout(5000);
//        http.setIdleTimeout(500);
        this.m_jetty.addConnector(http);

        this.registerToStateChanges();

        try {
            this.m_jetty.start();
            //m_jetty.join();
        } catch (Exception e) {
            throw new OSHException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

        if (this.m_jetty != null) {
            try {
                this.m_jetty.stop();
                this.m_jetty.join();
            } catch (Exception e) {
                throw new OSHException(e.getMessage(), e.getCause());
            }
        }
    }

    // restStateDetails functions {

    public Map<UUID, RestDevice> getRestStateDetails() {
        return this.restStateDetails;
    }

    public RestDevice getRestStateDetails(UUID deviceId) {
        RestDevice details = this.restStateDetails.get(deviceId);
        if (details == null) {
            details = new RestDevice();
            details.setUuid(deviceId);
            this.restStateDetails.put(deviceId, details);
        }
        return details;
    }

    protected void storeStateExchange(UUID uuid, StateExchange sx, boolean fromOC) {
//		getGlobalLogger().printDebugMessage("rest driver got state exchange: " + sx.toString());


        if (sx instanceof CommodityPowerStateExchange) { // Global O/C unit ?!?
            CommodityPowerStateExchange cpse = (CommodityPowerStateExchange) sx;
            RestPowerDetails powerDetails = this.getRestStateDetails(uuid).getPowerDetails();

            powerDetails.setTimestamp(cpse.getTimestamp());

            Double activePower = cpse.getPowerState(Commodity.ACTIVEPOWER);
            if (activePower != null) {
                powerDetails.setActivePower(activePower);
            }
            Double reactivePower = cpse.getPowerState(Commodity.REACTIVEPOWER);
            if (reactivePower != null) {
                powerDetails.setReactivePower(reactivePower);
            }
        } else if (sx instanceof BusDeviceStatusDetails) {
            BusDeviceStatusDetails bdsd = (BusDeviceStatusDetails) sx;
            RestBusDeviceStatusDetails busdevDetails = this.getRestStateDetails(uuid).getBusDeviceStatusDetails();

            busdevDetails.setSender(bdsd.getSender());
            busdevDetails.setTimestamp(bdsd.getTimestamp());
            busdevDetails.setState(RestBusDeviceStatusDetails.ConnectionStatus.valueOf(bdsd.getState().name()));
        } else if (sx instanceof SwitchDriverDetails) {
            SwitchDriverDetails sdd = (SwitchDriverDetails) sx;
            RestSwitchDetails switchDetails = this.getRestStateDetails(uuid).getSwitchDetails();

            switchDetails.setSender(sdd.getSender());
            switchDetails.setTimestamp(sdd.getTimestamp());
            switchDetails.setOn(sdd.isOn());
        } else if (sx instanceof GenericApplianceDriverDetails) {
            GenericApplianceDriverDetails gadd = (GenericApplianceDriverDetails) sx;
            RestApplianceDetails applianceDetails = this.getRestStateDetails(uuid).getApplianceDetails();

            applianceDetails.setSender(gadd.getSender());
            applianceDetails.setTimestamp(gadd.getTimestamp());
            applianceDetails.setState(gadd.getState());
            applianceDetails.setStateTextDE(gadd.getStateTextDE());

            switch (gadd.getState()) {
                case PROGRAMMED:
                case PROGRAMMEDWAITINGTOSTART: {
                    Map<String, String> actionMap = new HashMap<>();
                    actionMap.put("start", "/rs/" + uuid + "/do?action=start");
                    applianceDetails.setActions(actionMap);
                }
                break;
                case RUNNING: {
                    Map<String, String> actionMap = new HashMap<>();
                    actionMap.put("stop", "/rs/" + uuid + "/do?action=stop");
                    applianceDetails.setActions(actionMap);
                }
                break;
                case PHASEHOLD:
                case RINSEHOLD: {
                    Map<String, String> actionMap = new HashMap<>();
                    actionMap.put("start", "/rs/" + uuid + "/do?action=start");
                    actionMap.put("stop", "/rs/" + uuid + "/do?action=stop");
                    applianceDetails.setActions(actionMap);
                }
                break;
                default: {
                    applianceDetails.setActions(Collections.emptyMap());
                }
                break;
            }
        } else if (sx instanceof GenericApplianceProgramDriverDetails) {
            GenericApplianceProgramDriverDetails gapdd = (GenericApplianceProgramDriverDetails) sx;
            RestApplianceDetails applianceDetails = this.getRestStateDetails(uuid).getApplianceDetails();

            applianceDetails.setSender(gapdd.getSender());
            applianceDetails.setTimestamp(gapdd.getTimestamp());

            applianceDetails.setProgramName(gapdd.getProgramName());
            applianceDetails.setPhaseName(gapdd.getPhaseName());

            applianceDetails.setStartTime(gapdd.getStartTime());
            applianceDetails.setEndTime(gapdd.getEndTime());
            applianceDetails.setRemainingTime(gapdd.getRemainingTime());
        } else if (sx instanceof MieleApplianceDriverDetails) {
            MieleApplianceDriverDetails madd = (MieleApplianceDriverDetails) sx;
            RestApplianceDetails applianceDetails = this.getRestStateDetails(uuid).getApplianceDetails();

            applianceDetails.setSender(madd.getSender());
            applianceDetails.setTimestamp(madd.getTimestamp());
            applianceDetails.setExpectedProgramDuration((int) madd.getExpectedProgramDuration());
        } else if (sx instanceof DeviceMetaDriverDetails) {
            DeviceMetaDriverDetails dmdd = (DeviceMetaDriverDetails) sx;
            RestDeviceMetaDetails deviceMetaDetails = this.getRestStateDetails(uuid).getDeviceMetaDetails();

            deviceMetaDetails.setSender(dmdd.getSender());
            deviceMetaDetails.setTimestamp(dmdd.getTimestamp());
            deviceMetaDetails.setConfigured(dmdd.isConfigured());
            deviceMetaDetails.setDeviceClassification(dmdd.getDeviceClassification());
            deviceMetaDetails.setDeviceType(dmdd.getDeviceType());
            deviceMetaDetails.setLocation(dmdd.getLocation());
            deviceMetaDetails.setName(dmdd.getName());
            deviceMetaDetails.setIcon(dmdd.getIcon());
        } else if (sx instanceof ConfigurationDetails) {
            ConfigurationDetails cd = (ConfigurationDetails) sx;
            RestConfigurationDetails configurationDetails = this.getRestStateDetails(uuid).getConfigurationDetails();

            configurationDetails.setSender(cd.getSender());
            configurationDetails.setTimestamp(cd.getTimestamp());
            configurationDetails.setConfigurationStatus(RestConfigurationDetails.ConfigurationStatus.valueOf(cd.getConfigurationStatus().name()));
            configurationDetails.setUsedBy(cd.getUsedBy());
        } else if (sx instanceof TemperatureDetails) {
            TemperatureDetails td = (TemperatureDetails) sx;

            if (td.getTemperature() != null) {
                RestTemperatureDetails tempDetails = this.getRestStateDetails(uuid).getTemperatureDetails();

                tempDetails.setSender(td.getSender());
                tempDetails.setTimestamp(td.getTimestamp());
                tempDetails.setTemperature(td.getTemperature());
                for (Map.Entry<String, Double> ent : td.getAuxiliaryTemperatures().entrySet()) {
                    if (ent.getValue() != null) {
                        tempDetails.setAuxiliaryTemperature(ent.getKey(), ent.getValue());
                    }
                }
            }
        } else if (sx instanceof ExpectedStartTimeExchange) {
            ExpectedStartTimeExchange este = (ExpectedStartTimeExchange) sx;

            if (este.getExpectedStartTime() >= 0) {
                RestScheduleDetails scheduleDetails = this.getRestStateDetails(uuid).getScheduleDetails();
                scheduleDetails.setTimestamp(este.getTimestamp());
                scheduleDetails.setScheduledStartTime(este.getExpectedStartTime());
            } else {
                this.getRestStateDetails(uuid).setScheduleDetails(null);
            }
        }

    }

    // }

    /**
     * Gets state exchanges from ComManager
     */
    @Override
    public void updateDataFromBusManager(IHALExchange exchangeObject) {
        if (exchangeObject instanceof HttpRestInteractionComManagerExchange) {
            StateExchange sx = ((HttpRestInteractionComManagerExchange) exchangeObject).getStateExchange();

            this.storeStateExchange(sx.getSender(), sx, true /* from OC layer */);
        }
    }

    //Register for state changes of any "standard info"
    protected void registerToStateChanges() throws OSHException {
        this.getDriverRegistry().subscribe(DeviceMetaDriverDetails.class, this);

        this.getDriverRegistry().subscribe(ElectricPowerDriverDetails.class, this);

        this.getDriverRegistry().subscribe(BusDeviceStatusDetails.class, this);

        this.getDriverRegistry().subscribe(SwitchDriverDetails.class, this);

        // Generic Appliances
        this.getDriverRegistry().subscribe(GenericApplianceDriverDetails.class, this);
        this.getDriverRegistry().subscribe(GenericApplianceDofDriverDetails.class, this);
        this.getDriverRegistry().subscribe(GenericApplianceProgramDriverDetails.class, this);

        this.getDriverRegistry().subscribe(TemperatureDetails.class, this);
        this.getDriverRegistry().subscribe(ConfigurationDetails.class, this);

        // KIT Miele
        this.getDriverRegistry().subscribe(MieleApplianceDriverDetails.class, this);

    }

    public DriverRegistry getDriverRegistry() {
        return super.getDriverRegistry();
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof StateExchange) {
            this.storeStateExchange(exchange.getSender(), (StateExchange) exchange, false /* from HAL layer */);
        }
    }

    public void sendStartRequest(UUID device) {
        StartDeviceRequest req = new StartDeviceRequest(this.getDeviceID(), device, this.getTimer().getUnixTime());
        this.getDriverRegistry().publish(StartDeviceRequest.class, req);
    }

    public void sendStopRequest(UUID device) {
        StopDeviceRequest req = new StopDeviceRequest(this.getDeviceID(), device, this.getTimer().getUnixTime());
        this.getDriverRegistry().publish(StopDeviceRequest.class, req);
    }

    public void sendSwitchRequest(UUID device, boolean turnOn) {
        SwitchRequest req = new SwitchRequest(this.getDeviceID(), device, this.getTimer().getUnixTime());
        req.setTurnOn(turnOn);
        this.getDriverRegistry().publish(SwitchRequest.class, req);
    }

    @Override
    public HALDriver getSyncObject() {
        return this;
    }

    abstract String getEnvironment();
}