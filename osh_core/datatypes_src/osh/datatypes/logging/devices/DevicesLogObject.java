package osh.datatypes.logging.devices;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about a singular device.
 *
 * @author Sebastian Kramer
 */
public class DevicesLogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final int plannedDeviceStarts;
    private final int actualDeviceStarts;
    private final double activePowerConsumption;
    private final int[] profileStarts;
    private final int[] dofs;
    private final int[] startTimes;
    private final int[] profilesSelected;
    private final DeviceTypes deviceIdentifier;

    /**
     * Constructs this log exchange with the given sender, timestamp and information about the runs of the
     * device.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param plannedDeviceStarts the number of scheduled starts of the device
     * @param actualDeviceStarts the actual number of starts of the device
     * @param activePowerConsumption the active power consumption of the device
     * @param profileStarts the number of starts per profile of the device
     * @param dofs the distribution of the degrees-of-freedom used
     * @param startTimes the distribution of the start-times
     * @param profilesSelected the number of times specific profiles were selected
     * @param deviceIdentifier the type of the device
     */
    public DevicesLogObject(UUID sender, ZonedDateTime timestamp, int plannedDeviceStarts, int actualDeviceStarts,
                            double activePowerConsumption, int[] profileStarts, int[] dofs, int[] startTimes, int[] profilesSelected,
                            DeviceTypes deviceIdentifier) {
        super(sender, timestamp);
        this.plannedDeviceStarts = plannedDeviceStarts;
        this.actualDeviceStarts = actualDeviceStarts;
        this.activePowerConsumption = activePowerConsumption;
        this.profileStarts = profileStarts;
        this.dofs = dofs;
        this.startTimes = startTimes;
        this.profilesSelected = profilesSelected;
        this.deviceIdentifier = deviceIdentifier;
    }

    public int getPlannedDeviceStarts() {
        return this.plannedDeviceStarts;
    }

    public int getActualDeviceStarts() {
        return this.actualDeviceStarts;
    }

    public double getActivePowerConsumption() {
        return this.activePowerConsumption;
    }

    public int[] getProfileStarts() {
        return this.profileStarts;
    }

    public int[] getDofs() {
        return this.dofs;
    }

    public int[] getStartTimes() {
        return this.startTimes;
    }

    public int[] getProfilesSelected() {
        return this.profilesSelected;
    }

    public DeviceTypes getDeviceIdentifier() {
        return this.deviceIdentifier;
    }
}
