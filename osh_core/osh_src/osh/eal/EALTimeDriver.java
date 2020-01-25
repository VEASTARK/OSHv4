package osh.eal;

import osh.core.logging.IGlobalLogger;
import osh.eal.time.TimeEventProvider;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.registry.TimeRegistry;
import osh.utils.time.TimeConversion;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides a manager for time in the OSH, capable of running as a clock-driver in simulation or representing the
 * real time-clock if not.
 *
 * @author Sebastian Kramer
 */
public class EALTimeDriver implements Runnable {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ZonedDateTime currentTime;
    private long currentEpochSecond;
    private EnumSet<TimeSubscribeEnum> currentTimeEvents;
    private final ZoneId hostTimeZone;

    private final ZonedDateTime timeAtStart;

    private final IGlobalLogger logger;
    private final TimeRegistry timeRegistry;

    /**
     * Creates this time-manager with a given starting time (and implicit time-zone through this given time), given
     * information if this run is in a simulation if real environment, a reference to the global logger and the
     * registry to publish the time in.
     *
     * @param timeAtStart the time at the start, the time-zone to use will be inferred from this
     * @param isSimulation flag if this is run in a simulation
     * @param logger the global logger
     * @param timeRegistry the registry to publish in
     */
    public EALTimeDriver(ZonedDateTime timeAtStart, boolean isSimulation, IGlobalLogger logger,
                         TimeRegistry timeRegistry) {
        this.hostTimeZone = timeAtStart.getZone();
        this.logger = logger;
        this.timeRegistry = timeRegistry;

        TimeConversion.setZone(this.hostTimeZone);

        if (!isSimulation) {
            this.timeAtStart = ZonedDateTime.now(this.hostTimeZone).truncatedTo(ChronoUnit.SECONDS);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
        } else {
            this.timeAtStart = timeAtStart;
        }

        this.currentTimeEvents = TimeEventProvider.getTimeEvents(timeAtStart);
        this.currentTime = this.timeAtStart;
        this.currentEpochSecond = this.currentTime.toEpochSecond();
    }

    /**
     * Updates the time this time-manager uses to the desired seconds since the start of the simulation. After the
     * internal update will publish the new time and associated time events to all subscribers.
     *
     * @param simulationSeconds the amount of seconds this simulation has run
     */
    public void updateTimer(long simulationSeconds) {
        //we don't update subscribers at the start of the simulation, only at the first second
        if (simulationSeconds == 0) return;
        this.currentTime = this.timeAtStart.plusSeconds(simulationSeconds);
        this.currentEpochSecond = this.currentTime.toEpochSecond();

        this.currentTimeEvents = TimeEventProvider.getTimeEvents(this.currentTime);

        if (this.currentTimeEvents.contains(TimeSubscribeEnum.HOUR)) {
            this.logger.logDebug("current time: " + this.currentTime.format(this.timeFormatter));
        }

        this.timeRegistry.publish(new TimeExchange(this.currentTimeEvents, this.currentTime));
    }

    /**
     * Moves the internal clock to now and publishes the new time along with the resulting time events to all
     * subscribers
     */
    @Override
    public void run() {
        try {
            this.currentTime = ZonedDateTime.now(this.hostTimeZone).truncatedTo(ChronoUnit.SECONDS);
            this.currentEpochSecond = this.currentTime.toEpochSecond();
            this.currentTimeEvents = TimeEventProvider.getTimeEvents(this.currentTime);

            this.timeRegistry.publish(new TimeExchange(this.currentTimeEvents, this.currentTime));
        } catch (Exception e) {
            this.logger.logError("Something has gone terribly wrong, trying to continue ...", e);
            e.printStackTrace();
        }
    }

    /**
     * Returns the current time.
     * @return the current time
     */
    public ZonedDateTime getCurrentTime() {
        return this.currentTime;
    }

    /**
     * Returns a set of all time events occurring at the current time.
     * @return a set of all time events occurring at current time
     */
    public EnumSet<TimeSubscribeEnum> getCurrentTimeEvents() {
        return this.currentTimeEvents;
    }

    /**
     * Returns the time zone this time manager uses
     * @return the time zone of this time manager
     */
    public ZoneId getHostTimeZone() {
        return this.hostTimeZone;
    }

    /**
     * Returns the time at the start of the run of the OSH.
     * @return the time at the start
     */
    public ZonedDateTime getTimeAtStart() {
        return this.timeAtStart;
    }

    /**
     * Returns the current time represented as seconds since epoch
     * @return the current time in seconds since epoch
     */
    public long getCurrentEpochSecond() {
        return this.currentEpochSecond;
    }
}
