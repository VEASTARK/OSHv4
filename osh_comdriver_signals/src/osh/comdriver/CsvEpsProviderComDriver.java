package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.PriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
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
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Florian Allerding
 */
public class CsvEpsProviderComDriver extends CALComDriver {

    private final List<Double> priceSignalYear;
    private Duration newSignalAfterThisPeriod;
    private int resolutionOfPriceSignal;
    private String filePathPriceSignal;
    private int signalPeriod;

    private double activePowerFeedInPV;
    private double activePowerFeedInCHP;
    private double naturalGasPowerPrice;
    private double activePowerAutoConsumptionPV;
    private double activePowerAutoConsumptionCHP;

    private final List<AncillaryCommodity> activeAncillaryCommodities;

    private ZonedDateTime lastTimeSignalSent;


    /**
     * CONSTRUCTOR
     *
     * @param osh          reference to the top level control element
     * @param deviceID     unique id of this driver
     * @param driverConfig parameter configuration of this driver
     */
    public CsvEpsProviderComDriver(IOSH osh,
                                   UUID deviceID, OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
        this.priceSignalYear = new ArrayList<>();

        try {
            this.newSignalAfterThisPeriod = Duration.ofSeconds(Integer.parseInt(this.getComConfig().getParameter(
                    "newSignalAfterThisPeriod")));
        } catch (Exception e) {
            this.newSignalAfterThisPeriod = Duration.ofHours(12); //12 hours
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
            this.resolutionOfPriceSignal = 3600; //1 hour
            this.getGlobalLogger().logWarning("Can't get resolutionOfPriceSignal, using the default value: " + this.resolutionOfPriceSignal);
        }

        try {
            this.naturalGasPowerPrice = Double.parseDouble(this.getComConfig().getParameter("naturalGasPowerPrice"));
        } catch (Exception e) {
            this.naturalGasPowerPrice = 7.0;
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
            this.activePowerFeedInCHP = 0.0;
            this.getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionCHP, using the default value: " + this.activePowerAutoConsumptionCHP);
        }

        try {
            this.activePowerAutoConsumptionCHP = Double.parseDouble(this.getComConfig().getParameter("activePowerAutoConsumptionCHP"));
        } catch (Exception e) {
            this.activePowerFeedInCHP = 0.0;
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
            this.filePathPriceSignal = this.getComConfig().getParameter("filePathPriceSignal");
            if (this.filePathPriceSignal == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            this.filePathPriceSignal = "configfiles/externalSignal/priceDynamic.csv";
            this.getGlobalLogger().logWarning("Can't get filePathPriceSignal, using the default value: " + this.filePathPriceSignal);
        }
    }

    private void readCsvPriceSignal() {

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(new File(this.filePathPriceSignal)));
            String priceSignalLine;
            while ((priceSignalLine = csvReader.readLine()) != null) {
                String[] splitLine = priceSignalLine.split(";");
                this.priceSignalYear.add(Double.valueOf(splitLine[0]));
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void onSystemIsUp() {

        this.readCsvPriceSignal();
        this.generateNewPriceSignal();

        if (this.newSignalAfterThisPeriod.toSeconds() % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }

        this.lastTimeSignalSent = this.getTimeDriver().getCurrentTime();
    }

    /**
     * Generate PriceSignal
     */
    private void generateNewPriceSignal() {
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();
        int relativeTimeFromYearStart = (int) TimeConversion.getSecondsSinceYearStart(now);
        long yearStart = TimeConversion.getStartOfYear(now).toEpochSecond();
        long signalStart = now.toEpochSecond();
        long signalEnd = signalStart + this.signalPeriod;

        EnumMap<AncillaryCommodity, PriceSignal> priceSignals = new EnumMap<>(AncillaryCommodity.class);

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
            int priceSignalFrom = relativeTimeFromYearStart / this.resolutionOfPriceSignal;
            int priceSignalTo = (relativeTimeFromYearStart + this.signalPeriod) / this.resolutionOfPriceSignal;
            PriceSignal priceSignal = new PriceSignal();
            for (int i = priceSignalFrom; i < priceSignalTo; i++) {
                if (this.priceSignalYear.size() <= i) {
                    priceSignal.setPrice(yearStart + i * this.resolutionOfPriceSignal, 0.0);
                } else {
                    priceSignal.setPrice(yearStart + i * this.resolutionOfPriceSignal, this.priceSignalYear.get(i));
                }
            }
            priceSignal.setKnownPriceInterval(signalStart, signalEnd);
            priceSignal.compress();
            priceSignals.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, priceSignal);
        }

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {
            // PV ActivePower FeedIn
            PriceSignal newPriceSignalFeedInPV = PriceSignalGenerator.getConstantPriceSignal(
                    signalStart,
                    signalEnd,
                    this.signalPeriod,
                    this.activePowerFeedInPV);
            newPriceSignalFeedInPV.setKnownPriceInterval(signalStart, signalEnd);
            newPriceSignalFeedInPV.compress();
            priceSignals.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newPriceSignalFeedInPV);
        }

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
            // CHP ActivePower FeedIn
            PriceSignal newPriceSignalFeedInCHP = PriceSignalGenerator.getConstantPriceSignal(
                    signalStart,
                    signalEnd,
                    this.signalPeriod,
                    this.activePowerFeedInCHP);
            newPriceSignalFeedInCHP.setKnownPriceInterval(signalStart, signalEnd);
            newPriceSignalFeedInCHP.compress();
            priceSignals.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newPriceSignalFeedInCHP);
        }

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
            // Natural Gas Power Price
            PriceSignal newPriceSignalNaturalGas = PriceSignalGenerator.getConstantPriceSignal(
                    signalStart,
                    signalEnd,
                    this.signalPeriod,
                    this.naturalGasPowerPrice);
            newPriceSignalNaturalGas.setKnownPriceInterval(signalStart, signalEnd);
            newPriceSignalNaturalGas.compress();
            priceSignals.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newPriceSignalNaturalGas);
        }

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
            // Natural Gas Power Price
            PriceSignal newPriceSignalPVAutoConsumption = PriceSignalGenerator.getConstantPriceSignal(
                    signalStart,
                    signalEnd,
                    this.signalPeriod,
                    this.activePowerAutoConsumptionPV);
            newPriceSignalPVAutoConsumption.setKnownPriceInterval(signalStart, signalEnd);
            newPriceSignalPVAutoConsumption.compress();
            priceSignals.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newPriceSignalPVAutoConsumption);
        }

        if (this.activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
            // Natural Gas Power Price
            PriceSignal newPriceSignalCHPAutoConsumption = PriceSignalGenerator.getConstantPriceSignal(
                    signalStart,
                    signalEnd,
                    this.signalPeriod,
                    this.activePowerAutoConsumptionCHP);
            newPriceSignalCHPAutoConsumption.setKnownPriceInterval(signalStart, signalEnd);
            newPriceSignalCHPAutoConsumption.compress();
            priceSignals.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, newPriceSignalCHPAutoConsumption);
        }

        //now sending priceSignal
        EpsComExchange ex = new EpsComExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime(),
                priceSignals);
        this.updateComDataSubscriber(ex);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        if (!exchange.getTime().isAfter(this.lastTimeSignalSent.plus(this.newSignalAfterThisPeriod))) {
            this.generateNewPriceSignal();

            this.lastTimeSignalSent = exchange.getTime();
        }
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

}
