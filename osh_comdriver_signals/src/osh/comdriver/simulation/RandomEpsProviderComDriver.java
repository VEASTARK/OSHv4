package osh.comdriver.simulation;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.EpsComExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Random;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class RandomEpsProviderComDriver extends CALComDriver {


    private final EnumMap<AncillaryCommodity, PriceSignal> currentPriceSignal = new EnumMap<>(AncillaryCommodity.class);

    /**
     * Time after which a signal is send
     */
    private final Duration newSignalAfterThisPeriod = Duration.ofHours(12);
    /**
     * Maximum time the signal is available in advance (36h)
     */
    private final int signalPeriod = 36 * 60 * 60;
    private final long gaussianTimeMu = 10 * 60; //changes every 10m on avaerage
//	/** Minimum time the signal is available in advance (24h) */
//	private int signalAvailableFor = 24 * 60 * 60;
    private final long gaussianTimeSigma = 5 * 60; // 68-95-99.7% of all values will be within 1/2/3x mu +/- sigma
    private final AncillaryCommodity[] pricesToSet = {
            AncillaryCommodity.ACTIVEPOWEREXTERNAL,
            AncillaryCommodity.REACTIVEPOWEREXTERNAL,
            AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,
            AncillaryCommodity.CHPACTIVEPOWERFEEDIN,
            AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
            AncillaryCommodity.PVACTIVEPOWERFEEDIN,
            AncillaryCommodity.NATURALGASPOWEREXTERNAL,
    };
    private final double[] pricesGaussianMu = {
            30.0,
            10.0,
            10.0,
            8.0,
            5.0,
            10.0,
            6.0
    };
    private final double[] pricesGaussianSigma = {
            10.0,
            2.0,
            2.5,
            1.5,
            1.0,
            2.0,
            1.0
    };
    /**
     * Timestamp of the last price signal sent to global controller
     */
    private ZonedDateTime lastSignalSent;


    public RandomEpsProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        OSHRandomGenerator rand = new OSHRandomGenerator(new Random(this.getRandomGenerator().getNextLong()));
        this.generatePriceSignal(rand);
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();
        this.lastSignalSent = now;

        // EPS
        EpsComExchange ex = new EpsComExchange(
                this.getUUID(),
                now,
                this.currentPriceSignal);
        this.notifyComManager(ex);

        // register
        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();
        OSHRandomGenerator rand = new OSHRandomGenerator(
                new Random(this.getOSH().getRandomGenerator().getNextLong()));

        if (!now.isAfter(this.lastSignalSent.plus(this.newSignalAfterThisPeriod))) {
            this.generatePriceSignal(rand);

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


    private void generatePriceSignal(OSHRandomGenerator randomGen) {

        long now = this.getTimeDriver().getCurrentEpochSecond();

        for (int i = 0; i < this.pricesToSet.length; i++) {
            AncillaryCommodity ac = this.pricesToSet[i];

            PriceSignal ps = new PriceSignal(ac);
            long time = now - 1;
            double price = -1;
            while (price < 1) {
                price = Math.round((randomGen.getNextGaussian() * this.pricesGaussianSigma[i]) + this.pricesGaussianMu[i]);
            }
            ps.setPrice(time, price);

            while (time < now + this.signalPeriod) {
                //new time
                long additional = -1;

                while (additional < 1) {
                    additional = Math.round((randomGen.getNextGaussian() * this.gaussianTimeSigma) + this.gaussianTimeMu);
                }
                time += additional;

                price = -1;
                while (price < 1) {
                    price = Math.round((randomGen.getNextGaussian() * this.pricesGaussianSigma[i]) + this.pricesGaussianMu[i]);
                }
                ps.setPrice(time, price);
            }
            System.out.println(ps.getPrices());

            ps.setKnownPriceInterval(now, now + this.signalPeriod);

            this.currentPriceSignal.put(ac, ps);
        }
    }


}
