package osh.comdriver.logging;

import osh.OSH;
import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.logging.LoggingConfigurationStateExchange;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.datatypes.logging.devices.BaseloadLogObject;
import osh.datatypes.logging.devices.DevicesLogObject;
import osh.datatypes.logging.devices.SmartHeaterLogObject;
import osh.datatypes.logging.devices.WaterTankLogObject;
import osh.datatypes.logging.electrical.DetailedPowerLogObject;
import osh.datatypes.logging.electrical.H0LogObject;
import osh.datatypes.logging.general.EALogObject;
import osh.datatypes.logging.general.PowerLimitSignalLogObject;
import osh.datatypes.logging.general.PriceSignalLogObject;
import osh.datatypes.logging.general.SimulationResultsLogObject;
import osh.datatypes.logging.thermal.ThermalLoggingObject;
import osh.datatypes.logging.thermal.ThermalSupplyLogObject;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.StateExchange;
import osh.registry.Registry;
import osh.registry.Registry.DriverRegistry;
import osh.registry.Registry.OCRegistry;
import osh.registry.interfaces.IDataRegistryListener;
import osh.simulation.database.DatabaseLoggerThread;
import osh.utils.string.ParameterConstants;
import osh.utils.string.StringConversions;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Represents a {@link CALComDriver} that distributes information about the logging configuration and collects,
 * transforms and disributes any emitted {@link LoggingObjectStateExchange} log-object.
 *
 * @author Sebastian Kramer
 */
public class OSHLoggingComDriver extends CALComDriver {

    //logging intervals
    protected List<Long[]> loggingIntervals = new ArrayList<>();
    protected boolean logDetailedPower;
    protected boolean logEpsPls;
    protected boolean logH0;
    protected boolean logIntervals;
    protected boolean logDevices;
    protected boolean logBaseload;
    protected boolean logThermal;
    protected boolean logWaterTank;
    protected boolean logEA;
    protected boolean logSmartHeater;
    protected boolean isLogToDatabase;

