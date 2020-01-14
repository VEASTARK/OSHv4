package osh.datatypes.ea;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.ILoadProfile;

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
    private double lukewarmCervisia;

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
    public Schedule(ILoadProfile<Commodity> profile, double lukewarmCervisia, String scheduleName) {
        super();

        this.profile = profile;
        this.lukewarmCervisia = lukewarmCervisia;
        this.scheduleName = scheduleName;
    }


    public ILoadProfile<Commodity> getProfile() {
        return this.profile;
    }

    public double getLukewarmCervisia() {
        return this.lukewarmCervisia;
    }

    public String getScheduleName() {
        return this.scheduleName;
    }

    /**
     * merge two schedules (use profile.merge and add cervisia)
     */
    public Schedule merge(Schedule other) {
        double cervisia;
        ILoadProfile<Commodity> profile;
        try {
            profile = this.profile.merge(other.profile, 0);
        } catch (Exception ex) {
            throw new RuntimeException("Bad error merging profiles", ex);
        }
        cervisia = this.lukewarmCervisia + other.lukewarmCervisia;

        return new Schedule(profile, cervisia, this.scheduleName);
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