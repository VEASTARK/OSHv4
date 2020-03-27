package osh.mgmt.globalcontroller.jmetal.logging;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.binarySet.BinarySet;
import osh.configuration.oc.EAObjectives;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.simulation.DatabaseLoggerThread;
import osh.utils.dataStructures.Enum2DoubleMap;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a simple logger implementing the EA logger interface {@link IEALogger}.
 *
 * @author Sebastian Kramer
 */
public class EALogger implements IEALogger {

    final boolean log;
    private final boolean logExtended;
    private final int logXthGeneration;
    private final IGlobalLogger logger;
    private final boolean logOverallEA;
    private int optimizationCounter;
    private double generationsUsed;
    private final int logExtendedGenerations;
    private final double[][] fitnessChange;
    private final double[][] fitnessSpread;
    private double[] homogeneity;
    private final double[] bestFirstFitness;
    private final double[][] cervisiaInformation = new double[5][2];
    final int objectiveCount;

    private PrintWriter additionalWriter;
    private long timestamp;

    /**
     * Constructs this ea-logger with the given global logger and the configuration parameters.
     *
     * @param globalLogger the global logger
     * @param log flag if anything should be logged
     * @param logExtended flag if extended population values should be logged
     * @param logXthGeneration counter after how many generations there should be a log of population values
     * @param logExtendedGenerations counter after how many generations there should be an exteneded log of population
     *                               values
     * @param logOverallEA flag if the number of optimization runs should be logges
     * @param eaObjectives collection of the objectives of the optimization
     */
    public EALogger(
            IGlobalLogger globalLogger,
            boolean log,
            boolean logExtended,
            int logXthGeneration,
            int logExtendedGenerations,
            boolean logOverallEA,
            Collection<EAObjectives> eaObjectives) {

        this.log = log;
        this.logExtended = logExtended;
        this.logXthGeneration = logXthGeneration;
        this.logExtendedGenerations = logExtendedGenerations;
        this.logger = globalLogger;
        this.logOverallEA = logOverallEA;
        this.objectiveCount = eaObjectives.size();

        this.bestFirstFitness = new double[this.objectiveCount];
        this.fitnessChange = new double[logExtendedGenerations][this.objectiveCount];
        this.fitnessSpread = new double[logExtendedGenerations][this.objectiveCount];
        this.homogeneity = new double[logExtendedGenerations];
    }

    @Override
    public void attachWriter(PrintWriter writer) {
        this.additionalWriter = writer;
    }

