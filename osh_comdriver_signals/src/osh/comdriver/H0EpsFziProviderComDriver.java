package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.VirtualPriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.EpsComExchange;
import osh.utils.slp.IH0Profile;
import osh.utils.string.ParameterConstants;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class H0EpsFziProviderComDriver extends CALComDriver {

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals = new EnumMap<>(AncillaryCommodity.class);

    private IH0Profile h0Profile;
    private String h0ProfileFileName;
    private String h0ClassName;
    private int currentYear;

    /**
     * Time after which a signal is send
     */
    private Duration newSignalAfterThisPeriod;

    /**
     * Maximum time the signal is available in advance (36h)
     */
    private int signalPeriod;

    private ZonedDateTime lastTimeSignalSent;

    /* Minimum time the signal is available in advance (24h)
     * atLeast = signalPeriod - newSignalAfterThisPeriod */
//	private int signalAvailableFor;

    /**
     * Signal is constant for 15 minutes
     */
    private int signalConstantPeriod;

    private double activePowerExternalSupplyMin;
    private double activePowerExternalSupplyAvg;
    private double activePowerExternalSupplyMax;
    private double activePowerFeedInPV;
    private double activePowerFeedInCHP;
    private double activePowerAutoConsumptionPV;
    private double activePowerAutoConsumptionCHP;
    private double naturalGasPowerPrice;


    public H0EpsFziProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        try {
            this.h0ProfileFileName = this.getComConfig().getParameter(ParameterConstants.General_Devices.h0Filename);
            if (this.h0ProfileFileName == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            this.h0ProfileFileName = "configfiles/h0/H0Profile15MinWinterSummerIntermediate.csv";
            this.getGlobalLogger().logWarning("Can't get h0Filename, using the default value: " + this.h0ProfileFileName);
        }

        try {
            this.h0ClassName = this.getComConfig().getParameter(ParameterConstants.General_Devices.h0Classname);
            if (this.h0ClassName == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            this.h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
            this.getGlobalLogger().logWarning("Can't get h0ClassName, using the default value: " + this.h0ClassName);
        }

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
            this.activePowerExternalSupplyMin =
                    Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.activePriceSupplyMin));
        } catch (Exception e) {
            this.activePowerExternalSupplyMin = 5.0;
            this.getGlobalLogger().logWarning("Can't get activePowerExternalSupplyMin, using the default value: " + this.activePowerExternalSupplyMin);
        }

        try {
            this.activePowerExternalSupplyAvg =
                    Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.activePriceSupplyAvg));
        } catch (Exception e) {
            this.activePowerExternalSupplyAvg = 25.0;
            this.getGlobalLogger().logWarning("Can't get activePowerExternalSupplyAvg, using the default value: " + this.activePowerExternalSupplyAvg);
        }

        try {
            this.activePowerExternalSupplyMax =
                    Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.activePriceSupplyMax));
        } catch (Exception e) {
            this.activePowerExternalSupplyMax = 45.0;
            this.getGlobalLogger().logWarning("Can't get activePowerExternalSupplyMax, using the default value: " + this.activePowerExternalSupplyMax);
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

        try {
            this.naturalGasPowerPrice = Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.gasPrice));
        } catch (Exception e) {
            this.naturalGasPowerPrice = 7.5;
            this.getGlobalLogger().logWarning("Can't get naturalGasPowerPrice, using the default value: " + this.naturalGasPowerPrice);
        }

        try {
            this.activePowerFeedInPV = Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.pvFeedInPrice));
        } catch (Exception e) {
            this.activePowerFeedInPV = 10.0;
            this.getGlobalLogger().logWarning("Can't get activePowerFeedInPV, using the default value: " + this.activePowerFeedInPV);
        }

        try {
            this.activePowerFeedInCHP =
                    Double.parseDouble(this.getComConfig().getParameter(ParameterConstants.EPS.chpFeedInPrice));
        } catch (Exception e) {
            this.activePowerFeedInCHP = 5.0;
            this.getGlobalLogger().logWarning("Can't get activePowerFeedInCHP, using the default value: " + this.activePowerFeedInCHP);
        }
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.currentYear = this.getTimeDriver().getCurrentTime().getYear();

        try {
            Class h0Class = Class.forName(this.h0ClassName);

            this.h0Profile = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
                    .newInstance(this.currentYear,
                            this.h0ProfileFileName,
                            1000.0);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        this.priceSignals = this.generateNewPriceSignal();
        EpsComExchange ex = new EpsComExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime(),
                this.priceSignals);
        this.notifyComManager(ex);

        this.lastTimeSignalSent = this.getTimeDriver().getCurrentTime();

        //register update
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

        // generate new PriceSignal and send it
        if (!now.isBefore(this.lastTimeSignalSent.plus(this.newSignalAfterThisPeriod))) {

            int nowIsYear = now.getYear();
            if (nowIsYear != this.currentYear) {
                // new years eve...
                this.currentYear = nowIsYear;

                // renew H0 Profile

                try {
                    Class h0Class = Class.forName(this.h0ClassName);
                    this.h0Profile = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
                            .newInstance(this.currentYear,
                                    this.h0ProfileFileName,
                                    1000.0);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException e) {
                    e.printStackTrace();
                }
            }

            // EPS
            this.priceSignals = this.generateNewPriceSignal();
            EpsComExchange ex = new EpsComExchange(
                    this.getUUID(),
                    now,
                    this.priceSignals);
            this.notifyComManager(ex);

            this.lastTimeSignalSent = now;
        }
    }


    private EnumMap<AncillaryCommodity, PriceSignal> generateNewPriceSignal() {

        long epochSecond = this.getTimeDriver().getCurrentEpochSecond();
        long signalEnd = epochSecond + this.signalPeriod;

        PriceSignal newPriceSignalAutoConsPV = VirtualPriceSignalGenerator.getConstantPriceSignal(
                epochSecond,
                signalEnd,
                this.signalConstantPeriod,
                this.activePowerAutoConsumptionPV,
                AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);

        PriceSignal newPriceSignalAutoConsCHP = VirtualPriceSignalGenerator.getConstantPriceSignal(
                epochSecond,
                signalEnd,
                this.signalConstantPeriod,
                this.activePowerAutoConsumptionCHP,
                AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);

        PriceSignal newPriceSignalExternal = VirtualPriceSignalGenerator.getRandomH0BasedPriceSignal(
                epochSecond,
                signalEnd,
                this.signalConstantPeriod,
                this.activePowerExternalSupplyMin,
                this.activePowerExternalSupplyAvg,
                this.activePowerExternalSupplyMax,
                this.h0Profile,
                this.getOSH().getRandomGenerator(),
                false,
                0,
                0,
                AncillaryCommodity.ACTIVEPOWEREXTERNAL);

        PriceSignal newPriceSignalFeedInPV = VirtualPriceSignalGenerator.getConstantPriceSignal(
                epochSecond,
                signalEnd,
                this.signalConstantPeriod,
                this.activePowerFeedInPV,
                AncillaryCommodity.PVACTIVEPOWERFEEDIN);

        PriceSignal newPriceSignalFeedInCHP = VirtualPriceSignalGenerator.getConstantPriceSignal(
                epochSecond,
                signalEnd,
                this.signalConstantPeriod,
                this.activePowerFeedInCHP,
                AncillaryCommodity.CHPACTIVEPOWERFEEDIN);

        PriceSignal newPriceSignalNaturalGas = VirtualPriceSignalGenerator.getConstantPriceSignal(
                epochSecond,
                signalEnd,
                this.signalConstantPeriod,
                this.naturalGasPowerPrice,
                AncillaryCommodity.NATURALGASPOWEREXTERNAL);

        EnumMap<AncillaryCommodity, PriceSignal> newPriceSignal = new EnumMap<>(AncillaryCommodity.class);
        newPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newPriceSignalExternal);
        newPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newPriceSignalAutoConsPV);
        newPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newPriceSignalFeedInPV);
        newPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newPriceSignalFeedInCHP);
        newPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, newPriceSignalAutoConsCHP);
        newPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newPriceSignalNaturalGas);

        return newPriceSignal;
    }


    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

}
