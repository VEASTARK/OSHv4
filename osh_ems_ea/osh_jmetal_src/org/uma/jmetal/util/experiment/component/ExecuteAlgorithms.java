package org.uma.jmetal.util.experiment.component;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;

import java.io.File;
import java.util.List;

/**
 * This class executes the algorithms the have been configured with a instance of class
 * {@link Experiment}. Java 8 parallel streams are used to run the algorithms in parallel.
 * <p>
 * The result of the execution is a pair of files FUNrunId.tsv and VARrunID.tsv per experiment,
 * which are stored in the directory
 * {@link Experiment #getExperimentBaseDirectory()}/algorithmName/problemName.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ExecuteAlgorithms<S extends Solution<?>, Result extends List<S>> implements ExperimentComponent {
    private final Experiment<S, Result> experiment;

    /**
     * Constructor
     */
    public ExecuteAlgorithms(Experiment<S, Result> configuration) {
        this.experiment = configuration;
    }

    @Override
    public void run() {
        JMetalLogger.logger.info("ExecuteAlgorithms: Preparing output directory");
        this.prepareOutputDirectory();

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                "" + this.experiment.getNumberOfCores());

        this.experiment.getAlgorithmList()
                .parallelStream()
                .forEach(algorithm -> algorithm.runAlgorithm(this.experiment));
    }


    private void prepareOutputDirectory() {
        if (this.experimentDirectoryDoesNotExist()) {
            this.createExperimentDirectory();
        }
    }

    private boolean experimentDirectoryDoesNotExist() {
        boolean result;
        File experimentDirectory;

        experimentDirectory = new File(this.experiment.getExperimentBaseDirectory());
        return !experimentDirectory.exists() || !experimentDirectory.isDirectory();
    }

    private void createExperimentDirectory() {
        File experimentDirectory;
        experimentDirectory = new File(this.experiment.getExperimentBaseDirectory());

        if (experimentDirectory.exists()) {
            experimentDirectory.delete();
        }

        boolean result;
        result = new File(this.experiment.getExperimentBaseDirectory()).mkdirs();
        if (!result) {
            throw new JMetalException("Error creating experiment directory: " +
                    this.experiment.getExperimentBaseDirectory());
        }
    }
}