    @Override
    public void detachWriter() {
        this.additionalWriter.flush();
        this.additionalWriter.close();
        this.additionalWriter = null;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Logs the given message.
     *
     * @param message the message to log
     */
    void log(String message) {
        this.log(message, false);
    }

    /**
     * Logs the given message with an optional prepended timestamp.
     *
     * @param message the message to log
     * @param prependTime flag if a timestamp should be prepended
     */
    void log(String message, boolean prependTime) {
        this.logger.logDebug(message);
        if (this.additionalWriter != null) {
            if (prependTime) {
                this.additionalWriter.println("[" + this.timestamp + "] " + message);
            } else {
                this.additionalWriter.println(message);
            }
        }
    }

    /**
     * Returns the array containing the best fitness values for each objective.
     *
     * @return the array containing the best fitness values for each objective
     */
    double[] getBestFirstFitness() {
        return this.bestFirstFitness;
    }

    @Override
    public void logStart(Algorithm<?> usedAlgorithm) {
        if (this.log) {
            this.log("===    New Optimization, using " + usedAlgorithm.getDescription() + "    ===", true);
            Arrays.fill(this.getBestFirstFitness(), Double.NaN);
        }
    }

    @Override
    public void logEnd(Solution<?> bestSolution) {
        if (this.log) {
            StringBuilder logMessage = new StringBuilder("===    Finished Optimization, final Fitness: ");
            for (int i = 0; i < this.objectiveCount; i++) {
                logMessage.append(" ").append(bestSolution.getObjective(i)).append(" ");
            }
            logMessage.append("    ===");
            this.log(logMessage.toString());

        }
        if (this.logOverallEA) {
            this.optimizationCounter++;
        }
    }

    @Override
    public void logPopulation(List<? extends Solution<?>> population, int generation) {
        if (this.log) {

            String logMessage = "";

            if (generation % this.logXthGeneration == 0) {

                double[] bestFitness = new double[this.objectiveCount];

                for (int i = 0; i < this.objectiveCount; i++) {
                    population.sort(this.getComparatorForObjective(i));

                    bestFitness[i] = population.get(0).getObjective(i);
                    if (Double.isNaN(this.getBestFirstFitness()[i])) {
                        this.getBestFirstFitness()[i] = bestFitness[i];
                    }
                }

                logMessage = "[" + generation + "] -- BestFitness: " + Arrays.toString(bestFitness);

            } else {
                for (int i = 0; i < this.objectiveCount; i++) {
                    if (Double.isNaN(this.getBestFirstFitness()[i])) {
                        population.sort(this.getComparatorForObjective(i));
                        this.getBestFirstFitness()[i] = population.get(0).getObjective(i);
                    }
                }
            }

            if ((this.logExtended && generation % this.logXthGeneration == 0)
                    || (this.logOverallEA && generation < this.logExtendedGenerations)) {
                logMessage += this.logExtendedPopulation(population, generation);

            }

            if (!logMessage.isEmpty()) {
                this.log(logMessage);
            }

        }
        if (this.logOverallEA) {
            this.generationsUsed++;
        }
    }

    /**
     * Logs the extended population values and returns an optional log-message.
     *
     * @param population the population to log
     * @param generation the generation the ea algorithm is in
     *
     * @return the optional log message or an empty string
     */
    String logExtendedPopulation(List<? extends Solution<?>> population, int generation) {

        double[][] popValues = this.getExtendedPopulationValues(population);

        if ((this.logOverallEA && generation < this.logExtendedGenerations)) {

            this.homogeneity[generation] += popValues[0][0];

            for (int i = 0; i < this.objectiveCount; i++) {
                population.sort(this.getComparatorForObjective(i));
                this.fitnessChange[generation][i] += population.get(0).getObjective(i) / this.getBestFirstFitness()[i];
                this.fitnessSpread[generation][i] += popValues[1][i];
            }
        }

        if ((this.logExtended && generation % this.logXthGeneration == 0)) {
            return " -- Homogeneity mean: " + popValues[0][0] + " -- max: " + popValues[0][1]
                    + " -- deltaFitnessSpread: " + Arrays.toString(popValues[1]);
        }

        return "";
    }

    @Override
    public void logAdditional(String message) {
        if (this.log) {
            this.log(message);
        }
    }

    @Override
    public void shutdown() {
        if (this.log && DatabaseLoggerThread.isIsLogToDatabase()) {
            this.generationsUsed /= this.optimizationCounter;

            for (int i = 0; i < this.objectiveCount; i++) {
                this.fitnessChange[i] = Arrays.stream(this.fitnessChange[i]).map(d -> d / this.optimizationCounter).toArray();
                this.fitnessSpread[i] = Arrays.stream(this.fitnessSpread[i]).map(d -> d / this.optimizationCounter).toArray();
            }

            this.homogeneity = Arrays.stream(this.homogeneity).map(d -> d / this.optimizationCounter).toArray();

            double[] cervisiaResults = new double[5];
            for (int i = 0; i < cervisiaResults.length; i++) {
                if (this.cervisiaInformation[i][1] != 0) {
                    cervisiaResults[i] = this.cervisiaInformation[i][0] / this.cervisiaInformation[i][1];
                }
            }

            //TODO: change logging to include multi-objective as soon as next backwards-compatibility breaking update
            // is released
            DatabaseLoggerThread.enqueueGA(this.generationsUsed, this.fitnessChange[0], this.fitnessSpread[0],
                    this.homogeneity, this.optimizationCounter, cervisiaResults);
        }
    }

    /* [0][0] = homogeneity
     * [0][1] = maxDifference
     * [1] = deltaFitnessChanges
     */
    private double[][] getExtendedPopulationValues(List<? extends Solution<?>> population) {

        double[][] values = new double[2][];
        values[0] = new double[2];
        values[1] = new double[this.objectiveCount];

        if (BinarySolution.class.isAssignableFrom(population.get(0).getClass())) {


            BinarySet best = (BinarySet) population.get(0).getVariableValue(0);
            int numberOfBits = best.getBinarySetLength();
            double max = Double.MIN_VALUE;
            double homogeneitySum = 0.0;

            for (Solution<?> solution : population) {
                BinarySet act = (BinarySet) solution.getVariableValue(0);
                BinarySet diff = (BinarySet) act.clone();
                diff.xor(best);
                double diffBits = 0.0;
                for (int j = diff.nextSetBit(0); j >= 0; j = diff.nextSetBit(j + 1)) {
                    diffBits++;
                }
                double homogeneity = diffBits / numberOfBits;
                if (max < homogeneity)
                    max = homogeneity;
                homogeneitySum += homogeneity;
            }

            values[0][0] = (homogeneitySum / population.size());
            values[0][1] = max;
        }

        for (int i = 0; i < this.objectiveCount; i++) {
            double bestFitness = population.get(0).getObjective(i);
            double deltaFitnessSum = 0.0;

            for (Solution<?> solution : population) {

                deltaFitnessSum += (solution.getObjective(i) / bestFitness) - 1;

            }
            values[1][i] = deltaFitnessSum / population.size();
        }

        return values;
    }

    private Comparator<Solution<?>> getComparatorForObjective(int objective) {
        return Comparator.comparingDouble(s -> s.getObjective(objective));
    }

    @Override
    public void logCervisia(DeviceTypes type, Enum2DoubleMap<EAObjectives> cervisia) {
        synchronized (this.cervisiaInformation) {
            if (type == DeviceTypes.CHPPLANT) {
                this.cervisiaInformation[0][0] += cervisia.get(EAObjectives.MONEY);
                this.cervisiaInformation[0][1]++;
            } else if (type == DeviceTypes.HOTWATERSTORAGE) {
                this.cervisiaInformation[1][0] += cervisia.get(EAObjectives.MONEY);
                this.cervisiaInformation[1][1]++;
            } else if (type == DeviceTypes.DISHWASHER) {
                this.cervisiaInformation[2][0] += cervisia.get(EAObjectives.MONEY);
                this.cervisiaInformation[2][1]++;
            } else if (type == DeviceTypes.DRYER) {
                this.cervisiaInformation[3][0] += cervisia.get(EAObjectives.MONEY);
                this.cervisiaInformation[3][1]++;
            } else if (type == DeviceTypes.WASHINGMACHINE) {
                this.cervisiaInformation[4][0] += cervisia.get(EAObjectives.MONEY);
                this.cervisiaInformation[4][1]++;
            }
        }

    }
}
