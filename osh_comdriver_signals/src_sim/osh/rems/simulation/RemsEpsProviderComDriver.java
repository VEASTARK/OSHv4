package osh.rems.simulation;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.PriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.EpsComExchange;
import osh.utils.time.TimeConversion;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * Based on McFlat for REMS
 *
 * @author Malte Schröder
 */
public class RemsEpsProviderComDriver extends CALComDriver {

    private final EnumMap<AncillaryCommodity, PriceSignal> currentPriceSignal = new EnumMap<>(AncillaryCommodity.class);


    /**
     * Time after which a signal is send
     */
    private final Duration newSignalAfterThisPeriod = Duration.ofHours(12);
    /**
     * Maximum time the signal is available in advance (36h)
     */
    private final int signalPeriod = 129600; // 36 hours
    /**
     * Minimum time the signal is available in advance (24h)
     */
    private final int signalAvailableFor = (int) (this.signalPeriod - this.newSignalAfterThisPeriod.toSeconds());
    /**
     * Signal is constant for 15 minutes
     */
    private final int signalConstantPeriod = 900; // change every 15 minutes
    private final double activePowerPrice = 25.0;
    private final double naturalGasPowerPrice = 6.0;
    private final double activePowerFeedInPV = 12.0;
    //	private double activePowerFeedInPV = 10.0;
    private final double activePowerFeedInCHP = 9.0;
    /**
     * Timestamp of the last price signal sent to global controller
     */
    private ZonedDateTime lastSignalSent;
    private double reactivePowerPrice;
    //	private double activePowerFeedInCHP = 5.0;
    private boolean newSignalReceived;
    private EnumMap<AncillaryCommodity, PriceSignal> newSignals;


