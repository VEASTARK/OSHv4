package org.uma.jmetal.util.experiment;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for class {@link Experiment}
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ExperimentBuilder<S extends Solution<?>, Result extends List<S>> {
    private final String experimentName;
    private List<ExperimentAlgorithm<S, Result>> algorithmList;
    private List<ExperimentProblem<S>> problemList;
    private String referenceFrontDirectory;
    private String experimentBaseDirectory;
    private String outputParetoFrontFileName;
    private String outputParetoSetFileName;
    private int independentRuns;

    private List<GenericIndicator<S>> indicatorList;

    private int numberOfCores;

    public ExperimentBuilder(String experimentName) {
        this.experimentName = experimentName;
        this.independentRuns = 1;
        this.numberOfCores = 1;
        this.referenceFrontDirectory = null;
    }

    public Experiment<S, Result> build() {
        return new Experiment<>(this);
    }

    /* Getters */
    public String getExperimentName() {
        return this.experimentName;
    }

    public List<ExperimentAlgorithm<S, Result>> getAlgorithmList() {
        return this.algorithmList;
    }

    public ExperimentBuilder<S, Result> setAlgorithmList(List<ExperimentAlgorithm<S, Result>> algorithmList) {
        this.algorithmList = new ArrayList<>(algorithmList);

        return this;
    }

    public List<ExperimentProblem<S>> getProblemList() {
        return this.problemList;
    }

    public ExperimentBuilder<S, Result> setProblemList(List<ExperimentProblem<S>> problemList) {
        this.problemList = problemList;

        return this;
    }

    public String getExperimentBaseDirectory() {
        return this.experimentBaseDirectory;
    }

    public ExperimentBuilder<S, Result> setExperimentBaseDirectory(String experimentBaseDirectory) {
        this.experimentBaseDirectory = experimentBaseDirectory + "/" + this.experimentName;

        return this;
    }

    public String getOutputParetoFrontFileName() {
        return this.outputParetoFrontFileName;
    }

    public ExperimentBuilder<S, Result> setOutputParetoFrontFileName(String outputParetoFrontFileName) {
        this.outputParetoFrontFileName = outputParetoFrontFileName;

        return this;
    }

    public String getOutputParetoSetFileName() {
        return this.outputParetoSetFileName;
    }

    public ExperimentBuilder<S, Result> setOutputParetoSetFileName(String outputParetoSetFileName) {
        this.outputParetoSetFileName = outputParetoSetFileName;

        return this;
    }

    public int getIndependentRuns() {
        return this.independentRuns;
    }

    public ExperimentBuilder<S, Result> setIndependentRuns(int independentRuns) {
        this.independentRuns = independentRuns;

        return this;
    }

    public int getNumberOfCores() {
        return this.numberOfCores;
    }

    public ExperimentBuilder<S, Result> setNumberOfCores(int numberOfCores) {
        this.numberOfCores = numberOfCores;

        return this;
    }

    public String getReferenceFrontDirectory() {
        return this.referenceFrontDirectory;
    }

    public ExperimentBuilder<S, Result> setReferenceFrontDirectory(String referenceFrontDirectory) {
        this.referenceFrontDirectory = referenceFrontDirectory;

        return this;
    }

    public List<GenericIndicator<S>> getIndicatorList() {
        return this.indicatorList;
    }

    public ExperimentBuilder<S, Result> setIndicatorList(
            List<GenericIndicator<S>> indicatorList) {
        this.indicatorList = indicatorList;

        return this;
    }
}
