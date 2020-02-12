package osh.datatypes.registry.oc.commands.globalcontroller;

import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.registry.CommandExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Till Schuberth, Ingo Mauser
 */
public class EAPredictionCommandExchange<PredictionType extends IPrediction> extends CommandExchange {

    private final PredictionType prediction;

    public EAPredictionCommandExchange(
            UUID sender,
            UUID receiver,
            ZonedDateTime timestamp,
            PredictionType prediction) {
        super(sender, receiver, timestamp);

        this.prediction = prediction;
    }

    public PredictionType getPrediction() {
        return this.prediction;
    }
}
