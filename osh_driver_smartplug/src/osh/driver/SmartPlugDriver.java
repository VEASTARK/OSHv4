package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.datatypes.registry.details.common.SwitchDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.energy.ElectricPowerDriverDetails;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.en50523.EN50523DeviceState;
import osh.hal.exchange.SmartPlugObserverExchange;
import osh.hal.interfaces.ISwitchRequest;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.*;


/**
 * Generic Meter / Switch Device Driver<br>
 * <br>
 * Configure via Parameter "MeterDataUUIDs" and "SwitchDataUUIDs", which are
 * comma separated lists of UUIDs of real metering devices.<br>
 * Data from more than one device are aggregated (e.g. power of all 3-phases summed into one value)
 *
 * @author Kaibin Bao, Ingo Mauser
 */
public class SmartPlugDriver extends HALDeviceDriver implements IDataRegistryListener {

    private DeviceMetaDriverDetails deviceMetaDetails;
    private List<UUID> meterDataSources;
    private List<UUID> switchDataSources;
    private boolean generateApplianceData;

    private int incompleteCounter;

    private boolean updateElectricPowerDriverState;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public SmartPlugDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }


    /* ********************
     * methods
     */

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();
        this.initSmartPlug(this.getDriverConfig());

        this.getDriverRegistry().subscribe(ElectricPowerDriverDetails.class, this,this);
        this.getDriverRegistry().subscribe(SwitchDriverDetails.class, this, this);
    }

    private void initSmartPlug(OSHParameterCollection config) throws OSHException {
        // prepare device details
        this.deviceMetaDetails = new DeviceMetaDriverDetails(this.getUUID(), this.getTimer().getUnixTime());
        this.deviceMetaDetails.setName(config.getParameter("name"));
        this.deviceMetaDetails.setLocation(config.getParameter("location"));
        if (this.getDeviceType() != null) {
            this.deviceMetaDetails.setDeviceType(this.getDeviceType());
        }

        if (this.getDeviceClassification() != null) {
            this.deviceMetaDetails.setDeviceClassification(this.getDeviceClassification());
        }

        // generate appliance data from power data
        if (config.getParameter("generateappliancedata") != null &&
                config.getParameter("generateappliancedata").equals("true")) {
            this.generateApplianceData = true;
        }

        // set data sources
        String cfgMeterSources = config.getParameter("metersources");
        if (cfgMeterSources != null)
            this.meterDataSources = this.parseUUIDArray(cfgMeterSources);
        else
            this.meterDataSources = Collections.emptyList();

        if (this.meterDataSources.contains(this.getUUID())) {
            this.getGlobalLogger().logWarning("metersources can not contain own UUID! smart plug uuid: " + this.getUUID());
            this.updateElectricPowerDriverState = false;
        }

        // optional
        String cfgSwitch = config.getParameter("switch");
        if (cfgSwitch != null)
            this.switchDataSources = this.parseUUIDArray(cfgSwitch);
        else
            this.switchDataSources = Collections.emptyList();

        // set device meta details in driver registry
        this.getDriverRegistry().publish(DeviceMetaDriverDetails.class, this.deviceMetaDetails);

        // set configuration details for meter and switch sources
        this.setDataSourcesUsed(this.meterDataSources);
        this.setDataSourcesUsed(this.switchDataSources);
        this.setDataSourcesConfigured(Collections.singleton(this.getUUID()));
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        if (controllerRequest instanceof ISwitchRequest) {
            for (UUID switchUUID : this.switchDataSources) {
                SwitchRequest switchReq = new SwitchRequest(controllerRequest.getDeviceID(), switchUUID, controllerRequest.getTimestamp());
                this.getDriverRegistry().publish(SwitchRequest.class, switchReq);
            }
        }
    }

    public SmartPlugObserverExchange updateHALExchange() throws OSHException {
        SmartPlugObserverExchange _ox
                = new SmartPlugObserverExchange(this.getUUID(), this.getTimer().getUnixTime());

        // Set DeviceMetaDetails
        _ox.setDeviceMetaDetails(this.deviceMetaDetails);

        // aggregate power data
        {
            ArrayList<ElectricPowerDriverDetails> pdList = new ArrayList<>();
            UUID meterUUID = null;

            for (UUID sourceUUID : this.meterDataSources) {
                ElectricPowerDriverDetails p = (ElectricPowerDriverDetails) this.getDriverRegistry().getData(
                        ElectricPowerDriverDetails.class, sourceUUID);

                if (p == null) {
                    // unable to fetch state
                    if (this.incompleteCounter == 0) {
                        this.getGlobalLogger().logWarning("incomplete data source(s) (device: " + this.getUUID() + " meterDataSource: " + sourceUUID + ")");
                    }
                    this.incompleteCounter++;
                    return null;
                }

                pdList.add(p);

                if (meterUUID == null)
                    meterUUID = p.getMeterUuid();
            }

            ElectricPowerDriverDetails aggregated = ElectricPowerDriverDetails.aggregatePowerDetails(this.getUUID(), pdList);
            _ox.setActivePower((int) Math.round(aggregated.getActivePower()));
            _ox.setReactivePower((int) Math.round(aggregated.getReactivePower()));

            if (this.updateElectricPowerDriverState) {
                this.getDriverRegistry().publish(ElectricPowerDriverDetails.class, aggregated);
            }
        }

        // aggregate switch data
        {
            int _sdCount = 0;
            boolean ambiguousState = false; // if one switch is on and another is off
            SwitchDriverDetails _sd = new SwitchDriverDetails(_ox.getDeviceID(), _ox.getTimestamp());

            for (UUID sourceUUID : this.switchDataSources) {
                SwitchDriverDetails s = (SwitchDriverDetails) this.getDriverRegistry().getData(SwitchDriverDetails.class, sourceUUID);
                if (s == null) {
                    // unable to fetch state
                    if (this.incompleteCounter == 0) {
                        this.getGlobalLogger().logWarning("incomplete data source(s) (device: " + this.getUUID() + " switchDataSources: " + sourceUUID + ")");
                    }
                    this.incompleteCounter++;
                    return null;
                }

                if (_sdCount == 0) {
                    _sd.setOn(s.isOn());
                } else {
                    if (_sd.isOn() != s.isOn())
                        ambiguousState = true;
                }

                _sdCount++;
            }

            if (!ambiguousState) {
                if (_sdCount > 0) {
                    _ox.setOn(_sd.isOn());
                } else {
                    //no switch available (for example: house connection)
                    _ox.setOn(true);
                }
            } else {
                throw new OSHException("ERROR: setting undefined state (switchDataSources: " + Arrays.toString(this.switchDataSources.toArray()) + ", meterDataSources: " + Arrays.toString(this.meterDataSources.toArray()) + ")");
            }
        }

        //all data is available, reset incomplete counter
        if (this.incompleteCounter > 0) {
            this.getGlobalLogger().logWarning("data source(s) for device: " + this.getUUID() + " are available again after " + this.incompleteCounter);
        }
        this.incompleteCounter = 0;

        this.notifyObserver(_ox);

        // generate appliance data
        if (this.generateApplianceData) {
            GenericApplianceDriverDetails appDetails = new GenericApplianceDriverDetails(this.getUUID(), _ox.getTimestamp());
            if (_ox.isOn()) {
                if (_ox.getActivePower() > 5)
                    appDetails.setState(EN50523DeviceState.RUNNING);
                else
                    appDetails.setState(EN50523DeviceState.STANDBY);
            } else {
                appDetails.setState(EN50523DeviceState.OFF);
            }
            appDetails.setStateTextDE(appDetails.getState().getDescriptionDE());
            this.getDriverRegistry().publish(GenericApplianceDriverDetails.class, this, appDetails);
        }

        return _ox;
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (this.meterDataSources.contains(exchange.getSender())
                || this.switchDataSources.contains(exchange.getSender())) {
            try {
                this.updateHALExchange();
            } catch (OSHException e) {
                this.getGlobalLogger().logWarning(e);
                e.printStackTrace();
            }
        }
    }
}
