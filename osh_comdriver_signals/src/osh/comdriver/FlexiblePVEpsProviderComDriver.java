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
import osh.utils.time.TimeConversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Jan Mueller
 */
public class FlexiblePVEpsProviderComDriver extends CALComDriver {

    private final EnumMap<AncillaryCommodity, PriceSignal> currentPriceSignal = new EnumMap<>(AncillaryCommodity.class);
    /**
     * Minimum time the signal is available in advance (24h)
     */
    private final int signalAvailableFor;
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

    //private List<Double> priceSignalYear;
    /**
     * Signal is constant for 15 minutes
     */
    private int resolutionOfPriceSignal;
    private double activePowerPrice;
    private double reactivePowerPrice;
    private double naturalGasPowerPrice;
    private double activePowerFeedInCHP;
    private double activePowerAutoConsumptionPV;
    private double activePowerAutoConsumptionCHP;
    /**
     * Path of price signal CSV file
     */
    private String filePathActivePowerFeedInPVPriceSignal;
    private List<Double> pVPriceSignal;


    public FlexiblePVEpsProviderComDriver(
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
            this.resolutionOfPriceSignal = Integer.parseInt(this.getComConfig().getParameter("resolutionOfPriceSignal"));
        } catch (Exception e) {
            this.resolutionOfPriceSignal = 3600; //15 minutes
            this.getGlobalLogger().logWarning("Can't get signalConstantPeriod, using the default value: " + this.resolutionOfPriceSignal);
        }

        try {
            this.activePowerPrice = Double.parseDouble(this.getComConfig().getParameter("activePowerPrice"));
        } catch (Exception e) {
            this.activePowerPrice = 28.0;
            this.getGlobalLogger().logWarning("Can't get activePowerPrice, using the default value: " + this.activePowerPrice);
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
            this.naturalGasPowerPrice = 7.0;
            this.getGlobalLogger().logWarning("Can't get naturalGasPowerPrice, using the default value: " + this.naturalGasPowerPrice);
        }

        try {
            this.activePowerFeedInCHP = Double.parseDouble(this.getComConfig().getParameter("activePowerFeedInCHP"));
        } catch (Exception e) {
            this.activePowerFeedInCHP = 5.0;
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

        try {
            this.filePathActivePowerFeedInPVPriceSignal = this.getComConfig().getParameter("filePathActivePowerFeedInPVPriceSignal");
        } catch (Exception e) {
            this.filePathActivePowerFeedInPVPriceSignal = "configfiles/externalSignal/priceDynamic.csv";
            this.getGlobalLogger().logWarning("Can't get filePathPriceSignal, using the default value: " + this.filePathActivePowerFeedInPVPriceSignal);
        }


        this.signalAvailableFor = this.signalPeriod - this.newSignalAfterThisPeriod;

    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        long now = this.getTimeDriver().getCurrentEpochSecond();

        this.pVPriceSignal = this.readCsvPriceSignal(this.filePathActivePowerFeedInPVPriceSignal);

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, this.activePowerPrice);
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
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, this.pVPriceSignal);
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
        if (this.newSignalAfterThisPeriod % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        long now = exchange.getEpochSecond();

        if ((now - this.lastSignalSent) >= this.newSignalAfterThisPeriod) {
            if (this.activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, this.activePowerPrice);
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
                PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, this.pVPriceSignal);
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


    private List<Double> readCsvPriceSignal(String filePath) {
        List<Double> priceSignalYear = new ArrayList<>();
        try {

            BufferedReader csvReader = new BufferedReader(new FileReader(new File(filePath)));
            String priceSignalLine;
            while ((priceSignalLine = csvReader.readLine()) != null) {
                String[] splitLine = priceSignalLine.split(";");
                priceSignalYear.add(Double.valueOf(splitLine[0]));
            }
            csvReader.close();
        } catch (Exception e) { // TODO: rethrow and handle
            e.printStackTrace();
        }
        return priceSignalYear;
    }


    private PriceSignal generatePriceSignal(AncillaryCommodity commodity, List<Double> priceSignalYear) {
        PriceSignal priceSignal;


        long now = this.getTimeDriver().getCurrentEpochSecond();
        if (now == this.getTimeDriver().getTimeAtStart().toEpochSecond()) {
            // initial price signal
            long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
            long timeTillEndOfDay = 86400 - timeSinceMidnight;

            priceSignal = PriceSignalGenerator.getFlexiblePriceSignal(
                    commodity,
                    TimeConversion.convertUnixTime2SecondsFromYearStart(now),
                    now,
                    now + timeTillEndOfDay + this.signalAvailableFor,
                    this.resolutionOfPriceSignal,
                    priceSignalYear);

        } else {
            // generate every 12 hours

            priceSignal = PriceSignalGenerator.getFlexiblePriceSignal(
                    commodity,
                    TimeConversion.convertUnixTime2SecondsFromYearStart(now),
                    now,
                    now + this.signalPeriod,
                    this.resolutionOfPriceSignal,
                    priceSignalYear);
        }

        return priceSignal;
    }

    private PriceSignal generatePriceSignal(AncillaryCommodity commodity, double price) {
        PriceSignal priceSignal;


        long now = this.getTimeDriver().getCurrentEpochSecond();
        if (now == this.getTimeDriver().getTimeAtStart().toEpochSecond()) {
            // initial price signal
            long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
            long timeTillEndOfDay = 86400 - timeSinceMidnight;

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    commodity,
                    now,
                    now + timeTillEndOfDay + this.signalAvailableFor,
                    this.resolutionOfPriceSignal,
                    price);

        } else {
            // generate every 12 hours

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    commodity,
                    now,
                    now + this.signalPeriod,
                    this.resolutionOfPriceSignal,
                    price);
        }

        return priceSignal;
    }

}
