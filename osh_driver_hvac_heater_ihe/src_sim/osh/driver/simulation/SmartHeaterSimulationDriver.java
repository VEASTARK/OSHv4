package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.driver.ihe.SmartHeaterModel;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.hal.exchange.SmartHeaterOX;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

import java.time.Duration;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class SmartHeaterSimulationDriver extends DeviceSimulationDriver {

    private final int INITIAL_STATE = 0;
    private final long[] INITIAL_LAST_CHANGE = {-1, -1, -1}; // do NOT use MIN_VALUE!!!
    private int temperatureSetting;
    private Duration newIppAfter;
    private int triggerIppIfDeltaTempBigger;

    private SmartHeaterModel model;
    private double currentWaterTemperature;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public SmartHeaterSimulationDriver(IOSH osh, UUID deviceID,
                                       OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        try {
            this.temperatureSetting = Integer.parseInt(this.getDriverConfig().getParameter("temperatureSetting"));
        } catch (Exception e) {
            this.temperatureSetting = 80;
            this.getGlobalLogger().logWarning("Can't get temperatureSetting, using the default value: " + this.temperatureSetting);
        }

        try {
            this.newIppAfter = Duration.ofSeconds(Long.parseLong(this.getDriverConfig().getParameter("newIppAfter")));
        } catch (Exception e) {
            this.newIppAfter = Duration.ofHours(1);
            this.getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
        }

        try {
            this.triggerIppIfDeltaTempBigger = Integer.parseInt(this.getDriverConfig().getParameter("triggerIppIfDeltaTempBigger"));
        } catch (Exception e) {
            this.triggerIppIfDeltaTempBigger = 1;
            this.getGlobalLogger().logWarning("Can't get triggerIppIfDeltaTempBigger, using the default value: " + this.triggerIppIfDeltaTempBigger);
        }

        this.model = new SmartHeaterModel(
                this.temperatureSetting,
                this.INITIAL_STATE,
                this.INITIAL_LAST_CHANGE);
    }

    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();

        IPPSchedulingExchange _ise = new IPPSchedulingExchange(this.getUUID(), this.getTimeDriver().getCurrentTime());
        _ise.setNewIppAfter(this.newIppAfter);
        _ise.setTriggerIfDeltaX(this.triggerIppIfDeltaTempBigger);
        this.notifyObserver(_ise);
    }

    @Override
    public void onSystemShutdown() {
        if (DatabaseLoggerThread.isLogSmartHeater()) {
            DatabaseLoggerThread.enqueueSmartHeater(this.model.getCounter(), this.model.getRuntime(), this.model.getPowerTierRunTimes());
        }
    }

    @Override
    public void onNextTimeTick() {
        int availablePower = 0;
//		if (ancillaryInputStates != null) {
        if (this.ancillaryMeterState != null) {

            // #1
//			double chpFeedIn = 0;
//			double pvFeedIn = 0;
//			if (ancillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null) {
//				chpFeedIn = Math.abs(ancillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPower());
//			}
//			if (ancillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null) {
//				pvFeedIn = Math.abs(ancillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPower());
//			}
//			availablePower = (int) (chpFeedIn + pvFeedIn);

            // #2
//			if (ancillaryInputStates.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL) != null) {
            // iff < 0 -> use it with IHE
//				availablePower = (int) ancillaryInputStates.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPower();

            availablePower = (int) this.ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);

//				if (availablePower < -600) {
//					@SuppressWarnings("unused")
//					int xxx = 0;
//				}
//			}
        }

        this.model.updateAvailablePower(this.getTimeDriver().getCurrentEpochSecond(), availablePower, this.currentWaterTemperature);

        int activePower = (int) this.model.getPower();
        int hotWaterPower = (int) -this.model.getPower();

        this.setPower(Commodity.ACTIVEPOWER, activePower);
        this.setPower(Commodity.REACTIVEPOWER, activePower);
        this.setPower(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);

        if (hotWaterPower != 0) {
            @SuppressWarnings("unused")
            int xxx = 0;
        }

        SmartHeaterOX ox = new SmartHeaterOX(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime(),
                this.temperatureSetting,
                (int) this.currentWaterTemperature,
                this.model.getCurrentState(),
                activePower,
                hotWaterPower,
                this.model.getTimestampOfLastChangePerSubElement());
        this.notifyObserver(ox);
    }

    @Override
    public void setCommodityInputStates(
            LimitedCommodityStateMap inputStates,
            AncillaryMeterState ancillaryMeterState) {
        super.setCommodityInputStates(inputStates, ancillaryMeterState);

        if (inputStates != null) {
            if (inputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
                this.currentWaterTemperature = inputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
            }
        }
    }


    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }

}
