package osh.datatypes.registry.oc.commands.globalcontroller;

import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.registry.CommandExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @param <PhenotypeType>
 * @author Till Schuberth, Ingo Mauser
 */
public class EASolutionCommandExchange<PhenotypeType extends ISolution> extends CommandExchange {

    /**
     *
     */
    private static final long serialVersionUID = 4399537651455121419L;
    private final PhenotypeType phenotype;

    public EASolutionCommandExchange(
            UUID sender,
            UUID receiver,
            ZonedDateTime timestamp,
            PhenotypeType phenotype) {
        super(sender, receiver, timestamp);

        this.phenotype = phenotype;
    }

    public PhenotypeType getPhenotype() {
        return this.phenotype;
    }

}
