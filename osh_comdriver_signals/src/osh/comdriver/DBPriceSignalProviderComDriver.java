package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.db.PriceSignalThread;
import osh.comdriver.signals.PriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.details.utility.CurrentPriceSignalLogDetails;
import osh.hal.exchange.EpsComExchange;
import osh.utils.time.TimeConversion;

import java.util.EnumMap;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class DBPriceSignalProviderComDriver extends CALComDriver {

    private final EnumMap<AncillaryCommodity, PriceSignal> currentPriceSignal = new EnumMap<>(AncillaryCommodity.class);
    private final double reactivePowerPrice = 1.0;
    private final double pvFeedInPrice = 12.0;
    private final double chpFeedInPrice = 8.0;
    private final double naturalGasPrice = 9.0;
    /**
     * Maximum time the signal is available in advance (36h)
     */
    private final int signalPeriod = 36 * 3600;
    /**
     * Minimum time the signal is available in advance (24h)
     */
    private final int signalAvailableFor = 24 * 3600;
    /**
     * Signal is constant for 15 minutes
     */
    private final int signalConstantPeriod = 15 * 60;
    private final String spsDBHost = "db";
    private final String spsDBPort = "3306";
    private final String spsDBName = "database";
    private final String spsDBLoginName = "user";
    private final String spsDBLoginPwd = "pw";
    private PriceSignalThread priceSignalThread;


    public DBPriceSignalProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

//		this.spsDBHost = driverConfig.getParameter("spsdbhost");
//		this.spsDBPort = driverConfig.getParameter("spsdbport");
//		this.spsDBName = driverConfig.getParameter("spsdbname");
//		this.spsDBLoginName = driverConfig.getParameter("spsdbloginname");
//		this.spsDBLoginPwd = driverConfig.getParameter("spsdbloginpwd");
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();
        //processPriceSignal();
        this.priceSignalThread = new PriceSignalThread(this.getGlobalLogger(), this);
        try {
            this.priceSignalThread.setUpSQLConnection(
                    this.spsDBHost,
                    this.spsDBPort,
                    this.spsDBName,
                    this.spsDBLoginName,
                    this.spsDBLoginPwd);
        } catch (ClassNotFoundException e) {
            this.getGlobalLogger().logError("unable to set up SQL connection for price signal provider", e);
            return;
        }
        this.priceSignalThread.start();
    }

    /**
     * communicate the price to the global observer
     *
     * @param pricesignal
     * @param powerLimit
     */
    public void processPriceSignal(PriceSignal pricesignal, PowerLimitSignal powerLimit) {

        long now = this.getTimeDriver().getCurrentEpochSecond();

//		HashMap<VirtualCommodity,PriceSignal> map = new HashMap<>();
//		map.put(VirtualCommodity.ACTIVEPOWEREXTERNAL, pricesignal);
//		EpsComExchange epsComEx = new EpsComExchange(getDeviceID(), now, map);
//		this.notifyComManager(epsComEx);

        // save as current state
        CurrentPriceSignalLogDetails priceSignalDetails = new CurrentPriceSignalLogDetails(this.getUUID(), now);
        priceSignalDetails.setCommodity(pricesignal.getCommodity());
        priceSignalDetails.getPricePerUnit(pricesignal.getPrice(now));

        this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, pricesignal);

        {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, this.reactivePowerPrice);
            this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
        }
        {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, this.naturalGasPrice);
            this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
        }
        {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, this.pvFeedInPrice);
            this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
        }
        {
            PriceSignal newSignal = this.generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, this.chpFeedInPrice);
            this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
        }

        // EPS
        EpsComExchange ex = new EpsComExchange(
                this.getUUID(),
                now,
                this.currentPriceSignal);
        this.notifyComManager(ex);

    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

    private PriceSignal generatePriceSignal(AncillaryCommodity commodity, double price) {
        PriceSignal priceSignal;

        long now = this.getTimeDriver().getCurrentEpochSecond();

        if (now == this.getTimeDriver().getTimeAtStart().toEpochSecond()) {
            // initial price signal
            //			long diff = now % 3600;
//			if (diff < 60) {
//				now = now - diff - 3600;
//			}
//			else {
//				now = now - diff;
//			}

            long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
            long timeTillEndOfDay = 86400 - timeSinceMidnight;

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    commodity,
                    now - 100,
                    now + timeTillEndOfDay + this.signalAvailableFor,
                    this.signalConstantPeriod,
                    price);

        } else {
            // generate every 12 hours

            priceSignal = PriceSignalGenerator.getConstantPriceSignal(
                    commodity,
                    now - 100,
                    now + this.signalPeriod,
                    this.signalConstantPeriod,
                    price);
        }

        return priceSignal;
    }
}
