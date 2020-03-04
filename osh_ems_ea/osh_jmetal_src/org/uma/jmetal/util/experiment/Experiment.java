package org.uma.jmetal.util.experiment;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Class for describing the configuration of a jMetal experiment.
 * <p>
 * Created by Antonio J. Nebro on 17/07/14.
 */
public class Experiment<S extends Solution<?>, Result extends List<S>> {
    private final String experimentName;
    private final List<ExperimentProblem<S>> problemList;
    private final String experimentBaseDirectory;
    private final String outputParetoFrontFileName;
    private final String outputParetoSetFileName;
    private final int independentRuns;
    private final List<GenericIndicator<S>> indicatorList;
    private final int numberOfCores;
    private List<ExperimentAlgorithm<S, Result>> algorithmList;
    private String referenceFrontDirectory;

    /**
     * Constructor
     */
    public Experiment(ExperimentBuilder<S, Result> builder) {
        this.experimentName = builder.getExperimentName();
        this.experimentBaseDirectory = builder.getExperimentBaseDirectory();
        this.algorithmList = builder.getAlgorithmList();
        this.problemList = builder.getProblemList();
        this.independentRuns = builder.getIndependentRuns();
        this.outputParetoFrontFileName = builder.getOutputParetoFrontFileName();
        this.outputParetoSetFileName = builder.getOutputParetoSetFileName();
        this.numberOfCores = builder.getNumberOfCores();
        this.referenceFrontDirectory = builder.getReferenceFrontDirectory();
        this.indicatorList = builder.getIndicatorList();
    }

    /* Getters */
    public String getExperimentName() {
        return this.experimentName;
    }

    public List<ExperimentAlgorithm<S, Result>> getAlgorithmList() {
        return this.algorithmList;
    }

    public void setAlgorithmList(List<ExperimentAlgorithm<S, Result>> algorithmList) {
        this.algorithmList = algorithmList;
    }

    public List<ExperimentProblem<S>> getProblemList() {
        return this.problemList;
    }

    public String getExperimentBaseDirectory() {
        return this.experimentBaseDirectory;
    }

    public String getOutputParetoFrontFileName() {
        return this.outputParetoFrontFileName;
    }

    public String getOutputParetoSetFileName() {
        return this.outputParetoSetFileName;
    }

    public int getIndependentRuns() {
        return this.independentRuns;
    }

    public int getNumberOfCores() {
        return this.numberOfCores;
    }

    public String getReferenceFrontDirectory() {
        return this.referenceFrontDirectory;
    }

    /* Setters */
    public void setReferenceFrontDirectory(String referenceFrontDirectory) {
        this.referenceFrontDirectory = referenceFrontDirectory;
    }

    public List<GenericIndicator<S>> getIndicatorList() {
        return this.indicatorList;
    }

    /**
     * The list of algorithms contain an algorithm instance per problem. This is not convenient for
     * calculating statistical data, because a same algorithm will appear many times.
     * This method remove duplicated algorithms and leave only an instance of each one.
     */
    public void removeDuplicatedAlgorithms() {
        List<ExperimentAlgorithm<S, Result>> algorithmList = new ArrayList<>();
        HashSet<String> algorithmTagList = new HashSet<>();


        this.algorithmList.removeIf(alg -> !algorithmTagList.add(alg.getAlgorithmTag()));
    }
}
