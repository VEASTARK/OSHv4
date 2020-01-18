package osh.old.busdriver.wago;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
public enum Wago750860ModuleType {
    CONTROLLER(0),
    METER(1),
    SWITCH(2),
    VIRTUAL_SWITCH(3),
    DIGITAL_INPUT(4),
    ANALOG_INPUT(5),
    VIRTUAL_METER(6),
    DIGITAL_OUTPUT(7);

    private final byte value;

    Wago750860ModuleType(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return this.value;
    }
}
