package jmetal.metaheuristics.stoppingRule;

import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import osh.utils.string.ParameterConstants;

import java.util.Map;

public class DeltaFitnessStoppingRule extends StoppingRule {

    private double minDeltaFitnessPercent;
    private int maxGenerationsDeltaFitnessViolated;

    private Double lastGenerationBestFitness;
    private int generationsDeltaFitnessViolated;

    public DeltaFitnessStoppingRule(Map<String, Object> parameters) throws JMException {
        super(parameters);

        if (this._parameters.get(ParameterConstants.EA.minDeltaFitnessPercent) != null)
            this.minDeltaFitnessPercent = (double) parameters.get(ParameterConstants.EA.minDeltaFitnessPercent);
        else {
            Configuration.logger_.severe("EvaluationsStoppingRule no minDeltaFitnessPerc in parameters.");
            throw new JMException("no minDeltaFitnessPerc in parameters");
        }

        if (this._parameters.get(ParameterConstants.EA.maxGenerationsDeltaFitnessViolated) != null)
            this.maxGenerationsDeltaFitnessViolated = (int) parameters.get(ParameterConstants.EA.maxGenerationsDeltaFitnessViolated);
        else {
            Configuration.logger_.severe("EvaluationsStoppingRule no maxGenerationsDeltaFitnessViolated in parameters.");
            throw new JMException("no minGenerations in parameters");
        }
    }

    /**
     * checks if the optimisation should stop
     * <p>
     * Optimisation will stop if:
     * - delta Fitness change between generations was smaller then required for the required generations
     * <p>
     * It is assumed that the optimisation cannot return worse fitness values for generation n+1 then for generation n
     */
    @Override
    public boolean checkIfStop(Problem problem, int generation, SolutionSet currentSortedSolutions) {
        if (this.lastGenerationBestFitness == null) {
            this.lastGenerationBestFitness = currentSortedSolutions.get(0).getObjective(0);
            return false;
        }
        double thisGenerationBestFitness = currentSortedSolutions.get(0).getObjective(0);

        double deltaFitness;

        deltaFitness = Math.abs((Math.abs(thisGenerationBestFitness) - Math.abs(this.lastGenerationBestFitness)) / Math.abs(this.lastGenerationBestFitness));


        if (deltaFitness >= this.minDeltaFitnessPercent) {
            this.generationsDeltaFitnessViolated = 0;
            this.lastGenerationBestFitness = thisGenerationBestFitness;
            return false;
        } else {
            this.generationsDeltaFitnessViolated++;
            if (this.generationsDeltaFitnessViolated < this.maxGenerationsDeltaFitnessViolated)
                return false;
            else {
                this._msg = "Optimisation stopped after violating minDeltaFitness for " + this.generationsDeltaFitnessViolated + " generations.";
                return true;
            }
        }
    }
}
