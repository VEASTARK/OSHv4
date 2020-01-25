package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.PowerLimitSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.PlsComExchange;

import java.util.EnumMap;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class FlatPlsProviderComDriver extends CALComDriver {

    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;

    /**
     * Time after which a signal is send
     */
    private int newSignalAfterThisPeriod;

    /**
     * Maximum time the signal is available in advance (36h)
     */
    private int signalPeriod;

    private long lastTimeSignalSent;

    private int activeLowerLimit;
    private int activeUpperLimit;

    private int reactiveLowerLimit;
    private int reactiveUpperLimit;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public FlatPlsProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
        this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

        try {
            this.newSignalAfterThisPeriod = Integer.parseInt(this.getComConfig().getParameter("newSignalAfterThisPeriod"));
        } catch (Exception e) {
            this.newSignalAfterThisPeriod = 43200; //12h
            this.getGlobalLogger().logWarning("Can't get newSignalAfterThisPeriod, using the default value: " + this.newSignalAfterThisPeriod);
        }

        try {
            this.signalPeriod = Integer.parseInt(this.getComConfig().getParameter("signalPeriod"));
        } catch (Exception e) {
            this.signalPeriod = 129600; //36h
            this.getGlobalLogger().logWarning("Can't get signalPeriod, using the default value: " + this.signalPeriod);
        }

        try {
            this.activeLowerLimit = Integer.parseInt(this.getComConfig().getParameter("activeLowerLimit"));
        } catch (Exception e) {
            this.activeLowerLimit = -3000; //kW
            this.getGlobalLogger().logWarning("Can't get activeLowerLimit, using the default value: " + this.activeLowerLimit);
        }

        try {
            this.activeUpperLimit = Integer.parseInt(this.getComConfig().getParameter("activeUpperLimit"));
        } catch (Exception e) {
            this.activeUpperLimit = 10000; //kW
            this.getGlobalLogger().logWarning("Can't get activeUpperLimit, using the default value: " + this.activeUpperLimit);
        }

        try {
            this.reactiveLowerLimit = Integer.parseInt(this.getComConfig().getParameter("reactiveLowerLimit"));
        } catch (Exception e) {
            this.reactiveLowerLimit = -3000; //kW
            this.getGlobalLogger().logWarning("Can't get reactiveLowerLimit, using the default value: " + this.reactiveLowerLimit);
        }

        try {
            this.reactiveUpperLimit = Integer.parseInt(this.getComConfig().getParameter("reactiveUpperLimit"));
        } catch (Exception e) {
            this.reactiveUpperLimit = 10000; //kW
            this.getGlobalLogger().logWarning("Can't get reactiveUpperLimit, using the default value: " + this.reactiveUpperLimit);
        }
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        long now = this.getTimeDriver().getCurrentEpochSecond();
        this.powerLimitSignals = this.generateNewPowerLimitSignal(now);
        PlsComExchange ex = new PlsComExchange(
                this.getUUID(),
                now,
                this.powerLimitSignals);
        this.notifyComManager(ex);

        this.lastTimeSignalSent = now;

        // register
        if (this.newSignalAfterThisPeriod % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }
    }


    //TODO: better signal
    private EnumMap<AncillaryCommodity, PowerLimitSignal> generateNewPowerLimitSignal(long now) {
        EnumMap<AncillaryCommodity, PowerLimitSignal> newPls = new EnumMap<>(AncillaryCommodity.class);

        newPls.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, PowerLimitSignalGenerator.generateFlatPowerLimitSignal(now,
                now + this.signalPeriod, this.activeUpperLimit, this.activeLowerLimit));

        newPls.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, PowerLimitSignalGenerator.generateFlatPowerLimitSignal(now,
                now + this.signalPeriod, this.reactiveUpperLimit, this.reactiveLowerLimit));

        return newPls;
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        long now = exchange.getEpochSecond();
        // generate new PriceSignal and send it
        if ((now - this.lastTimeSignalSent) >= this.newSignalAfterThisPeriod) {
            // PLS
            this.powerLimitSignals = this.generateNewPowerLimitSignal(now);
            PlsComExchange ex = new PlsComExchange(
                    this.getUUID(),
                    now,
                    this.powerLimitSignals);
            this.notifyComManager(ex);

            this.lastTimeSignalSent = now;
        }
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

}
