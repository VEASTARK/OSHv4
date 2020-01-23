package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.ea.Schedule;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


/**
 * @author Florian Allerding, Ingo Mauser
 */
public class GUIScheduleStateExchange extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = -9037948484261116427L;
    private final List<Schedule> schedules;
    private final int stepSize;

    public GUIScheduleStateExchange(
            UUID sender,
            ZonedDateTime timestamp,
            List<Schedule> schedules,
            int stepSize) {
        super(sender, timestamp);
        this.schedules = schedules;
        this.stepSize = stepSize;
    }

    public List<Schedule> getDebugGetSchedules() {
        return this.schedules;
    }

    public int getStepSize() {
        return this.stepSize;
    }

    @Override
    public GUIScheduleStateExchange clone() {
        //TODO: do proper cloning
        return (GUIScheduleStateExchange) super.clone();
    }

}