    public RemsEpsProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

    }

    //PS needs to be relative from now
    public void setNewSignal(PriceSignal signal) {
        this.newSignals = new EnumMap<>(AncillaryCommodity.class);
        long now = this.getTimeDriver().getCurrentEpochSecond();
        PriceSignal ps = new PriceSignal(signal.getPrices(), now);
        ps.setKnownPriceInterval(now, now + signal.getPriceUnknownAtAndAfter());
        this.newSignals.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL,ps);
        this.newSignalReceived = true;
    }

    public void setNewSignals(EnumMap<AncillaryCommodity, PriceSignal> signals) {
        this.newSignals = new EnumMap<>(AncillaryCommodity.class);
        long now = this.getTimeDriver().getCurrentEpochSecond();

        for (Entry<AncillaryCommodity, PriceSignal> signal : signals.entrySet()) {
            PriceSignal ps = new PriceSignal(signal.getValue().getPrices(), now);
            ps.setKnownPriceInterval(now, now + signal.getValue().getPriceUnknownAtAndAfter());
            this.newSignals.put(signal.getKey(), ps);
        }

        this.newSignalReceived = true;
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        {
            PriceSignal newSignal = this.generatePriceSignal(this.activePowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
        }
        {
            PriceSignal newSignal = this.generatePriceSignal(this.reactivePowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
        }
        {
            PriceSignal newSignal = this.generatePriceSignal(this.naturalGasPowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
        }
        {
            PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInPV);
            this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
        }
        {
            PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInCHP);
            this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
        }

        // EPS
        EpsComExchange ex = new EpsComExchange(
                this.getUUID(),
                now,
                this.currentPriceSignal);
        this.notifyComManager(ex);

        this.lastSignalSent = now;

        // register
        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();

        if (this.newSignalReceived) {
            ArrayList<AncillaryCommodity> allRelevantCommodities = new ArrayList<>();
            allRelevantCommodities.add(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
            allRelevantCommodities.add(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);
            allRelevantCommodities.add(AncillaryCommodity.NATURALGASPOWEREXTERNAL);
            allRelevantCommodities.add(AncillaryCommodity.PVACTIVEPOWERFEEDIN);
            allRelevantCommodities.add(AncillaryCommodity.REACTIVEPOWEREXTERNAL);


            for (Entry<AncillaryCommodity, PriceSignal> signal : this.newSignals.entrySet()) {
                this.currentPriceSignal.put(signal.getKey(), signal.getValue());
                allRelevantCommodities.remove(signal.getKey());
            }

            for (AncillaryCommodity vc : allRelevantCommodities) {

                //TODO: Fix
                if (vc == AncillaryCommodity.ACTIVEPOWEREXTERNAL) {
                    PriceSignal newSignal = this.generatePriceSignal(this.activePowerPrice);
                    this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
                } else if (vc == AncillaryCommodity.REACTIVEPOWEREXTERNAL) {
                    PriceSignal newSignal = this.generatePriceSignal(this.reactivePowerPrice);
                    this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
                } else if (vc == AncillaryCommodity.NATURALGASPOWEREXTERNAL) {
                    PriceSignal newSignal = this.generatePriceSignal(this.naturalGasPowerPrice);
                    this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
                } else if (vc == AncillaryCommodity.PVACTIVEPOWERFEEDIN) {
                    PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInPV);
                    this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
                } else if (vc == AncillaryCommodity.CHPACTIVEPOWERFEEDIN) {
                    PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInCHP);
                    this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
                }
            }

            // EPS
            EpsComExchange ex = new EpsComExchange(
                    this.getUUID(),
                    now,
                    this.currentPriceSignal,
                    true);
            this.notifyComManager(ex);
//			String uuid = this.getDeviceID().toString();

            this.lastSignalSent = now;
            //System.out.println("SENT NEW PRICE SIGNAL TO OSH: " + newSignals.get(0).getPrices() + " FOR: " + uuid.substring(uuid.length() - 3));
            //System.out.println("SENT NEW PRICE SIGNAL TO OSH: " + newSignals.get(0).getPrices() + newSignals.get(1).getPrices()+newSignals.get(2).getPrices());
        } else if (!now.isAfter(this.lastSignalSent.plus(this.newSignalAfterThisPeriod))) {

            //DIRTY FIX, pls make better ...
            ArrayList<AncillaryCommodity> allRelevantCommodities = new ArrayList<>();
            allRelevantCommodities.add(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
            allRelevantCommodities.add(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);
            allRelevantCommodities.add(AncillaryCommodity.NATURALGASPOWEREXTERNAL);
            allRelevantCommodities.add(AncillaryCommodity.PVACTIVEPOWERFEEDIN);
            allRelevantCommodities.add(AncillaryCommodity.REACTIVEPOWEREXTERNAL);

            for (AncillaryCommodity vc : allRelevantCommodities) {

                //TODO: Fix
                if (vc == AncillaryCommodity.ACTIVEPOWEREXTERNAL) {
                    PriceSignal newSignal = this.generatePriceSignal(this.activePowerPrice);
                    this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
                } else if (vc == AncillaryCommodity.REACTIVEPOWEREXTERNAL) {
                    PriceSignal newSignal = this.generatePriceSignal(this.reactivePowerPrice);
                    this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
                } else if (vc == AncillaryCommodity.NATURALGASPOWEREXTERNAL) {
                    PriceSignal newSignal = this.generatePriceSignal(this.naturalGasPowerPrice);
                    this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
                } else if (vc == AncillaryCommodity.PVACTIVEPOWERFEEDIN) {
                    PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInPV);
                    this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
                } else if (vc == AncillaryCommodity.CHPACTIVEPOWERFEEDIN) {
                    PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInCHP);
                    this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
                }
            }

            // EPS
            EpsComExchange ex = new EpsComExchange(
                    this.getUUID(),
                    now,
                    this.currentPriceSignal,
                    true);
            this.notifyComManager(ex);

            this.lastSignalSent = now;
        }

        this.newSignalReceived = false;
    }


    @Override
    public void updateDataFromComManager(ICALExchange hx) {
        //NOTHING
    }


    private PriceSignal generatePriceSignal(double price) {
        PriceSignal priceSignal;

        long now = this.getTimeDriver().getCurrentEpochSecond();
        if (now == this.getTimeDriver().getTimeAtStart().toEpochSecond()) {
            // initial price signal
            long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
            long timeTillEndOfDay = 86400 - timeSinceMidnight;

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    now,
                    now + timeTillEndOfDay + this.signalAvailableFor,
                    this.signalConstantPeriod,
                    price);

        } else {
            // generate every 12 hours

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    now,
                    now + this.signalPeriod,
                    this.signalConstantPeriod,
                    price);
        }

        return priceSignal;
    }


}
