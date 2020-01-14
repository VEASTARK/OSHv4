package osh.esc.grid;

import osh.esc.grid.carrier.ConnectionType;

import java.io.Serializable;

/**
 * @author Ingo Mauser
 */
public class EnergyRelation<T extends ConnectionType> implements Serializable {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = -6270348787737984480L;

    private final T activeToPassiveConnection;
    private final T passiveToActiveConnection;

    private final EnergySourceSink activeEntity;
    private final EnergySourceSink passiveEntity;

    /**
     * CONSTRUCTOR
     */
    public EnergyRelation(
            EnergySourceSink activeEntity,
            EnergySourceSink passiveEntity,
            T activeToPassiveConnection,
            T passiveToActiveConnection) {
        super();
        this.activeEntity = activeEntity;
        this.passiveEntity = passiveEntity;
        this.activeToPassiveConnection = activeToPassiveConnection;
        this.passiveToActiveConnection = passiveToActiveConnection;
    }

    public T getActiveToPassive() {
        return this.activeToPassiveConnection;
    }

    public T getPassiveToActive() {
        return this.passiveToActiveConnection;
    }

    public EnergySourceSink getActiveEntity() {
        return this.activeEntity;
    }

    public EnergySourceSink getPassiveEntity() {
        return this.passiveEntity;
    }

    @Override
    public String toString() {
        return "[active] " + this.activeEntity.getDeviceUuid() + " <--> [passive] " + this.passiveEntity.getDeviceUuid();
    }

}
