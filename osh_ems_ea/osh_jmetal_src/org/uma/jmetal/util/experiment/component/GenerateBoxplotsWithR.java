package org.uma.jmetal.util.experiment.component;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This class generates a R script that generates an eps file containing boxplots
 * <p>
 * The results are a set of R files that are written in the directory
 * {@link Experiment #getExperimentBaseDirectory()}/R. Each file is called as
 * indicatorName.Wilcoxon.R
 * <p>
 * To run the R script: Rscript indicatorName.Wilcoxon.R
 * To generate the resulting Latex file: pdflatex indicatorName.Wilcoxon.tex
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerateBoxplotsWithR<Result extends List<? extends Solution<?>>> implements ExperimentComponent {
    private static final String DEFAULT_R_DIRECTORY = "R";

    private final Experiment<?, Result> experiment;
    private int numberOfRows;
    private int numberOfColumns;
    private boolean displayNotch;

    public GenerateBoxplotsWithR(Experiment<?, Result> experimentConfiguration) {
        this.experiment = experimentConfiguration;

        this.displayNotch = false;
        this.numberOfRows = 3;
        this.numberOfColumns = 3;

        this.experiment.removeDuplicatedAlgorithms();
    }

    public GenerateBoxplotsWithR<Result> setRows(int rows) {
        this.numberOfRows = rows;

        return this;
    }

    public GenerateBoxplotsWithR<Result> setColumns(int columns) {
        this.numberOfColumns = columns;

        return this;
    }

    public GenerateBoxplotsWithR<Result> setDisplayNotch() {
        this.displayNotch = true;

        return this;
    }

    @Override
    public void run() throws IOException {
        String rDirectoryName = this.experiment.getExperimentBaseDirectory() + "/" + DEFAULT_R_DIRECTORY;
        File rOutput;
        rOutput = new File(rDirectoryName);
        if (!rOutput.exists()) {
            new File(rDirectoryName).mkdirs();
            System.out.println("Creating " + rDirectoryName + " directory");
        }
        for (GenericIndicator<? extends Solution<?>> indicator : this.experiment.getIndicatorList()) {
            String rFileName = rDirectoryName + "/" + indicator.getName() + ".Boxplot" + ".R";

            try (FileWriter os = new FileWriter(rFileName, false)) {
                os.write("postscript(\"" +
                        indicator.getName() +
                        ".Boxplot.eps\", horizontal=FALSE, onefile=FALSE, height=8, width=12, pointsize=10)" +
                        "\n");

                os.write("resultDirectory<-\"../data" + "\"" + "\n");
                os.write("qIndicator <- function(indicator, problem)" + "\n");
                os.write("{" + "\n");

                for (int i = 0; i < this.experiment.getAlgorithmList().size(); i++) {
                    String algorithmName = this.experiment.getAlgorithmList().get(i).getAlgorithmTag();
                    os.write("file" + algorithmName + "<-paste(resultDirectory, \"" + algorithmName + "\", sep=\"/\")" + "\n");
                    os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "problem, sep=\"/\")" + "\n");
                    os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "indicator, sep=\"/\")" + "\n");
                    os.write(algorithmName + "<-scan(" + "file" + algorithmName + ")" + "\n");
                    os.write("\n");
                }

                os.write("algs<-c(");
                for (int i = 0; i < this.experiment.getAlgorithmList().size() - 1; i++) {
                    os.write("\"" + this.experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\",");
                } // for
                os.write("\"" + this.experiment.getAlgorithmList().get(this.experiment.getAlgorithmList().size() - 1).getAlgorithmTag() + "\")" + "\n");

                os.write("boxplot(");
                for (int i = 0; i < this.experiment.getAlgorithmList().size(); i++) {
                    os.write(this.experiment.getAlgorithmList().get(i).getAlgorithmTag() + ",");
                } // for
                if (this.displayNotch) {
                    os.write("names=algs, notch = TRUE)" + "\n");
                } else {
                    os.write("names=algs, notch = FALSE)" + "\n");
                }
                os.write("titulo <-paste(indicator, problem, sep=\":\")" + "\n");
                os.write("title(main=titulo)" + "\n");

                os.write("}" + "\n");

                os.write("par(mfrow=c(" + this.numberOfRows + "," + this.numberOfColumns + "))" + "\n");

                os.write("indicator<-\"" + indicator.getName() + "\"" + "\n");

                for (ExperimentProblem<?> problem : this.experiment.getProblemList()) {
                    os.write("qIndicator(indicator, \"" + problem.getTag() + "\")" + "\n");
                }
            }
        }
    }
}