    /**
     * Constructs this com-driver with the given osh-entitiy, the unquie identifier for this com-driver and the
     * parameter configuration.
     *
     * @param entity the osh entitiy
     * @param deviceID the unique device entitiy
     * @param driverConfig the parameter configuration
     *
     */
    public OSHLoggingComDriver(IOSH entity, UUID deviceID, OSHParameterCollection driverConfig) {
        super(entity, deviceID, driverConfig);

        try {
            this.logH0 = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logH0));
        } catch (Exception e) {
            this.logH0 = false;
        }

        try {
            this.logIntervals = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logIntervals));
        } catch (Exception e) {
            this.logIntervals = false;
        }

        try {
            this.logDevices = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logDevices));
        } catch (Exception e) {
            this.logDevices = false;
        }

        try {
            this.logThermal = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logThermal));
        } catch (Exception e) {
            this.logThermal = false;
        }

        try {
            this.logEpsPls = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logEpsPls));
        } catch (Exception e) {
            this.logEpsPls = false;
        }

        try {
            this.logDetailedPower = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logDetailedPower));
        } catch (Exception e) {
            this.logDetailedPower = false;
        }

        try {
            this.logWaterTank = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logWaterTank));
        } catch (Exception e) {
            this.logWaterTank = false;
        }

        try {
            this.logEA = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logEA));
        } catch (Exception e) {
            this.logEA = false;
        }

        try {
            this.logSmartHeater = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Logging.logSmartHeater));
        } catch (Exception e) {
            this.logSmartHeater = false;
        }


        String loggingIntervalsAsArray = null;

        try {
            loggingIntervalsAsArray = driverConfig.getParameter(ParameterConstants.Logging.loggingIntervals);
        } catch (Exception ignored) {
        }

        if (loggingIntervalsAsArray != null && loggingIntervalsAsArray.length() > 2) {
            Long[][] tmp = StringConversions.fromStringTo2DimLongArray(loggingIntervalsAsArray);
            Collections.addAll(this.loggingIntervals, tmp);
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.isLogToDatabase = DatabaseLoggerThread.isLogToDatabase();

        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        LoggingConfigurationStateExchange lcse1 = new LoggingConfigurationStateExchange(this.getUUID(), now, this.isLogToDatabase,
                this.loggingIntervals, this.logDetailedPower, this.logEpsPls,
                this.logH0, this.logIntervals, this.logDevices, this.logBaseload, this.logThermal, this.logWaterTank, this.logEA, this.logSmartHeater);

        LoggingConfigurationStateExchange lcse2 = new LoggingConfigurationStateExchange(this.getUUID(), now, this.isLogToDatabase,
                this.loggingIntervals, this.logDetailedPower, this.logEpsPls,
                this.logH0, this.logIntervals, this.logDevices, this.logBaseload, this.logThermal, this.logWaterTank, this.logEA, this.logSmartHeater);

        LoggingConfigurationStateExchange lcse3 = new LoggingConfigurationStateExchange(this.getUUID(), now, this.isLogToDatabase,
                this.loggingIntervals, this.logDetailedPower, this.logEpsPls,
                this.logH0, this.logIntervals, this.logDevices, this.logBaseload, this.logThermal, this.logWaterTank, this.logEA, this.logSmartHeater);

        this.getComRegistry().publish(LoggingConfigurationStateExchange.class, this.getUUID(), lcse1);
        this.getOCRegistry().publish(LoggingConfigurationStateExchange.class, this.getUUID(), lcse2);
        this.getDriverRegistry().publish(LoggingConfigurationStateExchange.class, this.getUUID(), lcse3);

        this.subscribeForRegistries();
    }

    private void subscribeForRegistries() {
        this.getComRegistry().subscribe(LoggingObjectStateExchange.class, new RegistryListener(this.getComRegistry()));
        this.getOCRegistry().subscribe(LoggingObjectStateExchange.class, new RegistryListener(this.getOCRegistry()));
        this.getDriverRegistry().subscribe(LoggingObjectStateExchange.class, new RegistryListener(this.getDriverRegistry()));
    }

    private void eventReciever(StateExchange exchangeObject) {

        if (exchangeObject instanceof ThermalLoggingObject) {
            this.sendDataToDatabase(ThermalLoggingObject.class, (ThermalLoggingObject) exchangeObject);
        } else if (exchangeObject instanceof SimulationResultsLogObject) {
            this.sendDataToDatabase(SimulationResultsLogObject.class, (SimulationResultsLogObject) exchangeObject);
        } else if (exchangeObject instanceof PriceSignalLogObject) {
            this.sendDataToDatabase(PriceSignalLogObject.class, (PriceSignalLogObject) exchangeObject);
        } else if (exchangeObject instanceof PowerLimitSignalLogObject) {
            this.sendDataToDatabase(PowerLimitSignalLogObject.class, (PowerLimitSignalLogObject) exchangeObject);
        } else if (exchangeObject instanceof DetailedPowerLogObject) {
            // TODO average to 60 sec (variable according to grid simulation interval (read from config)) values
            //detailed power is a load profile, which has methods to compress to timeslots
//			((DetailedPowerLogObject) exchangeObject).getLoadProfile().getCompressedProfile(LoadProfileCompressionTypes.TIMESLOTS, 0, 60);
            this.sendDataToDatabase(DetailedPowerLogObject.class, (DetailedPowerLogObject) exchangeObject);
        } else if (exchangeObject instanceof H0LogObject) {
            this.sendDataToDatabase(H0LogObject.class, (H0LogObject) exchangeObject);
        } else if (exchangeObject instanceof BaseloadLogObject) {
            this.sendDataToDatabase(BaseloadLogObject.class, (BaseloadLogObject) exchangeObject);
        } else if (exchangeObject instanceof DevicesLogObject) {
            this.sendDataToDatabase(DevicesLogObject.class, (DevicesLogObject) exchangeObject);
        } else if (exchangeObject instanceof SmartHeaterLogObject) {
            this.sendDataToDatabase(SmartHeaterLogObject.class, (SmartHeaterLogObject) exchangeObject);
        } else if (exchangeObject instanceof WaterTankLogObject) {
            this.sendDataToDatabase(WaterTankLogObject.class, (WaterTankLogObject) exchangeObject);
        } else if (exchangeObject instanceof ThermalSupplyLogObject) {
            this.sendDataToDatabase(ThermalSupplyLogObject.class, (ThermalSupplyLogObject) exchangeObject);
        } else if (exchangeObject instanceof EALogObject) {
            this.sendDataToDatabase(EALogObject.class, (EALogObject) exchangeObject);
        } else {
            throw new IllegalArgumentException("Handling of log object of type: " + exchangeObject.getClass() + " is not implemented");
        }
        //TODO: do pre-processing if desired
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //nothing
    }

    //This is a logging driver, so these tricks are okay, but generally avoid doing something like this
    private OCRegistry getOCRegistry() {
        return ((OSH) this.getOSH()).getOCRegistry();
    }

    private DriverRegistry getDriverRegistry() {
        return ((OSH) this.getOSH()).getDriverRegistry();
    }

    private <T extends StateExchange, U extends T> void sendDataToDatabase(Class<T> objClass, U exchangeObject) {

        if (!this.isLogToDatabase) {
            return;
        }

        exchangeObject.setSender(this.getUUID());

        assert (exchangeObject instanceof LoggingObjectStateExchange);
        DatabaseLoggerThread.enqueue((LoggingObjectStateExchange) exchangeObject);
    }

    private class RegistryListener implements IDataRegistryListener {

        protected final Registry registry;

        protected RegistryListener(Registry registry) {
            this.registry = registry;
        }

        @Override
        public <T extends AbstractExchange> void onExchange(final T exchange) {
            if (exchange instanceof StateExchange) {
                OSHLoggingComDriver.this.eventReciever(((StateExchange) exchange));
            }
        }
    }
}
