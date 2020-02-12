package osh.datatypes.gui;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * @author ???
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceTableEntry implements Comparable<DeviceTableEntry> {

    private final int entry;
    private final UUID id;
    private final String name;
    private final String representation;
    private final String reschedule;


    @Deprecated
    public DeviceTableEntry() {
        this(0, null, "", "", "");
    }

    public DeviceTableEntry(int entry, UUID id, String name, String reschedule, String representation) {
        super();

        this.entry = entry;
        this.id = id;
        this.name = name;
        this.reschedule = reschedule;
        this.representation = representation;
    }


    public int getEntry() {
        return this.entry;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getRepresentation() {
        return this.representation;
    }

    public String getReschedule() {
        return this.reschedule;
    }

    @Override
    public int compareTo(DeviceTableEntry o) {
        return this.entry - o.entry;
    }

    @Override
    public DeviceTableEntry clone() {
        return new DeviceTableEntry(
                this.entry, this.id, this.name, this.reschedule, this.representation);
    }


}