package org.uma.jmetal.util.fileoutput;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SolutionListOutput {
    private final String varFileName = "VAR";
    private final String funFileName = "FUN";
    private final List<? extends Solution<?>> solutionList;
    private FileOutputContext varFileContext;
    private FileOutputContext funFileContext;
    private String separator = "\t";
    private List<Boolean> isObjectiveToBeMinimized;

    public SolutionListOutput(List<? extends Solution<?>> solutionList) {
        this.varFileContext = new DefaultFileOutputContext(this.varFileName);
        this.funFileContext = new DefaultFileOutputContext(this.funFileName);
        this.varFileContext.setSeparator(this.separator);
        this.funFileContext.setSeparator(this.separator);
        this.solutionList = solutionList;
        this.isObjectiveToBeMinimized = null;
    }

    public SolutionListOutput setVarFileOutputContext(FileOutputContext fileContext) {
        this.varFileContext = fileContext;

        return this;
    }

    public SolutionListOutput setFunFileOutputContext(FileOutputContext fileContext) {
        this.funFileContext = fileContext;

        return this;
    }

    public SolutionListOutput setObjectiveMinimizingObjectiveList(List<Boolean> isObjectiveToBeMinimized) {
        this.isObjectiveToBeMinimized = isObjectiveToBeMinimized;

        return this;
    }

    public SolutionListOutput setSeparator(String separator) {
        this.separator = separator;
        this.varFileContext.setSeparator(this.separator);
        this.funFileContext.setSeparator(this.separator);

        return this;
    }

    public void print() {
        if (this.isObjectiveToBeMinimized == null) {
            this.printObjectivesToFile(this.funFileContext, this.solutionList);
        } else {
            this.printObjectivesToFile(this.funFileContext, this.solutionList, this.isObjectiveToBeMinimized);
        }
        this.printVariablesToFile(this.varFileContext, this.solutionList);
    }

    public void printVariablesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (!solutionList.isEmpty()) {
                int numberOfVariables = solutionList.get(0).getNumberOfVariables();
                for (Solution<?> solution : solutionList) {
                    for (int j = 0; j < numberOfVariables; j++) {
                        bufferedWriter.write(solution.getVariableValueString(j) + context.getSeparator());
                    }
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error writing data ", e);
        }

    }

    public void printObjectivesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (!solutionList.isEmpty()) {
                int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
                for (Solution<?> solution : solutionList) {
                    for (int j = 0; j < numberOfObjectives; j++) {
                        bufferedWriter.write(solution.getObjective(j) + context.getSeparator());
                    }
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error printing objecives to file: ", e);
        }
    }

    public void printObjectivesToFile(FileOutputContext context,
                                      List<? extends Solution<?>> solutionList,
                                      List<Boolean> minimizeObjective) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (!solutionList.isEmpty()) {
                int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
                if (numberOfObjectives != minimizeObjective.size()) {
                    throw new JMetalException("The size of list minimizeObjective is not correct: " + minimizeObjective.size());
                }
                for (Solution<?> solution : solutionList) {
                    for (int j = 0; j < numberOfObjectives; j++) {
                        if (minimizeObjective.get(j)) {
                            bufferedWriter.write(solution.getObjective(j) + context.getSeparator());
                        } else {
                            bufferedWriter.write(-1.0 * solution.getObjective(j) + context.getSeparator());
                        }
                    }
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error printing objecives to file: ", e);
        }
    }

    /*
     * Wrappers for printing with default configuration
     */
    public void printObjectivesToFile(String fileName) {
        this.printObjectivesToFile(new DefaultFileOutputContext(fileName), this.solutionList);
    }

    public void printObjectivesToFile(String fileName, List<Boolean> minimizeObjective) {
        this.printObjectivesToFile(new DefaultFileOutputContext(fileName), this.solutionList, minimizeObjective);
    }

    public void printVariablesToFile(String fileName) {
        this.printVariablesToFile(new DefaultFileOutputContext(fileName), this.solutionList);
    }

}
