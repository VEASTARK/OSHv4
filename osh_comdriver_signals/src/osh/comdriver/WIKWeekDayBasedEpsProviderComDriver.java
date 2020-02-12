package osh.comdriver;

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
import osh.utils.string.ParameterConstants;
import osh.utils.time.TimeConversion;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Ingo Mauser
 */
public class WIKWeekDayBasedEpsProviderComDriver extends CALComDriver {

    private final EnumMap<AncillaryCommodity, PriceSignal> currentPriceSignal = new EnumMap<>(AncillaryCommodity.class);
    /**
     * Minimum time the signal is available in advance (24h)
     */
    private final int signalAvailableFor;
    private final Double[] activePowerPrices;
    /**
     * Time after which a signal is send
     */
    private Duration newSignalAfterThisPeriod;
    /**
     * Timestamp of the last price signal sent to global controller
     */
    private ZonedDateTime lastSignalSent;
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

    //the active ancillary commodities for which a price signal should be produced
    private final List<AncillaryCommodity> activeAncillaryCommodities;


    public WIKWeekDayBasedEpsProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        try {
            this.newSignalAfterThisPeriod = Duration.ofSeconds(Integer.parseInt(this.getComConfig().getParameter(
                    ParameterConstants.Signal.newSignal)));
        } catch (Exception e) {
            this.newSignalAfterThisPeriod = Duration.ofHours(12);
            this.getGlobalLogger().logWarning("Can't get newSignalAfterThisPeriod, using the default value: " + this.newSignalAfterThisPeriod);
        }

        try {
            this.signalPeriod = Integer.parseInt(this.getComConfig().getParameter(ParameterConstants.Signal.signalPeriod));
        } catch (Exception e) {
            this.signalPeriod = 129600; //36 hours
            this.getGlobalLogger().logWarning("Can't get signalPeriod, using the default value: " + this.signalPeriod);
        }

        try {
            this.signalConstantPeriod = Integer.parseInt(this.getComConfig().getParameter(ParameterConstants.Signal.signalConstantPeriod));
        } catch (Exception e) {
            this.signalConstantPeriod = 900; //15 minutes
            this.getGlobalLogger().logWarning("Can't get signalConstantPeriod, using the default value: " + this.signalConstantPeriod);
        }

        try {
            this.reactivePowerPrice = Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.reactivePrice));
        } catch (Exception e) {
            this.reactivePowerPrice = 0.0;
            this.getGlobalLogger().logWarning("Can't get reactivePowerPrice, using the default value: " + this.reactivePowerPrice);
        }

        try {
            this.naturalGasPowerPrice =
                    Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.gasPrice));
        } catch (Exception e) {
            this.naturalGasPowerPrice = 6.0;
            this.getGlobalLogger().logWarning("Can't get naturalGasPowerPrice, using the default value: " + this.naturalGasPowerPrice);
        }

        try {
            this.activePowerFeedInPV = Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.pvFeedInPrice));
        } catch (Exception e) {
            this.activePowerFeedInPV = 10.0;
            this.getGlobalLogger().logWarning("Can't get activePowerFeedInPV, using the default value: " + this.activePowerFeedInPV);
        }

        try {
            this.activePowerFeedInCHP = Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.chpFeedInPrice));
        } catch (Exception e) {
            this.activePowerFeedInCHP = 8.0;
            this.getGlobalLogger().logWarning("Can't get activePowerFeedInCHP, using the default value: " + this.activePowerFeedInCHP);
        }

        try {
            this.activePowerAutoConsumptionPV =
                    Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.pvAutoConsumptionPrice));
        } catch (Exception e) {
            this.activePowerAutoConsumptionPV = 0.0;
            this.getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionPV, using the default value: " + this.activePowerAutoConsumptionPV);
        }

        try {
            this.activePowerAutoConsumptionCHP =
                    Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.chpAutoConsumptionPrice));
        } catch (Exception e) {
            this.activePowerAutoConsumptionCHP = 0.0;
            this.getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionCHP, using the default value: " + this.activePowerAutoConsumptionCHP);
        }

        String ancillaryCommoditiesAsArray;

        try {
            ancillaryCommoditiesAsArray = driverConfig.getParameter(ParameterConstants.EPS.ancillaryCommodities);
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
            activePowerPrices = driverConfig.getParameter(ParameterConstants.EPS.activePriceArray);
            if (activePowerPrices == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            activePowerPrices = "[25, 25, 25, 25, 25, 25, 25]";
            this.getGlobalLogger().logWarning("Can't get activePowerPrices, using the default value: " + activePowerPrices);
        }

        activePowerPrices = activePowerPrices.replaceAll("\\[|]|\\s", "");
        this.activePowerPrices = Arrays.stream(activePowerPrices.split(","))
                .map(Double::parseDouble)
                .toArray(Double[]::new);

        this.signalAvailableFor = (int) (this.signalPeriod - this.newSignalAfterThisPeriod.toSeconds());
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
            PriceSignal newSignal = this.generateWeekDayBasedPriceSignal(this.activePowerPrices);
            this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) {
            PriceSignal newSignal = this.generatePriceSignal(this.reactivePowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
            PriceSignal newSignal = this.generatePriceSignal(this.naturalGasPowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {
            PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInPV);
            this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
            PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInCHP);
            this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
            PriceSignal newSignal = this.generatePriceSignal(this.activePowerAutoConsumptionPV);
            this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newSignal);
        }
        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
            PriceSignal newSignal = this.generatePriceSignal(this.activePowerAutoConsumptionCHP);
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
        if (this.newSignalAfterThisPeriod.toSeconds() % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();

        if (!now.isBefore(this.lastSignalSent.plus(this.newSignalAfterThisPeriod))) {
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
                PriceSignal newSignal = this.generateWeekDayBasedPriceSignal(this.activePowerPrices);
                this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) {
                PriceSignal newSignal = this.generatePriceSignal(this.reactivePowerPrice);
                this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
                PriceSignal newSignal = this.generatePriceSignal(this.naturalGasPowerPrice);
                this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {
                PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInPV);
                this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
                PriceSignal newSignal = this.generatePriceSignal(this.activePowerFeedInCHP);
                this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
                PriceSignal newSignal = this.generatePriceSignal(this.activePowerAutoConsumptionPV);
                this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newSignal);
            }
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
                PriceSignal newSignal = this.generatePriceSignal(this.activePowerAutoConsumptionCHP);
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


    private PriceSignal generateWeekDayBasedPriceSignal(Double[] prices) {
        PriceSignal priceSignal;
        long now = this.getTimeDriver().getCurrentEpochSecond();

        if (now == this.getTimeDriver().getTimeAtStart().toEpochSecond()) {
            // initial price signal

            long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
            long timeTillEndOfDay = 86400 - timeSinceMidnight;

            priceSignal = PriceSignalGenerator.getPriceSignalWeekday(
                    now,
                    now + timeTillEndOfDay + this.signalAvailableFor,
                    this.signalConstantPeriod,
                    prices);
        } else {
            // generate every 12 hours

            priceSignal = PriceSignalGenerator.getPriceSignalWeekday(
                    now,
                    now + this.signalPeriod,
                    this.signalConstantPeriod,
                    prices);
        }

        return priceSignal;
    }


}
