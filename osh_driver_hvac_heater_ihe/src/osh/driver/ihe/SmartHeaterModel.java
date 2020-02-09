package osh.driver.ihe;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Heating element with the following specifications:<br>
 * 8 power states: 0...3.5 kW (in steps of 0.5 kW)<br>
 * <br>
 * 3 heating sub-elements:<br>
 * (0) 0.5 kW (1) 1.0 kW (2) 2.0 kW<br>
 * <br>
 * Min circuit times:<br>
 * (0) ? seconds (1) ? seconds (2) ? seconds<br>
 * <br>
 * TODO: 99% efficiency (or as parameter)
 *
 * @author Ingo Mauser
 */
public class SmartHeaterModel implements Serializable {

    // ### configuration ###

    /**
     *
     */
    private static final long serialVersionUID = 3818872291707857561L;
    private final int setTemperature;
    private static final int powerDelta = 100;

    // available states                0    1     2     3     4     5     6     7
    private static final int[] powerStates = {0, 500, 1000, 1500, 2000, 2500, 3000, 3500};

    // minimum on and off times
    private static final int[] minOnTimes = {10, 10, 10};
    private static final int[] minOffTimes = {110, 170, 230};

    // ### variables ###
    /**
     * 0...7
     */
    private int currentState;
    /**
     * 500 1000 2000
     */
    private boolean[] currentStates;
    /**
     * timestamp, timestamp, timestamp (absolute times)
     */
    private long[] timestampOfLastChangePerSubElement;


    // ### for logging purposes ###

    private long timestampOfLastChange;

    // sub-element           0 1 2
    private final int[] counter = {0, 0, 0}; // count only switch ON !
    private final long[] runtime = {0, 0, 0}; // time in state ON

    //									0,5kW 1kW 1.5kW 2kW 2.5kW 3kW 3.5kW
    private final long[] powerTierRunTimes = {0, 0, 0, 0, 0, 0, 0}; //time power tier was on
//	private int[] minCircuitTimeViolatedCounter = {0,0,0};


    /**
     * CONSTRUCTOR
     */
    public SmartHeaterModel(
            int setTemperature,
            int initialState,
            long[] timestampOfLastChangePerSubElement) {
        this.setTemperature = setTemperature;
        this.currentState = initialState;
        this.currentStates = convertStateToStates(initialState);
        this.timestampOfLastChangePerSubElement = Arrays.copyOf(timestampOfLastChangePerSubElement,
                timestampOfLastChangePerSubElement.length);
    }

    public SmartHeaterModel(SmartHeaterModel other) {
        this.setTemperature = other.setTemperature;
        this.currentState = other.currentState;
        this.currentStates = Arrays.copyOf(other.currentStates, other.currentStates.length);
        this.timestampOfLastChangePerSubElement = Arrays.copyOf(other.timestampOfLastChangePerSubElement,
                other.timestampOfLastChangePerSubElement.length);
        this.timestampOfLastChange = 0;
    }

    public SmartHeaterModel() {
        this.setTemperature = 0;
    }

    private static boolean[] convertStateToStates(int state) {
        boolean[] states = {false, false, false};
        if (state % 2 == 1) {
            states[0] = true;
        }
        if (state == 2 || state == 3 || state == 6 || state == 7) {
            states[1] = true;
        }
        if (state >= 4) {
            states[2] = true;
        }
        return states;
    }

    /**
     * @param availablePower [W] (value; negative value is net production/feed-in of building)
     */
    public void updateAvailablePower(long now, double availablePower, double currentTemperature) {

        // power delta
        double usedPower = availablePower;
        usedPower += powerDelta; // safety margin of 100W

        // power used by smart heater
        int selfUsePower = this.currentState * 500;
        usedPower -= selfUsePower;

        // if net consumption in building
        if (usedPower >= 0) {
            usedPower = 0;
        }

        usedPower = Math.abs(usedPower);

        // calculate new state of heater
        double tempMaxPower = usedPower / 1000.0 * 2.0;
        int newState = Math.min(7, (int) tempMaxPower);

        // check temperature constraint
        if (currentTemperature > this.setTemperature) {
            // switch OFF
            newState = 0;
        }

        // check whether new state possible...(time logic)
        boolean[] newStates = convertStateToStates(newState);
        boolean[] toBeSwitched = {false, false, false};
        for (int i = 0; i < 3; i++) {
            if (this.currentStates[i] != newStates[i]) {
                toBeSwitched[i] = true;
            }
        }
        boolean timeViolation = false;
        for (int i = 0; i < 3; i++) {
            if (toBeSwitched[i]) {
                long diff = now - this.timestampOfLastChangePerSubElement[i];
                if (newStates[i] && diff < minOffTimes[i]) {
                    //switch on violation
                    timeViolation = true;
                } else if (!newStates[i] && diff < minOnTimes[i]) {
                    //switch off violation
                    timeViolation = true;
                }
            }
        }

        // update state if no min time is violated...
        if (!timeViolation) {
            this.updateState(now, newState);
        }
    }

