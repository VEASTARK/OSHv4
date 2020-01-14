package osh.esc.grid;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.Serializable;

public class InitializedEnergyRelation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4621503592492456770L;

    //	private UUID sourceId;
    private int sourceId;
    private ObjectArrayList<InitializedEnergyRelationTarget> targets;
    private InitializedEnergyRelationTarget[] targetsArray;

    public InitializedEnergyRelation(int sourceId, ObjectArrayList<InitializedEnergyRelationTarget> targets) {
        this.sourceId = sourceId;
        this.targets = targets;
    }

    /**
     * do not use - only for serialisation
     */
    @Deprecated
    protected InitializedEnergyRelation() {
    }

    public void addEnergyTarget(InitializedEnergyRelationTarget target) {
        if (!this.targets.contains(target)) {
            this.targets.add(target);
        }
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public void transformToArrayTargets() {
        this.targetsArray = new InitializedEnergyRelationTarget[this.targets.size()];
        this.targetsArray = this.targets.toArray(this.targetsArray);
    }

    public InitializedEnergyRelationTarget[] getTargets() {
        return this.targetsArray;
    }

    public void setTargets(InitializedEnergyRelationTarget[] targetsArray) {
        this.targetsArray = targetsArray;
    }

}
