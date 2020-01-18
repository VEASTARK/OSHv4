package osh.comdriver.simulation;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.hal.exchange.PlsComExchange;

import java.util.EnumMap;
import java.util.Random;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class RandomPlsProviderComDriver extends CALComDriver {

    private final EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

    /**
     * Time after which a signal is send
     */
    private final int newSignalAfterThisPeriod = 12 * 60 * 60;
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
    private long lastSignalSent;


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

        OSHRandomGenerator rand = new OSHRandomGenerator(new Random(this.getRandomGenerator().getNextLong()));
        this.generateLimitSignal(rand);
        long now = this.getTimer().getUnixTime();
        this.lastSignalSent = now;

        PlsComExchange ex = new PlsComExchange(
                this.getDeviceID(),
                now,
                this.powerLimitSignals);
        this.notifyComManager(ex);

        // register
        this.getTimer().registerComponent(this, 1);
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        long now = this.getTimer().getUnixTime();
        OSHRandomGenerator rand = new OSHRandomGenerator(new Random(this.getRandomGenerator().getNextLong()));

        if ((now - this.lastSignalSent) >= this.newSignalAfterThisPeriod) {
            this.generateLimitSignal(rand);

            this.lastSignalSent = now;

            PlsComExchange ex = new PlsComExchange(
                    this.getDeviceID(),
                    now,
                    this.powerLimitSignals);
            this.notifyComManager(ex);
        }
    }


    private void generateLimitSignal(OSHRandomGenerator randomGen) {

        long now = this.getTimer().getUnixTime();

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
            System.out.println(pls.getLimits());

            pls.setKnownPowerLimitInterval(now, now + this.signalPeriod);

            this.powerLimitSignals.put(ac, pls);
        }
    }


    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

}
