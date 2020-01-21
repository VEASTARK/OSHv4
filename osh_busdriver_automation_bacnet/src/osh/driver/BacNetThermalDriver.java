package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.driver.bacnet.BacNetDispatcher;
import osh.driver.bacnet.BacNetDispatcher.BacNetObject;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.hal.exchange.BacNetThermalExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * BacNet/IP thermal sensors and A/C control
 *
 * @author Kaibin Bao
 */
@Deprecated
public class BacNetThermalDriver extends HALDeviceDriver {

    public static final String TEMPERATURE_KEY_SET_POINT = "setpoint";
    private static final Pattern ACTUATOR_PATTERN = Pattern.compile("[\\[\\]]");
    private static BacNetDispatcher dispatcher;
    private DeviceMetaDriverDetails deviceMetaDetails;
    private BacNetObject sensorObject;
    private List<BacNetObject> actuatorObjects;
    private String sensorObjectName;
    private String actuatorObjectName;


    /**
     * CONSTRUCTOR
     *
     * @param osh          osh reference
     * @param deviceID     UUID of this thermal driver
     * @param driverConfig parameter configuration of this driver
     */
    public BacNetThermalDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }

    private void init(OSHParameterCollection config) throws OSHException {
        this.deviceMetaDetails = new DeviceMetaDriverDetails(this.getDeviceID(), this.getTimer().getUnixTime());
        this.deviceMetaDetails.setName(config.getParameter("name"));
        this.deviceMetaDetails.setLocation(config.getParameter("location"));
        // deviceDetails.setDeviceType(getDeviceType().toString());
        // deviceDetails.setDeviceClass(getDeviceClassification().toString());

        if (dispatcher == null) {
            dispatcher = new BacNetDispatcher(this.getTimer(), this.getGlobalLogger());
            try {
                dispatcher.init();
            } catch (IOException e) {
                throw new OSHException("could not initialize BacNet dispatcher", e);
            }
        }

        String bacNetController = config.getParameter("controller");
        if (bacNetController == null || bacNetController.length() <= 0)
            throw new OSHException("Invalid config parameter: controller");

        dispatcher.addDevice(bacNetController, 47808);

        this.sensorObjectName = config.getParameter("sensor");
        {
            if (this.sensorObjectName.length() <= 0)
                throw new OSHException("Invalid Sensor");

            String[] oid = this.sensorObjectName.split("/");
            if (oid.length != 2)
                throw new OSHException("Invalid Sensor");

            try {
                int devOid = Integer.parseInt(oid[0]); // device id of bacnet controller
                int objOid = Integer.parseInt(oid[1]); // sensor object id
                this.sensorObject = new BacNetObject(devOid, objOid);
            } catch (NumberFormatException e) {
                throw new OSHException("Invalid Sensor", e);
            }
        }

        this.actuatorObjects = new ArrayList<>();
        // works? get setpoints from config
        this.actuatorObjectName = config.getParameter("actuator");
        {
            if (this.actuatorObjectName.length() <= 0)
                throw new OSHException("Invalid Actuator");

            // 4 combinations of deviceId, objectId
            String[] actuators = ACTUATOR_PATTERN.matcher(this.actuatorObjectName).replaceAll("").split(",");

            // for
            for (String actuator : actuators) {
                String[] oid = actuator.split("/");
                if (oid.length != 2)
                    throw new OSHException("Invalid Actuator");

                try {
                    int devOid = Integer.parseInt(oid[0]); // device id of bacnet controller
                    int objOid = Integer.parseInt(oid[1]); // actuator object id
                    this.actuatorObjects.add(new BacNetObject(devOid, objOid));
                } catch (NumberFormatException e) {
                    throw new OSHException("Invalid Actuator", e);
                }
            }
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        this.init(this.getDriverConfig());
        this.getTimer().registerComponent(this, 1);
        super.onSystemIsUp();
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        // build new HAL exchange
        BacNetThermalExchange _ox = this.buildObserverExchange();
        this.notifyObserver(_ox);

        // create TemperatureDetails and save to DriverRegistry
        this.getDriverRegistry().publish(TemperatureDetails.class, _ox.getTemperatureDetails());

    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        //NOTHING
    }

    private BacNetThermalExchange buildObserverExchange() {
        BacNetThermalExchange _ox = new BacNetThermalExchange(this.getDeviceID(), this.getTimer().getUnixTime());

        _ox.setDeviceMetaDetails(this.deviceMetaDetails);

        TemperatureDetails _td = new TemperatureDetails(this.getDeviceID(), this.getTimer().getUnixTime());

        _td.setTemperature(dispatcher.getAnalogInputState(this.sensorObject));

        // get setpoints from dispatcher and use average setpoint
        double average = 0.0;
        int i;
        for (i = 0; i < this.actuatorObjects.size(); i++) {
            BacNetObject obj = this.actuatorObjects.get(i);
            Double temp = dispatcher.getAnalogValueState(obj);
            if (temp == null)
                temp = 0.0;
            average += temp;
        }
        average /= (i + 1);

        // vorher: 22.0
        _td.addAuxiliaryTemperatures(TEMPERATURE_KEY_SET_POINT, average);

        _ox.setTemperatureDetails(_td);

        return _ox;
    }

    // Hannah
    public List<BacNetObject> getActuatorObjects() {
        return this.actuatorObjects;

    }
}
