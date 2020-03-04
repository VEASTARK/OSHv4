package osh.comdriver.simulation;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandom;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.PlsComExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class RandomPlsProviderComDriver extends CALComDriver {

    private final EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

    /**
     * Time after which a signal is send
     */
    private final Duration newSignalAfterThisPeriod = Duration.ofHours(12);
    /**
     * Maximum time the signal is available in advance (36h)
     */
    private final int signalPeriod = 36 * 60 * 60;
    private final long gaussianTimeMu = 10 * 60; //changes every 10m on avaerage
    private final long gaussianTimeSigma = 5 * 60; // 68-95-99.7% of all values will be within 1/2/3x mu +/- sigma
    private final AncillaryCommodity[] limitsToSet = {
            AncillaryCommodity.ACTIVEPOWEREXTERNAL,
            AncillaryCommodity.REACTIVEPOWEREXTERNAL,
    };
    private final int[][] limitsGaussianMu = {
            {3000, -3000},
            {2000, -2000},
    };
    private final int[] limitsGaussianSigma = {
            500,
            300
    };
    /**
     * Timestamp of the last price signal sent to global controller
     */
    private ZonedDateTime lastSignalSent;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public RandomPlsProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        OSHRandom rand = this.getRandomDistributor().getRandomGenerator(this.getUUID(), this.getClass());
        this.generateLimitSignal(rand);
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();
        this.lastSignalSent = now;

        PlsComExchange ex = new PlsComExchange(
                this.getUUID(),
                now,
                this.powerLimitSignals);
        this.notifyComManager(ex);

        // register
        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();
        OSHRandom rand = this.getRandomDistributor().getRandomGenerator(this.getUUID(), this.getClass());

        if (!now.isAfter(this.lastSignalSent.plus(this.newSignalAfterThisPeriod))) {
            this.generateLimitSignal(rand);

            this.lastSignalSent = now;

            PlsComExchange ex = new PlsComExchange(
                    this.getUUID(),
                    now,
                    this.powerLimitSignals);
            this.notifyComManager(ex);
        }
    }


    private void generateLimitSignal(OSHRandom randomGen) {

        long now = this.getTimeDriver().getCurrentEpochSecond();

        for (int i = 0; i < this.limitsToSet.length; i++) {
            AncillaryCommodity ac = this.limitsToSet[i];

            PowerLimitSignal pls = new PowerLimitSignal();
            long time = now - 1;
            int upperLimit = -1;
            int lowerLimit = 1;
            while (upperLimit < 1) {
                upperLimit = (int) Math.round((randomGen.getNextGaussian() * this.limitsGaussianSigma[i]) + this.limitsGaussianMu[i][0]);
            }
            while (lowerLimit > -1) {
                lowerLimit = (int) Math.round((randomGen.getNextGaussian() * this.limitsGaussianSigma[i]) + this.limitsGaussianMu[i][1]);
            }
            pls.setPowerLimit(time, upperLimit, lowerLimit);

            while (time < now + this.signalPeriod) {
                //new time
                long additional = -1;

                while (additional < 1) {
                    additional = Math.round((randomGen.getNextGaussian() * this.gaussianTimeSigma) + this.gaussianTimeMu);
                }
                time += additional;

                upperLimit = -1;
                lowerLimit = 1;
                while (upperLimit < 1) {
                    upperLimit = (int) Math.round((randomGen.getNextGaussian() * this.limitsGaussianSigma[i]) + this.limitsGaussianMu[i][0]);
                }
                while (lowerLimit > -1) {
                    lowerLimit = (int) Math.round((randomGen.getNextGaussian() * this.limitsGaussianSigma[i]) + this.limitsGaussianMu[i][1]);
                }

                pls.setPowerLimit(time, upperLimit, lowerLimit);
            }
            System.out.println(pls.toString());

            pls.setKnownPowerLimitInterval(now, now + this.signalPeriod);

            this.powerLimitSignals.put(ac, pls);
        }
    }


    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

}
