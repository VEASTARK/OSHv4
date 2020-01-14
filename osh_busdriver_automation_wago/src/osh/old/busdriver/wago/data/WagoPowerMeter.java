package osh.old.busdriver.wago.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * A meter device of the wago xml interface
 *
 * @author Kaibin Bao
 */
@XmlType
public class WagoPowerMeter {
    @XmlTransient
    private int groupId;

    @XmlPath("@port")
    private int meterId;

    @XmlPath("voltage/@value")
    private double voltage;

    @XmlPath("current/@value")
    private double current;

    @XmlPath("power/@value")
    private double power;

    @XmlPath("energy/@value")
    private double energy;

    @XmlPath("transformerdivisor/@value")
    private double transformerDivisor;

    @XmlPath("dcfilter/@value")
    private boolean dcFilter;

    @XmlPath("power/@time")
    private long timestamp;

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getMeterId() {
        return this.meterId;
    }

    public double getVoltage() {
        return this.voltage;
    }

    public double getCurrent() {
        return this.current;
    }

    public double getPower() {
        return this.power;
    }

    public double getEnergy() {
        return this.energy;
    }

    public double getTransformerDivisor() {
        return this.transformerDivisor;
    }

    public boolean isDcFilter() {
        return this.dcFilter;
    }
}
