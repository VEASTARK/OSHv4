package osh.comdriver.wamp;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.SparseLoadProfile;
import osh.hal.exchange.GUIEpsComExchange;
import osh.hal.exchange.GUIPlsComExchange;
import osh.hal.exchange.GUIScheduleComExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class SchedulesWAMPComDriver extends CALComDriver implements Runnable {

    SchedulesWAMPDispatcher schedulesWampDispatcher;
    private Map<AncillaryCommodity, PriceSignal> priceSignals;
    private Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
    private List<Schedule> schedules;
    private int stepSize;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public SchedulesWAMPComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.schedulesWampDispatcher = new SchedulesWAMPDispatcher(this.getGlobalLogger());

        new Thread(this, "push proxy of optimisation results to WAMP").start();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this.schedulesWampDispatcher) {
                try { // wait for new data
                    this.schedulesWampDispatcher.wait();
                } catch (InterruptedException e) {
                    this.getGlobalLogger().logError("should not happen", e);
                    break;
                }
            }
        }
    }

    private void cleanUpSchedulesAndSend() {
        Map<String, Map<Commodity, Map<Long, Integer>>> scheduleMap = new HashMap<>();

//		long now = getTimer().getUnixTime();

        for (Schedule s : this.schedules) {
            Map<Commodity, Map<Long, Integer>> simpleProfile = ((SparseLoadProfile) s.getProfile())
                    .getCompressedProfileByTimeSlot(this.stepSize)
                    .convertToSimpleMap();

            if (!simpleProfile.isEmpty() && simpleProfile.containsKey(Commodity.ACTIVEPOWER)) {
                scheduleMap.put(s.getScheduleName(), simpleProfile);
            }
        }

        SchedulesWAMPExchangeObject sweo = new SchedulesWAMPExchangeObject(this.priceSignals, this.powerLimitSignals, scheduleMap);
        this.schedulesWampDispatcher.sendSchedules(sweo);
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        if (exchangeObject instanceof GUIEpsComExchange) {
            this.priceSignals = ((GUIEpsComExchange) exchangeObject).getPriceSignals();
        } else if (exchangeObject instanceof GUIPlsComExchange) {
            this.powerLimitSignals = ((GUIPlsComExchange) exchangeObject).getPowerLimitSignals();
        } else if (exchangeObject instanceof GUIScheduleComExchange) {
            this.schedules = ((GUIScheduleComExchange) exchangeObject).getSchedules();
            this.stepSize = ((GUIScheduleComExchange) exchangeObject).getStepSize();
            this.cleanUpSchedulesAndSend();
        }
    }
}
