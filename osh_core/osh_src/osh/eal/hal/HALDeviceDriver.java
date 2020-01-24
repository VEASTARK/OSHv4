package osh.eal.hal;

import osh.configuration.OSHParameterCollection;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.oc.IOCHALDataSubscriber;
import osh.core.oc.LocalController;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.details.common.ConfigurationDetails;
import osh.datatypes.registry.details.common.ConfigurationDetails.ConfigurationStatus;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.eal.hal.exceptions.HALDriverException;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.IHALExchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.UUID;

/**
 * superclass for the drivers
 *
 * @author Florian Allerding, Ingo Mauser
 */
public abstract class HALDeviceDriver
        extends HALDriver
        implements IDriverDataPublisher, IOCHALDataSubscriber {

    private HALControllerExchange controllerDataObject;

    private Class<LocalObserver> requiredLocalObserverClass;
    private Class<LocalController> requiredLocalControllerClass;
    private IDriverDataSubscriber assignedLocalObserver;

    private boolean observable;
    private boolean controllable;

    // ### DeviceMetaDriverDetails ###
    private String name;
    private String location;
    private final String icon;

    private DeviceClassification deviceClassification;
    private DeviceTypes deviceType;
    private boolean configured;

    private boolean intelligent;

    private final ArrayList<UUID> meterUuids;
    private final ArrayList<UUID> switchUuids;

    // load
    private final EnumMap<Commodity, Integer> powerLoadForCommodity;


    /**
     * CONSTRUCTOR
     */
    public HALDeviceDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        this.name = driverConfig.getParameter("name");
        this.location = driverConfig.getParameter("location");
        this.icon = driverConfig.getParameter("icon");

        this.meterUuids = new ArrayList<>();
        this.switchUuids = new ArrayList<>();

        // Add meter uuids (sources for metering/measurement)
        String cfgMeterSources = driverConfig.getParameter("metersources");
        if (cfgMeterSources != null) {
            ArrayList<UUID> meterSources;
            try {
                meterSources = this.parseUUIDArray(cfgMeterSources);
                this.meterUuids.addAll(meterSources);
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }

        this.powerLoadForCommodity = new EnumMap<>(Commodity.class);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // set data sources as configured
        DeviceMetaDriverDetails deviceMetaDriverDetails = new DeviceMetaDriverDetails(
                this.getUUID(),
                this.getTimeDriver().getCurrentEpochSecond());
        deviceMetaDriverDetails.setName(this.name);
        deviceMetaDriverDetails.setLocation(this.location);
        deviceMetaDriverDetails.setIcon(this.icon);
        deviceMetaDriverDetails.setDeviceClassification(this.deviceClassification);
        deviceMetaDriverDetails.setDeviceType(this.deviceType);
        deviceMetaDriverDetails.setConfigured(this.configured);

        this.getDriverRegistry().publish(DeviceMetaDriverDetails.class, deviceMetaDriverDetails);
    }


    /**
     * Simplify the notification to the local observer
     *
     * @param observerExchange exchange data for the observer
     */
    public void notifyObserver(IHALExchange observerExchange) {
        try {
            this.updateOcDataSubscriber(observerExchange);
        } catch (HALDriverException e) {
            this.getGlobalLogger().logError("HAL communication error", e);
        }
    }

    /**
     * invoked from the local controller when the device receives an CX object
     *
     * @param controllerRequest contains what the device has to do...
     * @throws HALException on error processing the controller request
     */
    protected abstract void onControllerRequest(HALControllerExchange controllerRequest) throws HALException;


    // IOCHALDataSubscriber

    @Override
    public void onDataFromOcComponent(IHALExchange controllerObject) {

        if (controllerObject instanceof HALControllerExchange) {
            this.controllerDataObject = (HALControllerExchange) controllerObject;
        }
        try {
            this.changeDeviceState();
        } catch (HALException e) {
            throw new RuntimeException("Uncaught HAL-Exception...Very BAD!", e);
        }

    }

    private void changeDeviceState() throws HALException {
        this.onControllerRequest(this.controllerDataObject);
    }


    // IDriverDataPublisher (from former HALobserverDriver)

    @Override
    public void setOcDataSubscriber(IDriverDataSubscriber observerObject) {
        this.assignedLocalObserver = observerObject;
    }

    @Override
    public void removeOcDataSubscriber(IDriverDataSubscriber observerObject) {
        //FIXME add removal routine
    }

    @Override
    public void updateOcDataSubscriber(IHALExchange halExchange) throws HALDriverException {
        if (this.assignedLocalObserver != null) {
            try {
                this.assignedLocalObserver.onDataFromCALDriver(halExchange);
            } catch (OSHException e) {
                throw new HALDriverException(e);
            }
        }
    }


    /* HELPER FUNCTIONS */
    public void setDataSourcesConfigured(Collection<UUID> uuids) {
        for (UUID uuid : uuids) {
            ConfigurationDetails cd = new ConfigurationDetails(uuid, this.getTimeDriver().getCurrentEpochSecond());
            cd.setConfigurationStatus(ConfigurationStatus.CONFIGURED);
            this.getDriverRegistry().publish(ConfigurationDetails.class, cd);
        }
    }

    public void setDataSourcesUsed(Collection<UUID> uuids) {
        for (UUID uuid : uuids) {
            ConfigurationDetails cd = new ConfigurationDetails(uuid, this.getTimeDriver().getCurrentEpochSecond());
            cd.setConfigurationStatus(ConfigurationStatus.USED);
            cd.setUsedBy(this.getUUID());
            this.getDriverRegistry().publish(ConfigurationDetails.class, cd);
        }
    }


    public DeviceClassification getDeviceClassification() {
        return this.deviceClassification;
    }

    protected void setDeviceClassification(
            DeviceClassification deviceClassification) {
        this.deviceClassification = deviceClassification;
    }


    public DeviceTypes getDeviceType() {
        return this.deviceType;
    }

    protected void setDeviceType(DeviceTypes deviceType) {
        this.deviceType = deviceType;
    }


    public Class<LocalController> getRequiredLocalControllerClass() {
        return this.requiredLocalControllerClass;
    }

    protected void setRequiredLocalControllerClass(
            Class<LocalController> requiredLocalControllerClass) {
        this.requiredLocalControllerClass = requiredLocalControllerClass;
    }


    public Class<LocalObserver> getRequiredLocalObserverClass() {
        return this.requiredLocalObserverClass;
    }

    protected void setRequiredLocalObserverClass(
            Class<LocalObserver> requiredLocalObserverClass) {
        this.requiredLocalObserverClass = requiredLocalObserverClass;
    }


    public boolean isControllable() {
        return this.controllable;
    }

    protected void setControllable(boolean controllable) {
        this.controllable = controllable;
    }


    public boolean isIntelligent() {
        return this.intelligent;
    }

    protected void setIntelligent(boolean isIntelligent) {
        this.intelligent = isIntelligent;
    }


    public boolean isObservable() {
        return this.observable;
    }

    protected void setObservable(boolean observable) {
        this.observable = observable;
    }


    public ArrayList<UUID> getMeterUuids() {
        return this.meterUuids;
    }

    protected void addMeterUUID(UUID meterUUID) {
        this.meterUuids.add(meterUUID);
    }


    public ArrayList<UUID> getSwitchUuids() {
        return this.switchUuids;
    }

    protected void addSwitchUUID(UUID switchUUID) {
        this.switchUuids.add(switchUUID);
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getLocation() {
        return this.location;
    }


    public void setLocation(String location) {
        this.location = location;
    }


    /**
     * Get the Power consumption / Load
     *
     * @param c Commodity
     * @return power/load
     */
    public Integer getPower(Commodity c) {
        return this.powerLoadForCommodity.get(c);
    }

    /**
     * Set the Power consumption / load
     *
     * @param c     Commodity
     * @param power power value to set
     */
    protected void setPower(Commodity c, int power) {
        this.powerLoadForCommodity.put(c, power);
    }

}
