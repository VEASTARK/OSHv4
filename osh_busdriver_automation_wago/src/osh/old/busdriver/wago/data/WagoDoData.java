package osh.old.busdriver.wago.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * wago xml interface
 *
 * @author tisu
 */
@XmlType
public class WagoDoData {
    @XmlTransient
    private int groupId;

    @XmlPath("@id")
    private int id;

    @XmlPath("@state")
    private boolean state;

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getId() {
        return this.id;
    }

    public boolean getState() {
        return this.state;
    }
}
