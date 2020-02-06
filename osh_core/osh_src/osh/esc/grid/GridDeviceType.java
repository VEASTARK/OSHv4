package osh.esc.grid;

/**
 * Represents different special types of devices in the context of grid-calculations
 *
 * @author Sebastian Kramer
 */
public enum GridDeviceType {

    PV ("pv"),
    CHP ("chp"),
    BATTERY ("battery");

    private final String name;

    /**
     * Generate a device type with the given name
     *
     * @param name
     */
    GridDeviceType(String name) {
        this.name = name;
    }

    /**
     * Return the name of this devic type.
     *
     * @return the name of this device type
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the device type corresponding to the given name.
     *
     * @param v the name
     *
     * @return the device type corresponding to the given name
     */
    public static GridDeviceType fromString(String v) {
        for (GridDeviceType g : GridDeviceType.values()) {
            if (g.name.equalsIgnoreCase(v)) {
                return g;
            }
        }

        throw new IllegalArgumentException(v);
    }
}
