package osh.esc.grid;

import java.io.Serializable;
import java.util.List;

public class InitializedEnergyRelation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4621503592492456770L;

    //	private UUID sourceId;
    private int sourceId;
    private InitializedEnergyRelationTarget[] targetsArray;

    public InitializedEnergyRelation(int sourceId, List<InitializedEnergyRelationTarget> targets) {
        this.sourceId = sourceId;
        this.targetsArray = new InitializedEnergyRelationTarget[targets.size()];
        this.targetsArray = targets.toArray(this.targetsArray);
    }

    /**
     * do not use - only for serialisation
     */
    @Deprecated
    protected InitializedEnergyRelation() {
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public InitializedEnergyRelationTarget[] getTargets() {
        return this.targetsArray;
    }
}