    /**
     * @return [W] > 0
     */
    public double getPower() {
        return powerStates[this.currentState];
    }

    public int getCurrentState() {
        return this.currentState;
    }

    public boolean isOn() {
        return this.currentState > 0;
    }


    // ### helper methods ###

    public long[] getTimestampOfLastChangePerSubElement() {
        return this.timestampOfLastChangePerSubElement.clone();
    }

    private void updateState(long timestamp, int newState) {
        int oldState = this.currentState;

        if (oldState != newState) {
            this.updateCounters(timestamp, oldState, newState);
        }

        this.currentState = newState;
        this.currentStates = convertStateToStates(newState);
    }


    // ### for logging purposes ###

    private void updateCounters(long now, int oldState, int newState) {
        // increase runtime
        long timeDiff = now - this.timestampOfLastChange;
        this.timestampOfLastChange = now;

        if (oldState % 2 == 1) {
            this.runtime[0] += timeDiff;
        }
        if (oldState == 2 || oldState == 3 || oldState == 6 || oldState == 7) {
            this.runtime[1] += timeDiff;
        }
        if (oldState >= 4) {
            this.runtime[2] += timeDiff;
        }

        if (oldState != 0) {
            this.powerTierRunTimes[oldState - 1] += timeDiff;
        }

        // increase switch counters (only switch on)
        if ((oldState % 2 == 0) && (newState % 2 == 1)) {
            this.counter[0]++;
            this.timestampOfLastChangePerSubElement[0] = now;
        }
        if (oldState != newState) {
            if (oldState != 2 && oldState != 3 && oldState != 6 && oldState != 7) {
                if (newState == 2 || newState == 3 || newState == 6 || newState == 7) {
                    this.counter[1]++;
                    this.timestampOfLastChangePerSubElement[1] = now;
                }
            }
        }
        if (oldState < 4 && newState >= 4) {
            this.counter[2]++;
            this.timestampOfLastChangePerSubElement[2] = now;
        }

        // increase switch counters (switch off)
        if ((newState % 2 == 0) && (oldState % 2 == 1)) {
            this.timestampOfLastChangePerSubElement[0] = now;
        }
        if (oldState != newState) {
            if (newState != 2 && newState != 3 && newState != 6 && newState != 7) {
                if (oldState == 2 || oldState == 3 || oldState == 6 || oldState == 7) {
                    this.timestampOfLastChangePerSubElement[1] = now;
                }
            }
        }
        if (newState < 4 && oldState >= 4) {
            this.timestampOfLastChangePerSubElement[2] = now;
        }

    }

    public int[] getCounter() {
        return new int[]{this.counter[0], this.counter[1], this.counter[2]};
    }

    public long[] getRuntime() {
        return new long[]{this.runtime[0], this.runtime[1], this.runtime[2]};
    }

    public long[] getPowerTierRunTimes() {
        return new long[]{this.powerTierRunTimes[0], this.powerTierRunTimes[1], this.powerTierRunTimes[2],
                this.powerTierRunTimes[3], this.powerTierRunTimes[4], this.powerTierRunTimes[5], this.powerTierRunTimes[6]};
    }

    public int getSetTemperature() {
        return this.setTemperature;
    }

//	public int[] getMinCircuitTimeViolatedCounter() {
//		int[] cloned = {
//				minCircuitTimeViolatedCounter[0],
//				minCircuitTimeViolatedCounter[1],
//				minCircuitTimeViolatedCounter[2]};
//		return cloned;
//	}

}
