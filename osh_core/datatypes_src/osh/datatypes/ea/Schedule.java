package osh.datatypes.ea;

import osh.configuration.oc.EAObjectives;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.ILoadProfile;
import osh.utils.dataStructures.Enum2DoubleMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


/**
 * @author Florian Allerding, Till Schuberth
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Schedule implements Serializable {

    private static final long serialVersionUID = 8715958618527521394L;

    private String scheduleName;

    private ILoadProfile<Commodity> profile;

    /**
     * needed lukewarm cervisia to pay for this profile (other costs)
     */
    private Enum2DoubleMap<EAObjectives> lukewarmCervisia;

    /**
     * for deep copy
     */
    protected Schedule() {
        super();
    }

    /**
     * CONSTRUCTOR
     *
     * @param profile
     * @param lukewarmCervisia
     */
    public Schedule(ILoadProfile<Commodity> profile, Enum2DoubleMap<EAObjectives> lukewarmCervisia, String scheduleName) {
        super();

        this.profile = profile;
        this.lukewarmCervisia = lukewarmCervisia;
        this.scheduleName = scheduleName;
    }


    public ILoadProfile<Commodity> getProfile() {
        return this.profile;
    }

    public Enum2DoubleMap<EAObjectives> getLukewarmCervisia() {
        return this.lukewarmCervisia;
    }

    public String getScheduleName() {
        return this.scheduleName;
    }

    public Schedule clone() {
        ILoadProfile<Commodity> clonedProfile = this.profile.clone();
        return new Schedule(clonedProfile, this.lukewarmCervisia, this.scheduleName);
    }

    @Override
    public String toString() {
        return "LoadProfile=" + this.profile.toString() + ",cervisia=" + this.lukewarmCervisia;
    }
}