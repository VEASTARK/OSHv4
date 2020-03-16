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

    public EALogger(
            IGlobalLogger globalLogger,
            boolean log,
            boolean logExtended,
            int logXthGeneration,
            int logExtendedGenerations,
            boolean logOverallEA) {

        init(globalLogger, log, logExtended, logXthGeneration, logExtendedGenerations,
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

        fitnessChange = new double[logExtendedGenerations];
        fitnessSpread = new double[logExtendedGenerations];
        homogeneity = new double[logExtendedGenerations];
    }

    @Override
    public void logStart(Algorithm<?> usedAlgorithm) {
        if (log) {
            String logMessage = "===    New Optimization, using " + usedAlgorithm.getDescription() + "    ===";
            logger.logDebug(logMessage);
            this.bestFirstFitness = Double.NaN;
        }
    }

    @Override
    public void logEnd(Solution<?> bestSolution) {
        if (log) {
            StringBuilder logMessage = new StringBuilder("===    Finished Optimization, final Fitness: ");
            logMessage.append(" ").append(bestSolution.getObjective(0)).append(" ");
            logMessage.append("    ===");
            logger.logDebug(logMessage.toString());
        }
        if (logOverallEA) {
            optimizationCounter++;
        }
    }

    @Override
    public void logEnd(List<? extends Solution<?>> bestSolutions) {
        if (log) {
            StringBuilder logMessage = new StringBuilder("***    final Fitness of Candidates: ");
            for (Solution<?> s : bestSolutions) {
                logMessage.append("\n Candidate: ");
                logMessage.append(" ").append(s.getObjective(0)).append(" ");
            }

            logMessage.append("\n    ***");
            logger.logDebug(logMessage.toString());
        }
        if (logOverallEA) {
            optimizationCounter++;
        }
    }

    @Override
    public void logPopulation(List<? extends Solution<?>> population, int generation) {
        if (log) {

            String logMessage = "";

            if (generation % logXthGeneration == 0) {

                population.sort(comparator);

                double bestFitness = population.get(0).getObjective(0);

                if (Double.isNaN(this.bestFirstFitness)) {
                    this.bestFirstFitness = bestFitness;
                }

                logMessage = "[" + generation + "] -- BestFitness: " + bestFitness;

            } else {

                if (Double.isNaN(this.bestFirstFitness)) {
                    population.sort(comparator);
                    this.bestFirstFitness = population.get(0).getObjective(0);
                }
            }

            if ((logExtended && generation % logXthGeneration == 0)
                    || (logOverallEA && generation < logExtendedGenerations)) {
                double[] popValues = getExtendedPopulationValues(population);

                if ((logOverallEA && generation < logExtendedGenerations)) {

                    homogeneity[generation] += popValues[0];
                    population.sort(comparator);
                    fitnessChange[generation] += population.get(0).getObjective(0) / bestFirstFitness;
                    fitnessSpread[generation] += popValues[1];
                }

                if ((logExtended && generation % logXthGeneration == 0)) {
                    logMessage += " -- Homogeneity mean: " + popValues[0] + " -- max: " + popValues[1]
                            + " -- deltaFitnessSpread: " + popValues[2];
                }
            }

            if (!logMessage.isEmpty()) {
                logger.logDebug(logMessage);
            }

        }
        if (logOverallEA) {
            generationsUsed++;
        }
    }

    @Override
    public void logAdditional(String message) {
        if (log) {
            logger.logDebug(message);
        }
    }

    @Override
    public void shutdown() {
        if (log) {
            generationsUsed /= optimizationCounter;

            fitnessChange = Arrays.stream(fitnessChange).map(d -> d /= optimizationCounter).toArray();
            fitnessSpread = Arrays.stream(fitnessSpread).map(d -> d /= optimizationCounter).toArray();

            homogeneity = Arrays.stream(homogeneity).map(d -> d /= optimizationCounter).toArray();

            double[] cervisiaResults = new double[5];
            for (int i = 0; i < cervisiaResults.length; i++) {
                if (cervisiaInformation[i][1] != 0) {
                    cervisiaResults[i] = cervisiaInformation[i][0] / cervisiaInformation[i][1];
                }
            }

            DatabaseLoggerThread.enqueueGA(generationsUsed, fitnessChange, fitnessSpread, homogeneity, optimizationCounter, cervisiaResults);
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
            if (type.equals(DeviceTypes.CHPPLANT)) {
                this.cervisiaInformation[0][0] += cervisia;
                this.cervisiaInformation[0][1]++;
            } else if (type.equals(DeviceTypes.HOTWATERSTORAGE)) {
                this.cervisiaInformation[1][0] += cervisia;
                this.cervisiaInformation[1][1]++;
            } else if (type.equals(DeviceTypes.DISHWASHER)) {
                this.cervisiaInformation[2][0] += cervisia;
                this.cervisiaInformation[2][1]++;
            } else if (type.equals(DeviceTypes.DRYER)) {
                this.cervisiaInformation[3][0] += cervisia;
                this.cervisiaInformation[3][1]++;
            } else if (type.equals(DeviceTypes.WASHINGMACHINE)) {
                this.cervisiaInformation[4][0] += cervisia;
                this.cervisiaInformation[4][1]++;
            }
        }

    }
}
