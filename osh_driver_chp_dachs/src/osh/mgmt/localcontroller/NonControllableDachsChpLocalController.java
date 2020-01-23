package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.driver.chp.model.GenericChpModel;
import osh.hal.exchange.ChpControllerExchange;
import osh.mgmt.ipp.DachsChpNonControllableIPP;
import osh.mgmt.mox.DachsChpMOX;
import osh.utils.physics.ComplexPowerUtil;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class NonControllableDachsChpLocalController
        extends LocalController {

    private long lastTimeIppSent = Long.MIN_VALUE;
    private boolean lastSentState;


    private long newIPPAfter;
    private double currentHotWaterStorageMinTemp;
    private double currentHotWaterStorageMaxTemp;
    private double fixedCostPerStart;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    private long runningSince;
    private long stoppedSince;
    private int lastThermalPower;

    /**
     * CONSTRUCTOR
     */
    public NonControllableDachsChpLocalController(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();
        this.getTimeDriver().registerComponent(this, 1);
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();


        // get new MOX
        DachsChpMOX mox = (DachsChpMOX) this.getDataFromLocalObserver();

        long now = this.getTimeDriver().getUnixTime();
        this.newIPPAfter = mox.getNewIPPAfter();
        this.currentHotWaterStorageMinTemp = mox.getCurrentHotWaterStorageMinTemp();
        this.currentHotWaterStorageMaxTemp = mox.getCurrentHotWaterStorageMaxTemp();
        this.fixedCostPerStart = mox.getFixedCostPerStart();

        this.compressionType = mox.getCompressionType();
        this.compressionValue = mox.getCompressionValue();

        if (mox.isRunning())
            this.lastThermalPower = mox.getThermalPower();

        if ((now > this.lastTimeIppSent + this.newIPPAfter) || mox.isRunning() != this.lastSentState) {
            int typicalActivePower = mox.getTypicalActivePower();
            int typicalReactivePower = mox.getTypicalReactivePower();
            int typicalThermalPower = mox.getTypicalThermalPower();
            int typicalGasPower = mox.getTypicalGasPower();
            boolean isRunning = mox.isRunning();

            // new IPP
            boolean toBeScheduled = false;
            DachsChpNonControllableIPP sIPP = null;
            try {
                sIPP = new DachsChpNonControllableIPP(
                        this.getUUID(),
                        this.getGlobalLogger(),
                        this.getTimeDriver().getUnixTime(),
                        toBeScheduled,
                        mox.getMinRuntime(),
                        new GenericChpModel(
                                typicalActivePower,
                                typicalReactivePower,
                                typicalThermalPower,
                                typicalGasPower,
                                ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(typicalActivePower, typicalReactivePower),
                                isRunning,
                                this.lastThermalPower,
                                this.runningSince,
                                this.stoppedSince),
                        isRunning,
                        this.currentHotWaterStorageMinTemp,
                        this.currentHotWaterStorageMaxTemp,
                        mox.getWaterTemperature(),
                        this.fixedCostPerStart,
                        this.compressionType,
                        this.compressionValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.getOCRegistry().publish(
                    InterdependentProblemPart.class, this, sIPP);

            this.lastTimeIppSent = now;
            this.lastSentState = isRunning;
        }

        double currentWaterTemp = mox.getWaterTemperature(); // get it...

        ChpControllerExchange cx = null;
        if (currentWaterTemp <= this.currentHotWaterStorageMinTemp) {
            //starting
            this.runningSince = now;
            cx = new ChpControllerExchange(
                    this.getUUID(),
                    now,
                    false,
                    false,
                    true,
                    15 * 60);
        } else if (currentWaterTemp >= this.currentHotWaterStorageMaxTemp) {
            this.stoppedSince = now;
            cx = new ChpControllerExchange(
                    this.getUUID(),
                    this.getTimeDriver().getUnixTime(),
                    true,
                    false,
                    false,
                    0);
        }
        if (cx != null) {
            this.updateOcDataSubscriber(cx);
        }
    }
}
