package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.ea.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class GUIScheduleComExchange extends CALComExchange {

    private final List<Schedule> schedules;
    private final int stepSize;

    /**
     * CONSTRUCTOR
     */
    public GUIScheduleComExchange(
            UUID deviceID,
            Long timestamp,
            List<Schedule> schedules,
            int stepSize) {
        super(deviceID, timestamp);

        this.schedules = new ArrayList<>();
        this.stepSize = stepSize;

        synchronized (schedules) {
            for (Schedule s : schedules) {
                Schedule clonedS = s.clone();
                this.schedules.add(clonedS);
            }
        }
    }


    public List<Schedule> getSchedules() {
        return this.schedules;
    }

    public int getStepSize() {
        return this.stepSize;
    }

}
