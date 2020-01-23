package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.dof.DofWAMPDispatcher;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.dof.DofStateExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author Sebastian Kramer
 */
public class DofWAMPComDriver extends CALComDriver implements Runnable {

    private final Lock dispatcherWriteLock = new ReentrantReadWriteLock().writeLock();
    private final Map<Integer, UUID> mieleUUIDMap = new HashMap<>();
    private final HashMap<UUID, Integer> lastSentValues = new HashMap<>();
    private DofWAMPDispatcher dofDispatcher;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public DofWAMPComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        //ih: -1609550966
        this.mieleUUIDMap.put(-1609550966, UUID.fromString("a010338a-4d49-4d49-0000-5e09c0a80114"));
        //coffee: -1609551312
        this.mieleUUIDMap.put(-1609551312, UUID.fromString("a0103230-4d49-4d49-0000-5e0ac0a80114"));
        //dw: -1609555510
        this.mieleUUIDMap.put(-1609555510, UUID.fromString("a01021ca-4d49-4d49-0000-5601c0a80114"));
        //ov: -1609555623
        this.mieleUUIDMap.put(-1609555623, UUID.fromString("a0102159-4d49-4d49-0000-5e06c0a80114"));
        //td: -1609555628
        this.mieleUUIDMap.put(-1609555628, UUID.fromString("a0102154-4d49-4d49-0000-5602c0a80114"));
        //wm: -1609555631
        this.mieleUUIDMap.put(-1609555631, UUID.fromString("a0102151-4d49-4d49-0000-5604c0a80114"));

        for (UUID device : this.mieleUUIDMap.values()) {
            this.lastSentValues.put(device, Integer.MIN_VALUE);
        }
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.dofDispatcher = new DofWAMPDispatcher(
                this.getGlobalLogger());

        new Thread(this, "push proxy of Miele dof bus driver to WAMP").start();
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        // NOTHING
    }


    @Override
    public void run() {
        while (true) {
            this.dispatcherWriteLock.lock();

            try { // wait for new data
                this.dofDispatcher.wait();
            } catch (InterruptedException e) {
                this.getGlobalLogger().logError("should not happen", e);

                break;
            } finally {
                this.dispatcherWriteLock.unlock();
            }

            long timestamp = this.getTimeDriver().getUnixTime();

            if (this.dofDispatcher.getDeviceMap().isEmpty()) { // an error has occurred
                this.getGlobalLogger().logError("Device Data of Dof-WAMP-Dispatcher is empty");
            }

            for (Entry<Integer, Integer> dof : this.dofDispatcher.getDeviceMap().entrySet()) {
                // build UUID
                UUID mieleUUID = this.mieleUUIDMap.get(dof.getKey());
                if (!this.lastSentValues.get(mieleUUID).equals(dof.getValue())) {
                    DofStateExchange dse = new DofStateExchange(mieleUUID, timestamp);
                    dse.setDevice1stDegreeOfFreedom(dof.getValue());
                    dse.setDevice2ndDegreeOfFreedom(dof.getValue());
                    this.getComRegistry().publish(DofStateExchange.class, dse);

                    this.lastSentValues.put(mieleUUID, dof.getValue());
                }
            }
            this.dispatcherWriteLock.unlock();
        }
    }
}
