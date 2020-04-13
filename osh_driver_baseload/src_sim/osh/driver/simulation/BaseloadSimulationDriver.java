package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.logging.devices.BaseloadLogObject;
import osh.datatypes.logging.devices.DevicesLogObject;
import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.BaseloadObserverExchange;
import osh.hal.exchange.BaseloadPredictionExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.database.DatabaseLoggerThread;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.ComplexPowerUtil;
import osh.utils.physics.PhysicalConstants;
import osh.utils.slp.IH0Profile;
import osh.utils.string.ParameterConstants;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Ingo Mauser, Florian Allerding
 */
public class BaseloadSimulationDriver extends DeviceSimulationDriver {

    private final IH0Profile baseload;

    private final double cosPhi;
    private final boolean isInductive;

    private int pastDaysPrediction;
    private float weightForOtherWeekday;
    private float weightForSameWeekday;

    private double sumActivePower;
    private double sumReactivePower;

    /**
     * CONSTRUCTOR
     *
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public BaseloadSimulationDriver(IOSH osh, UUID deviceID,
                                    OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        int yearlyElectricityConsumptionOfHousehold;
        String h0ProfileFileName, h0ClassName;
        try {
            yearlyElectricityConsumptionOfHousehold =
                    Integer.parseInt(driverConfig.getParameter(ParameterConstants.Baseload.yearlyConsumption));
        } catch (Exception e) {
            yearlyElectricityConsumptionOfHousehold = 1000;
            this.getGlobalLogger().logWarning("Can't get yearlyElectricityConsumptionOfHousehold, using the default value: " + yearlyElectricityConsumptionOfHousehold);
        }

        try {
            h0ProfileFileName = this.getDriverConfig().getParameter(ParameterConstants.General_Devices.h0Filename);
            if (h0ProfileFileName == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            h0ProfileFileName = "configfiles/h0/H0ProfileNew.csv";
            this.getGlobalLogger().logWarning("Can't get h0ProfileFileName, using the default value: " + h0ProfileFileName);
        }

        try {
            h0ClassName = this.getDriverConfig().getParameter(ParameterConstants.General_Devices.h0Classname);
            if (h0ClassName == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
            this.getGlobalLogger().logWarning("Can't get h0ClassName, using the default value: " + h0ClassName);
        }

        this.cosPhi = Double.parseDouble(driverConfig.getParameter(ParameterConstants.Baseload.cosPhi));
        this.isInductive = Boolean.parseBoolean(driverConfig.getParameter(ParameterConstants.Baseload.isInductive));

        try {
            this.pastDaysPrediction = Integer.parseInt(driverConfig.getParameter(ParameterConstants.Prediction.pastDaysPrediction));
        } catch (Exception e) {
            this.pastDaysPrediction = 14;
            this.getGlobalLogger().logWarning("Can't get pastDaysPrediction, using the default value: " + this.pastDaysPrediction);
        }

        try {
            this.weightForOtherWeekday = Float.parseFloat(driverConfig.getParameter(ParameterConstants.Prediction.weightForOtherWeekday));
        } catch (Exception e) {
            this.weightForOtherWeekday = 1.0f;
            this.getGlobalLogger().logWarning("Can't get weightForOtherWeekday, using the default value: " + this.weightForOtherWeekday);
        }

        try {
            this.weightForSameWeekday = Float.parseFloat(driverConfig.getParameter(ParameterConstants.Prediction.weightForSameWeekday));
        } catch (Exception e) {
            this.weightForSameWeekday = 5.0f;
            this.getGlobalLogger().logWarning("Can't get weightForSameWeekday, using the default value: " + this.weightForSameWeekday);
        }

        try {
            Class h0Class = Class.forName(h0ClassName);

            this.baseload = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
                    .newInstance(this.getTimeDriver().getCurrentTime().getYear(),
                            h0ProfileFileName,
                            yearlyElectricityConsumptionOfHousehold);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onSimulationIsUp() {
        //initially give LocalObserver load data of past days
        ZonedDateTime timeAtStart = this.getTimeDriver().getTimeAtStart();

        List<SparseLoadProfile> predictions = new LinkedList<>();

        //starting in reverse so that the oldest profile is at index 0 in the list
        for (int i = this.pastDaysPrediction; i >= 1; i--) {
            SparseLoadProfile dayProfile = new SparseLoadProfile();
            ZonedDateTime pastDay = timeAtStart.minusDays(i);
            //loadprofile only provides 365 days so we have to reduce further for leapyears
            if (pastDay.getDayOfYear() > 365) {
                pastDay.minusDays(1);
            }
            for (int sec = 0; sec < 86400; sec++) {
                int activeLoad = this.baseload.getActivePowerAt(pastDay.plusSeconds(sec));

                dayProfile.setLoad(Commodity.ACTIVEPOWER, sec, activeLoad);

                try {
                    dayProfile.setLoad(
                            Commodity.REACTIVEPOWER, sec,
                            (int) ComplexPowerUtil.convertActiveToReactivePower(
                                    activeLoad,
                                    this.cosPhi,
                                    this.isInductive));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dayProfile.setEndingTimeOfProfile(86400);
            predictions.add(dayProfile.getProfileWithoutDuplicateValues());
        }

        BaseloadPredictionExchange _ox = new BaseloadPredictionExchange(
                this.getUUID(), this.getTimeDriver().getCurrentTime(),
                predictions,
                this.pastDaysPrediction, this.weightForOtherWeekday, this.weightForSameWeekday);
        this.notifyObserver(_ox);
    }

    @Override
    public void onNextTimeTick() {
        this.setPower(
                Commodity.ACTIVEPOWER,
                this.baseload.getActivePowerAt(this.getTimeDriver().getCurrentTime()));
        try {
            this.setPower(
                    Commodity.REACTIVEPOWER,
                    (int) ComplexPowerUtil.convertActiveToReactivePower(
                            this.getPower(Commodity.ACTIVEPOWER), this.cosPhi, this.isInductive));
        } catch (Exception e) {
            e.printStackTrace();
        }

        BaseloadObserverExchange blox =
                new BaseloadObserverExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentTime());

        blox.setPower(Commodity.ACTIVEPOWER, this.getPower(Commodity.ACTIVEPOWER));
        blox.setPower(Commodity.REACTIVEPOWER, this.getPower(Commodity.REACTIVEPOWER));

        this.sumActivePower += (double) this.getPower(Commodity.ACTIVEPOWER);
        this.sumReactivePower += (double) this.getPower(Commodity.REACTIVEPOWER);

        this.notifyObserver(blox);
    }


    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

        if (this.getOSH().getOSHStatus().isSimulation()) {
            if (DatabaseLoggerThread.isLogDevices()) {
                this.getDriverRegistry().publish(BaseloadLogObject.class, new BaseloadLogObject(this.getUUID(),
                        this.getTimeDriver().getCurrentTime(), this.sumActivePower / PhysicalConstants.factor_wsToKWh,
                        this.sumReactivePower / PhysicalConstants.factor_wsToKWh));
            }
        }
    }

}
