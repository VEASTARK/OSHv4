package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.driver.chp.model.GenericChpModel;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.ChpControllerExchange;
import osh.mgmt.ipp.DachsChpNonControllableIPP;
import osh.mgmt.mox.DachsChpMOX;
import osh.utils.physics.ComplexPowerUtil;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class NonControllableDachsChpLocalController
        extends LocalController {

    private ZonedDateTime lastTimeIppSent;
    private boolean lastSentState;


    private Duration newIPPAfter;
    private double currentHotWaterStorageMinTemp;
    private double currentHotWaterStorageMaxTemp;
    private double fixedCostPerStart;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    private ZonedDateTime runningSince;
    private ZonedDateTime stoppedSince;
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
        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        final ZonedDateTime now = exchange.getTime();
        // get new MOX
        DachsChpMOX mox = (DachsChpMOX) this.getDataFromLocalObserver();

        this.newIPPAfter = mox.getNewIPPAfter();
        this.currentHotWaterStorageMinTemp = mox.getCurrentHotWaterStorageMinTemp();
        this.currentHotWaterStorageMaxTemp = mox.getCurrentHotWaterStorageMaxTemp();
        this.fixedCostPerStart = mox.getFixedCostPerStart();

        this.compressionType = mox.getCompressionType();
        this.compressionValue = mox.getCompressionValue();

        if (mox.isRunning())
            this.lastThermalPower = mox.getThermalPower();

        if (this.lastTimeIppSent == null ||
                now.isAfter(this.lastTimeIppSent.plus(this.newIPPAfter)) || mox.isRunning() != this.lastSentState) {
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
                        now,
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
                this.lastTimeIppSent = now;
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
                    Duration.ofMinutes(15));
        } else if (currentWaterTemp >= this.currentHotWaterStorageMaxTemp) {
            this.stoppedSince = now;
            cx = new ChpControllerExchange(
                    this.getUUID(),
                    now,
                    true,
                    false,
                    false,
                    Duration.ZERO);
        }
        if (cx != null) {
            this.updateOcDataSubscriber(cx);
        }
    }
}
