package osh.core.oc;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;

import java.util.UUID;

/**
 * container class for the local observer and controller
 *
 * @author Florian Allerding
 */
public class LocalOCUnit extends OCUnit {

    public final LocalObserver localObserver;
    public final LocalController localController;
    private DeviceTypes deviceType;
    private DeviceClassification deviceClassification;


    public LocalOCUnit(
            IOSHOC osh,
            UUID deviceID,
            LocalObserver localObserver,
            LocalController localController) {
        super(deviceID, osh);

        //create local controller/observer  and assign to the OCUnit
        this.localObserver = localObserver;
        this.localController = localController;

        if (localObserver != null) {
            this.localObserver.assignLocalOCUnit(this);
        }
        if (localController != null) {
            this.localController.assignLocalOCUnit(this);
        }
    }

    public DeviceClassification getDeviceClassification() {
        return this.deviceClassification;
    }

    public void setDeviceClassification(DeviceClassification deviceClassification) {
        this.deviceClassification = deviceClassification;
    }

    public DeviceTypes getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(DeviceTypes deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String toString() {
        return "LocalUnit " + this.getUnitID();
    }
}
