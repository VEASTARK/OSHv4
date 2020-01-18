package osh.mgmt.localobserver.miele;

import osh.datatypes.registry.oc.state.IAction;
import osh.mgmt.localobserver.ipp.MieleApplianceIPP;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class MieleAction implements IAction {

    private UUID deviceID;
    private long programmedAt;
    private MieleApplianceIPP ipp;


    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    private MieleAction() {
    }

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param programmedAt
     */
    public MieleAction(
            UUID deviceID,
            long programmedAt,
            MieleApplianceIPP ipp) {
        this.deviceID = deviceID;
        this.programmedAt = programmedAt;
        this.ipp = ipp;
    }

    @Override
    public UUID getDeviceId() {
        return this.deviceID;
    }

    @Override
    public long getTimestamp() {
        return this.programmedAt;
    }

    public MieleApplianceIPP getIPP() {
        return this.ipp;
    }

    @Override
    public boolean equals(IAction other) {
        if (!(other instanceof MieleAction))
            return false;

        MieleAction otherMieleAction = (MieleAction) other;

        return this.deviceID.equals(otherMieleAction.deviceID);
    }

    @Override
    public int hashCode() {
        return this.deviceID.hashCode();
    }

    // TODO: keep for prediction
//	@Override
//	public IAction createAction(long newTimestamp) {
//		MieleEAPart newEAPart = new MieleEAPart(deviceID,newTimestamp,newTimestamp,newTimestamp+eapart.getOriginalDof(),eapart.getProfile(), true);
//
//		return new MieleAction(deviceID, newTimestamp, newEAPart);
//	}

}
