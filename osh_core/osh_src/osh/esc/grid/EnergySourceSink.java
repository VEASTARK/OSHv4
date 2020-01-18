package osh.esc.grid;

import java.io.Serializable;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class EnergySourceSink extends EnergyDevice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5283939620436042291L;
    private final UUID deviceUuid;

    // additional information...?

    /**
     * CONSTRUCTOR
     *
     * @param deviceUuid
     */
    public EnergySourceSink(UUID deviceUuid) {
        this.deviceUuid = deviceUuid;
    }


    public UUID getDeviceUuid() {
        return this.deviceUuid;
    }

    @Override
    public String toString() {
        return this.deviceUuid.toString();
    }


    @Override
    public int hashCode() {
        return this.deviceUuid == null ? 0 : this.deviceUuid.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        EnergySourceSink other = (EnergySourceSink) obj;
        if (this.deviceUuid == null) {
            return other.deviceUuid == null;
        } else return this.deviceUuid.equals(other.deviceUuid);
    }
}
