package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.PriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.hal.exchange.EpsComExchange;
import osh.utils.time.TimeConversion;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Ingo Mauser
 */
public class WIKHourlyBasedEpsProviderComDriver extends CALComDriver {

    private final EnumMap<AncillaryCommodity, PriceSignal> currentPriceSignal = new EnumMap<>(AncillaryCommodity.class);
    /**
     * Minimum time the signal is available in advance (24h)
     */
    private final int signalAvailableFor;
    private final TreeMap<Long, Double> activePowerPrices;
    //the active ancillary commodities for which a price signal should be produced
    private final List<AncillaryCommodity> activeAncillaryCommodities;
    /**
     * Time after which a signal is send
     */
    private int newSignalAfterThisPeriod;
    /**
     * Timestamp of the last price signal sent to global controller
     */
    private long lastSignalSent;
    /**
     * Maximum time the signal is available in advance (36h)
     */
    private int signalPeriod;
    /**
     * Signal is constant for 15 minutes
     */
    private int signalConstantPeriod;
    private double reactivePowerPrice;
    private double naturalGasPowerPrice;
    private double activePowerFeedInPV;
    private double activePowerFeedInCHP;
    private double activePowerAutoConsumptionPV;
    private double activePowerAutoConsumptionCHP;


    public WIKHourlyBasedEpsProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        try {
            this.newSignalAfterThisPeriod = Integer.parseInt(this.getComConfig().getParameter("newSignalAfterThisPeriod"));
        } catch (Exception e) {
            this.newSignalAfterThisPeriod = 43200; //12 hours
            this.getGlobalLogger().logWarning("Can't get newSignalAfterThisPeriod, using the default value: " + this.newSignalAfterThisPeriod);
        }

        try {
            this.signalPeriod = Integer.parseInt(this.getComConfig().getParameter("signalPeriod"));
        } catch (Exception e) {
            this.signalPeriod = 129600; //36 hours
            this.getGlobalLogger().logWarning("Can't get signalPeriod, using the default value: " + this.signalPeriod);
        }

        try {
            this.signalConstantPeriod = Integer.parseInt(this.getComConfig().getParameter("signalConstantPeriod"));
        } catch (Exception e) {
            this.signalConstantPeriod = 900; //15 minutes
            this.getGlobalLogger().logWarning("Can't get signalConstantPeriod, using the default value: " + this.signalConstantPeriod);
        }

        try {
            this.reactivePowerPrice = Double.parseDouble(this.getComConfig().getParameter("reactivePowerPrice"));
        } catch (Exception e) {
            this.reactivePowerPrice = 0.0;
            this.getGlobalLogger().logWarning("Can't get reactivePowerPrice, using the default value: " + this.reactivePowerPrice);
        }

        try {
            this.naturalGasPowerPrice = Double.parseDouble(this.getComConfig().getParameter("naturalGasPowerPrice"));
        } catch (Exception e) {
            this.naturalGasPowerPrice = 6.0;
            this.getGlobalLogger().logWarning("Can't get naturalGasPowerPrice, using the default value: " + this.naturalGasPowerPrice);
        }

        try {
            this.activePowerFeedInPV = Double.parseDouble(this.getComConfig().getParameter("activePowerFeedInPV"));
        } catch (Exception e) {
            this.activePowerFeedInPV = 10.0;
            this.getGlobalLogger().logWarning("Can't get activePowerFeedInPV, using the default value: " + this.activePowerFeedInPV);
        }

        try {
            this.activePowerFeedInCHP = Double.parseDouble(this.getComConfig().getParameter("activePowerFeedInCHP"));
        } catch (Exception e) {
            this.activePowerFeedInCHP = 8.0;
            this.getGlobalLogger().logWarning("Can't get activePowerFeedInCHP, using the default value: " + this.activePowerFeedInCHP);
        }

        try {
            this.activePowerAutoConsumptionPV = Double.parseDouble(this.getComConfig().getParameter("activePowerAutoConsumptionPV"));
        } catch (Exception e) {
            this.activePowerAutoConsumptionPV = 0.0;
            this.getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionPV, using the default value: " + this.activePowerAutoConsumptionPV);
        }

        try {
            this.activePowerAutoConsumptionCHP = Double.parseDouble(this.getComConfig().getParameter("activePowerAutoConsumptionCHP"));
        } catch (Exception e) {
            this.activePowerAutoConsumptionCHP = 0.0;
            this.getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionCHP, using the default value: " + this.activePowerAutoConsumptionCHP);
        }

        String ancillaryCommoditiesAsArray;

