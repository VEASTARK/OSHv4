package osh.mgmt.globalcontroller.jmetal.logging;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.simulation.DatabaseLoggerThread;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a simple logger implementing the EA logger interface {@link IEALogger}.
 *
 * @author Sebastian Kramer
 */
public class EALogger implements IEALogger {

    private boolean log;
    private boolean logExtended;
    private int logXthGeneration;
    private IGlobalLogger logger;
    private boolean logOverallEA;
    private int optimizationCounter;
    private double generationsUsed;
    private int logExtendedGenerations;
    private double[] fitnessChange;
    private double[] fitnessSpread;
    private double[] homogeneity;
    private double bestFirstFitness = Double.NaN;
    private final double[][] cervisiaInformation = new double[5][2];
    private final Comparator<Solution<?>> comparator= new ObjectiveComparator<>(0);

    private PrintWriter additionalWriter;
    private long timestamp;

    public EALogger(
            IGlobalLogger globalLogger,
            boolean log,
            boolean logExtended,
            int logXthGeneration,
            int logExtendedGenerations,
            boolean logOverallEA) {

        this.init(globalLogger, log, logExtended, logXthGeneration, logExtendedGenerations,
                logOverallEA);
    }

    private void init(
            IGlobalLogger globalLogger,
            boolean log,
            boolean logExtended,
            int logXthGeneration,
            int logExtendedGenerations,
            boolean logOverallEA) {

        this.log = log;
        this.logExtended = logExtended;
        this.logXthGeneration = logXthGeneration;
        this.logExtendedGenerations = logExtendedGenerations;
        this.logger = globalLogger;
        this.logOverallEA = logOverallEA;

        this.fitnessChange = new double[logExtendedGenerations];
        this.fitnessSpread = new double[logExtendedGenerations];
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

    @Override
    public void logStart(Algorithm<?> usedAlgorithm) {
        if (this.log) {
            String logMessage = "===    New Optimization, using " + usedAlgorithm.getDescription() + "    ===";
            this.logger.logDebug(logMessage);
            if (this.additionalWriter != null) {
                this.additionalWriter.println("[" + this.timestamp + "] " + logMessage);
            }
            this.bestFirstFitness = Double.NaN;
        }
    }

    @Override
    public void logEnd(Solution<?> bestSolution) {
        if (this.log) {
            StringBuilder logMessage = new StringBuilder("===    Finished Optimization, final Fitness: ");
            logMessage.append(" ").append(bestSolution.getObjective(0)).append(" ");
            logMessage.append("    ===");
            this.logger.logDebug(logMessage.toString());
            if (this.additionalWriter != null) {
                this.additionalWriter.println(logMessage.toString());
            }
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

                population.sort(this.comparator);

                double bestFitness = population.get(0).getObjective(0);

                if (Double.isNaN(this.bestFirstFitness)) {
                    this.bestFirstFitness = bestFitness;
                }

                logMessage = "[" + generation + "] -- BestFitness: " + bestFitness;

            } else {

                if (Double.isNaN(this.bestFirstFitness)) {
                    population.sort(this.comparator);
                    this.bestFirstFitness = population.get(0).getObjective(0);
                }
            }

            if ((this.logExtended && generation % this.logXthGeneration == 0)
                    || (this.logOverallEA && generation < this.logExtendedGenerations)) {
                double[] popValues = this.getExtendedPopulationValues(population);

                if ((this.logOverallEA && generation < this.logExtendedGenerations)) {

                    this.homogeneity[generation] += popValues[0];
                    population.sort(this.comparator);
                    this.fitnessChange[generation] += population.get(0).getObjective(0) / this.bestFirstFitness;
                    this.fitnessSpread[generation] += popValues[1];
                }

                if ((this.logExtended && generation % this.logXthGeneration == 0)) {
                    logMessage += " -- Homogeneity mean: " + popValues[0] + " -- max: " + popValues[1]
                            + " -- deltaFitnessSpread: " + popValues[2];
                }
            }

            if (!logMessage.isEmpty()) {
                this.logger.logDebug(logMessage);
                if (this.additionalWriter != null) {
                    this.additionalWriter.println(logMessage);
                }
            }

        }
        if (this.logOverallEA) {
            this.generationsUsed++;
        }
    }

    @Override
    public void logAdditional(String message) {
        if (this.log) {
            this.logger.logDebug(message);
            if (this.additionalWriter != null) {
                this.additionalWriter.println(message);
            }
        }
    }

    @Override
    public void shutdown() {
        if (this.log && DatabaseLoggerThread.isIsLogToDatabase()) {
            this.generationsUsed /= this.optimizationCounter;

            this.fitnessChange = Arrays.stream(this.fitnessChange).map(d -> d / this.optimizationCounter).toArray();
            this.fitnessSpread = Arrays.stream(this.fitnessSpread).map(d -> d / this.optimizationCounter).toArray();

            this.homogeneity = Arrays.stream(this.homogeneity).map(d -> d / this.optimizationCounter).toArray();

            double[] cervisiaResults = new double[5];
            for (int i = 0; i < cervisiaResults.length; i++) {
                if (this.cervisiaInformation[i][1] != 0) {
                    cervisiaResults[i] = this.cervisiaInformation[i][0] / this.cervisiaInformation[i][1];
                }
            }

            DatabaseLoggerThread.enqueueGA(this.generationsUsed, this.fitnessChange, this.fitnessSpread, this.homogeneity, this.optimizationCounter, cervisiaResults);
        }
    }

    /* [0] = homogeneity
     * [1] = maxDifference
     * [2] = deltaFitnessChanges
     */
    private double[] getExtendedPopulationValues(List<? extends Solution<?>> population) {

        double[] values = new double[3];

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

            values[0] = (homogeneitySum / population.size());
            values[1] = max;

            double bestFitness = population.get(0).getObjective(0);
            double deltaFitnessSum = 0.0;

            for (Solution<?> solution : population) {

                deltaFitnessSum += (solution.getObjective(0) / bestFitness) - 1;

            }
            values[2] = deltaFitnessSum / population.size();


        } else if (DoubleSolution.class.isAssignableFrom(population.get(0).getClass())) {

            double bestFitness = population.get(0).getObjective(0);
            double deltaFitnessSum = 0.0;

            for (Solution<?> solution : population) {

                deltaFitnessSum += (solution.getObjective(0) / bestFitness) - 1;

            }
            values[2] = deltaFitnessSum / population.size();
        }

        return values;
    }

    @Override
    public void logCervisia(DeviceTypes type, double cervisia) {
        synchronized (this.cervisiaInformation) {
            if (type == DeviceTypes.CHPPLANT) {
                this.cervisiaInformation[0][0] += cervisia;
                this.cervisiaInformation[0][1]++;
            } else if (type == DeviceTypes.HOTWATERSTORAGE) {
                this.cervisiaInformation[1][0] += cervisia;
                this.cervisiaInformation[1][1]++;
            } else if (type == DeviceTypes.DISHWASHER) {
                this.cervisiaInformation[2][0] += cervisia;
                this.cervisiaInformation[2][1]++;
            } else if (type == DeviceTypes.DRYER) {
                this.cervisiaInformation[3][0] += cervisia;
                this.cervisiaInformation[3][1]++;
            } else if (type == DeviceTypes.WASHINGMACHINE) {
                this.cervisiaInformation[4][0] += cervisia;
                this.cervisiaInformation[4][1]++;
            }
        }

    }
}
