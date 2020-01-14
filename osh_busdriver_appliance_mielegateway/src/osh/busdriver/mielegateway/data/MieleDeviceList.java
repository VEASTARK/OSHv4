package osh.busdriver.mielegateway.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * The XML homebus root node
 *
 * @author Kaibin Bao
 */
@XmlRootElement(name = "DEVICES")
public class MieleDeviceList {
    @XmlPath("device")
    private List<MieleDeviceHomeBusDataREST> devices;

    public List<MieleDeviceHomeBusDataREST> getDevices() {
        return this.devices;
    }

    public void setDevices(List<MieleDeviceHomeBusDataREST> devices) {
        this.devices = devices;
    }
}
