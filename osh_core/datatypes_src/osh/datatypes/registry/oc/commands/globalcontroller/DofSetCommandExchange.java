package osh.datatypes.registry.oc.commands.globalcontroller;

import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.util.Objects;
import java.util.UUID;


/**
 * @author Till Schuberth, Ingo Mauser, Kaibin Bao
 */
public class DofSetCommandExchange extends CommandExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = -3381723233858020252L;
    private final Integer firstDof;
    private final Integer secondDoF;

    public DofSetCommandExchange(
            UUID sender,
            UUID receiver,
            long timestamp,
            Integer firstDof,
            Integer secondDoF) {
        super(sender, receiver, timestamp);
        Objects.requireNonNull(firstDof, "dof cannot be not set(=null)");
        Objects.requireNonNull(secondDoF, "dof cannot be not set(=null)");

        this.firstDof = firstDof;
        this.secondDoF = secondDoF;

        if (firstDof < 0) throw new IllegalArgumentException("First Dof is less than zero");
        if (secondDoF < 0) throw new IllegalArgumentException("Second Dof is less than zero");
    }

    public Integer getDof() {
        return this.firstDof;
    }

    public Integer getSecondDof() {
        return this.secondDoF;
    }
}
