package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.ChillerControllerExchange;
import osh.mgmt.ipp.ChillerNonControllableIPP;
import osh.mgmt.mox.AdsorptionChillerMOX;

import java.util.Map;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class NonControllableAdsorptionChillerLocalController
        extends LocalController {

    private final double minColdWaterTemp = 10.0;
    private final double maxColdWaterTemp = 15.0;

    private final double minHotWaterTemp = 60.0;
    private final double maxHotWaterTemp = 80.0;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public NonControllableAdsorptionChillerLocalController(IOSHOC osh) {
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
        // get new Mox
        AdsorptionChillerMOX mox = (AdsorptionChillerMOX) this.getDataFromLocalObserver();

        double currentColdWaterTemp = mox.getColdWaterTemperature();
        double currentHotWaterTemp = mox.getHotWaterTemperature();
        boolean currentState = mox.isRunning();

        this.compressionType = mox.getCompressionType();
        this.compressionValue = mox.getCompressionValue();
        Map<Long, Double> temperaturePrediction = mox.getTemperatureMap();

        boolean toBeScheduled = false;
        // #1 Ask for rescheduling if temperature is above a certain threshold
//		if ( !currentState && currentColdWaterTemp >= maxColdWaterTemp - 0.1) {
//			toBeScheduled = true;
//		}

        // #2 Ask for rescheduling after a certain time (e.g. 6 hours)
        //TODO

        // #3 Ask for rescheduling after BIG changes...
        //TODO

        // new IPP
        ChillerNonControllableIPP ipp = new ChillerNonControllableIPP(
                this.getUUID(),
                exchange.getTime(),
                toBeScheduled,
                currentState,
                temperaturePrediction,
                this.compressionType,
                this.compressionValue);
        this.getOCRegistry().publish(
                InterdependentProblemPart.class, this, ipp);

        //build CX
        ChillerControllerExchange cx = null;
        if (currentColdWaterTemp <= this.minColdWaterTemp) {
            //TURN OFF Adsorption Chiller
            cx = new ChillerControllerExchange(
                    this.getUUID(),
                    exchange.getTime(),
                    true,
                    false,
                    0);
        } else if (currentColdWaterTemp >= this.maxColdWaterTemp) {
            //CHECK WHETER MIN AND MAX TEMPERATURE IS VALID
            if (currentHotWaterTemp <= this.maxHotWaterTemp
                    && currentHotWaterTemp >= this.minHotWaterTemp) {
                //TURN ON Adsorption Chiller
                cx = new ChillerControllerExchange(
                        this.getUUID(),
                        exchange.getTime(),
                        false,
                        true,
                        15 * 60);
            }
        }

        if (cx != null) {
            this.updateOcDataSubscriber(cx);
        }
    }
}
