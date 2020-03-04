package org.uma.jmetal.util.experiment.util;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.File;
import java.util.List;

/**
 * Class defining tasks for the execution of algorithms in parallel.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ExperimentAlgorithm<S extends Solution<?>, Result extends List<S>> {
    private final Algorithm<Result> algorithm;
    private final String algorithmTag;
    private final String problemTag;
    private final String referenceParetoFront;
    private final int runId;

    /**
     * Constructor
     */
    public ExperimentAlgorithm(
            Algorithm<Result> algorithm,
            String algorithmTag,
            ExperimentProblem<S> problem,
            int runId) {
        this.algorithm = algorithm;
        this.algorithmTag = algorithmTag;
        this.problemTag = problem.getTag();
        this.referenceParetoFront = problem.getReferenceFront();
        this.runId = runId;
    }

    public ExperimentAlgorithm(
            Algorithm<Result> algorithm,
            ExperimentProblem<S> problem,
            int runId) {

        this(algorithm, algorithm.getName(), problem, runId);

    }

    public void runAlgorithm(Experiment<?, ?> experimentData) {
        String outputDirectoryName = experimentData.getExperimentBaseDirectory()
                + "/data/"
                + this.algorithmTag
                + "/"
                + this.problemTag;

        File outputDirectory = new File(outputDirectoryName);
        if (!outputDirectory.exists()) {
            boolean result = new File(outputDirectoryName).mkdirs();
            if (result) {
                JMetalLogger.logger.info("Creating " + outputDirectoryName);
            } else {
                JMetalLogger.logger.severe("Creating " + outputDirectoryName + " failed");
            }
        }

        String funFile = outputDirectoryName + "/FUN" + this.runId + ".tsv";
        String varFile = outputDirectoryName + "/VAR" + this.runId + ".tsv";
        JMetalLogger.logger.info(
                " Running algorithm: " + this.algorithmTag +
                        ", problem: " + this.problemTag +
                        ", run: " + this.runId +
                        ", funFile: " + funFile);


        this.algorithm.run();
        Result population = this.algorithm.getResult();

        new SolutionListOutput(population)
                .setSeparator("\t")
                .setVarFileOutputContext(new DefaultFileOutputContext(varFile))
                .setFunFileOutputContext(new DefaultFileOutputContext(funFile))
                .print();
    }

    public Algorithm<Result> getAlgorithm() {
        return this.algorithm;
    }

    public String getAlgorithmTag() {
        return this.algorithmTag;
    }

    public String getProblemTag() {
        return this.problemTag;
    }

    public String getReferenceParetoFront() {
        return this.referenceParetoFront;
    }

    public int getRunId() {
        return this.runId;
    }
}
