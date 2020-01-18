package osh.old.busdriver.wago.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * The wago xml root node
 *
 * @author Kaibin Bao
 */
@XmlRootElement(name = "answer")
public class WagoDeviceList {
    @XmlPath("device[@type='uape']")
    private List<WagoMeterGroup> inputs;

    @XmlPath("device[@type='relay']")
    private List<WagoRelayData> relays;

    @XmlPath("device[@type='vs']")
    private List<WagoVirtualGroup> vsGroups;

    @XmlPath("device[@type='di8']")
    private List<WagoDiGroup> di8Groups;

    @XmlPath("device[@type='do8']")
    private List<WagoDoGroup> do8Groups;

    public List<WagoMeterGroup> getInputs() {
        return this.inputs;
    }

    public List<WagoRelayData> getRelays() {
        return this.relays;
    }

    public List<WagoVirtualGroup> getVsGroups() {
        return this.vsGroups;
    }

    public List<WagoDiGroup> getDi8Groups() {
        return this.di8Groups;
    }

    public List<WagoDoGroup> getDo8Groups() {
        return this.do8Groups;
    }
}