        try {
            ancillaryCommoditiesAsArray = driverConfig.getParameter("ancillaryCommodities");
            if (ancillaryCommoditiesAsArray == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            ancillaryCommoditiesAsArray = "[activepowerexternal, reactivepowerexternal, naturalgaspowerexternal, pvactivepowerfeedin, chpactivepowerfeedin]";
            this.getGlobalLogger().logWarning("Can't get ancillaryCommoditiesAsArray, using the default value: " + ancillaryCommoditiesAsArray);
        }

        ancillaryCommoditiesAsArray = ancillaryCommoditiesAsArray.replaceAll("\\[|]|\\s", "");
        this.activeAncillaryCommodities = Stream.of(ancillaryCommoditiesAsArray.split(","))
                .map(AncillaryCommodity::fromString)
                .collect(Collectors.toList());

        String activePowerPrices;

        try {
            activePowerPrices = driverConfig.getParameter("activePowerPrices");
            if (activePowerPrices == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            activePowerPrices = "0=25.0";
            this.getGlobalLogger().logWarning("Can't get activePowerPrices, using the default value: " + activePowerPrices);
        }

        activePowerPrices = activePowerPrices.replaceAll("\\s+", "");
        this.activePowerPrices = new TreeMap<>();

        Arrays.asList(activePowerPrices.split(","))
                .forEach(p -> this.activePowerPrices.put(Long.parseLong(p.split("=")[0]),
                        Double.parseDouble(p.split("=")[1])));


        this.signalAvailableFor = this.signalPeriod - this.newSignalAfterThisPeriod;
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        long now = this.getTimeDriver().getUnixTime();

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
            PriceSignal newSignal = this.generateTreeMapBasedPriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, this.activePowerPrices);
            this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, this.reactivePowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, this.naturalGasPowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, this.activePowerFeedInPV);
            this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, this.activePowerFeedInCHP);
            this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, this.activePowerAutoConsumptionPV);
            this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, this.activePowerAutoConsumptionCHP);
            this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, newSignal);
        }

        // EPS
        EpsComExchange ex = new EpsComExchange(
                this.getUUID(),
                now,
                this.currentPriceSignal);
        this.notifyComManager(ex);

        this.lastSignalSent = now;

        // register
        this.getTimeDriver().registerComponent(this, 1);
    }

    @Override
    public void onNextTimePeriod() {

        long now = this.getTimeDriver().getUnixTime();

        if ((now - this.lastSignalSent) >= this.newSignalAfterThisPeriod) {
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
                PriceSignal newSignal = this.generateTreeMapBasedPriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, this.activePowerPrices);
                this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) {
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, this.reactivePowerPrice);
                this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, this.naturalGasPowerPrice);
                this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, this.activePowerFeedInPV);
                this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, this.activePowerFeedInCHP);
                this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, this.activePowerAutoConsumptionPV);
                this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, this.activePowerAutoConsumptionCHP);
                this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, newSignal);
            }

            this.lastSignalSent = now;

            // EPS
            EpsComExchange ex = new EpsComExchange(
                    this.getUUID(),
                    now,
                    this.currentPriceSignal);
            this.notifyComManager(ex);
        }

    }


    @Override
    public void updateDataFromComManager(ICALExchange hx) {
        //NOTHING
    }


    private PriceSignal generatePriceSignal(AncillaryCommodity commodity, double price) {
        PriceSignal priceSignal;

        long now = this.getTimeDriver().getUnixTime();
        if (now == this.getTimeDriver().getUnixTimeAtStart()) {
            // initial price signal
            long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
            long timeTillEndOfDay = 86400 - timeSinceMidnight;

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    commodity,
                    now,
                    now + timeTillEndOfDay + this.signalAvailableFor,
                    this.signalConstantPeriod,
                    price);

        } else {
            // generate every 12 hours

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    commodity,
                    now,
                    now + this.signalPeriod,
                    this.signalConstantPeriod,
                    price);
        }

        return priceSignal;
    }


    private PriceSignal generateTreeMapBasedPriceSignal(AncillaryCommodity commodity, TreeMap<Long, Double> prices) {
        PriceSignal priceSignal;
        long now = this.getTimeDriver().getUnixTime();

        if (now == this.getTimeDriver().getUnixTimeAtStart()) {
            // initial price signal

            long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
            long timeTillEndOfDay = 86400 - timeSinceMidnight;

            priceSignal = PriceSignalGenerator.getPriceSignalOfTreeMap(
                    commodity,
                    now,
                    now + timeTillEndOfDay + this.signalAvailableFor,
                    this.signalConstantPeriod,
                    prices);
        } else {
            // generate every 12 hours

            priceSignal = PriceSignalGenerator.getPriceSignalOfTreeMap(
                    commodity,
                    now,
                    now + this.signalPeriod,
                    this.signalConstantPeriod,
                    prices);
        }

        return priceSignal;
    }


}
