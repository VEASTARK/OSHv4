package osh.datatypes.logging.general;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about the performance of the ea-algorithm.
 *
 * @author Sebastian Kramer
 */
public class EALogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final double averageGenerationsUsed;
    private final double[][] averageFitnessChange;
    private final double[][] averageFitnessSpread;
    private final double[] averageHomogeneity;
    private final int noOfOptimizations;
    private final double[] cervisia;

    /**
     * Constructs this log exchange with the given sender, timestamp and the performance values of the ea-algorithm.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param averageGenerationsUsed the average number ov generations used before stopping
     * @param averageFitnessChange the average fitness change per generation
     * @param averageFitnessSpread the average fitness spread in a generation
     * @param averageHomogeneity the average homogeneity of candidate solutions in a generations
     * @param noOfOptimizations the number of optimizations
     * @param cervisia the average cervisia per houshold device
     */
    public EALogObject(UUID sender, ZonedDateTime timestamp, double averageGenerationsUsed, double[][] averageFitnessChange,
                       double[][] averageFitnessSpread, double[] averageHomogeneity, int noOfOptimizations, double[] cervisia) {
        super(sender, timestamp);
        this.averageGenerationsUsed = averageGenerationsUsed;
        this.averageFitnessChange = averageFitnessChange;
        this.averageFitnessSpread = averageFitnessSpread;
        this.averageHomogeneity = averageHomogeneity;
        this.noOfOptimizations = noOfOptimizations;
        this.cervisia = cervisia;
    }

    public double getAverageGenerationsUsed() {
        return this.averageGenerationsUsed;
    }

    public double[][] getAverageFitnessChange() {
        return this.averageFitnessChange;
    }

    public double[][] getAverageFitnessSpread() {
        return this.averageFitnessSpread;
    }

    public double[] getAverageHomogeneity() {
        return this.averageHomogeneity;
    }

    public int getNoOfOptimizations() {
        return this.noOfOptimizations;
    }

    public double[] getCervisia() {
        return this.cervisia;
    }
}
