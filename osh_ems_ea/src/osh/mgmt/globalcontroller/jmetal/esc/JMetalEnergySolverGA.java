package osh.mgmt.globalcontroller.jmetal.esc;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import osh.configuration.oc.EAConfiguration;
import osh.core.OSHRandom;
import osh.core.exceptions.OCManagerException;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.JMetalSolver;
import osh.mgmt.globalcontroller.jmetal.SolutionWithFitness;
import osh.mgmt.globalcontroller.jmetal.builder.AlgorithmExecutor;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;
import osh.mgmt.globalcontroller.jmetal.solution.AlgorithmSolutionCollection;
import osh.mgmt.globalcontroller.jmetal.solution.SolutionComparatorFactory;
import osh.utils.costs.OptimizationCostFunction;
import osh.utils.map.MapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * New JMetalEnergySolverGA
 *
 * @author Ingo Mauser, Till Schuberth, Sebastian Kramer
 */
public class JMetalEnergySolverGA extends JMetalSolver {

    private final EAConfiguration eaConfiguration;
    private final Map<String, Object> solutionRankingParameters;
    private final String gaLogPath;

    /**
     * CONSTRUCTOR
     *
     * @param globalLogger
     * @param randomGenerator
     * @param showDebugMessages
     */
    public JMetalEnergySolverGA(
            IGlobalLogger globalLogger,
            OSHRandom randomGenerator,
            boolean showDebugMessages,
            EAConfiguration eaConfiguration,
            long timestamp,
            int STEP_SIZE,
            String logDir) {
        super(globalLogger, randomGenerator, showDebugMessages, STEP_SIZE);

        this.eaConfiguration = eaConfiguration;
        this.solutionRankingParameters = MapUtils.mapFromCPCollectionUnpacked(eaConfiguration.getSolutionRanking().getRankingParameters());
        File f = new File(logDir);
        if (!f.exists())
            f.mkdirs();

        this.gaLogPath = logDir + "/gaLog.txt";
    }


    @Override
    public SolutionWithFitness getSolutionAndFitness(
            InterdependentProblemPart<?, ?>[] problemParts,
            OCEnergySimulationCore ocESC,
            long ignoreLoadProfileBefore,
            OptimizationCostFunction costFunction,
            AlgorithmExecutor algorithmExecutor) throws FileNotFoundException {


        // DECLARATION
        SolutionDistributor distributor = new SolutionDistributor();
        distributor.gatherVariableInformation(problemParts);
        IEALogger eaLogger = algorithmExecutor.getEaLogger();

        // calculate ignoreLoadProfileAfter (Optimization Horizon)
        long ignoreLoadProfileAfter = ignoreLoadProfileBefore;
        for (InterdependentProblemPart<?, ?> ex : problemParts) {
            if (ex instanceof ControllableIPP<?, ?>) {
                ignoreLoadProfileAfter = Math.max(ex.getOptimizationHorizon(), ignoreLoadProfileAfter);
            }
        }

        // INITIALIZATION
        EMProblemEvaluator evaluator = new EMProblemEvaluator(
                problemParts,
                ocESC,
                distributor,
                ignoreLoadProfileBefore,
                ignoreLoadProfileAfter,
                costFunction,
                eaLogger,
                this.STEP_SIZE,
                this.eaConfiguration.getEaObjectives());

        algorithmExecutor.updateRandomGenerator(this.randomGenerator);

        PrintWriter pw = new PrintWriter(new FileOutputStream(
                new File(this.gaLogPath),
                true));

        eaLogger.attachWriter(pw);
        eaLogger.setTimestamp(ignoreLoadProfileBefore);

        try {
            AlgorithmSolutionCollection result = algorithmExecutor.runAlgorithms(distributor, evaluator);

            List<AlgorithmSolutionCollection.SolutionTypeUnion<?>> nonDomUnion = result.getNonDominatedSolutionList();
            FrontNormalizer normalizer = new FrontNormalizer(nonDomUnion);
            List<? extends Solution<?>> normNonDomUnion = normalizer.normalize(nonDomUnion);

            int[] id = {0};
            normNonDomUnion.forEach(s -> s.setAttribute("id", id[0]++));

            Comparator<Solution<?>> solutionComp = SolutionComparatorFactory.getComparator
                    (this.eaConfiguration.getSolutionRanking
                            ().getType(), this.solutionRankingParameters);

            normNonDomUnion.sort(solutionComp);

            for (int i = 0; i < normNonDomUnion.size(); i++) {
                int idx = (int) normNonDomUnion.get(i).getAttribute("id");
                nonDomUnion.get(idx).setAttribute("id", i);
            }
            nonDomUnion.sort(Comparator.comparing(item -> (int) item.getAttribute("id")));

            Solution<?> bestSolution = nonDomUnion.get(0).getSolution();

            //TODO: generate log objects for information about the results according to the new logging
            // configurations in EAConfiguration

            eaLogger.logEnd(bestSolution);
            eaLogger.detachWriter();

            return new SolutionWithFitness(bestSolution, bestSolution.getObjectives());

        } catch (OCManagerException | InterruptedException e) {
            e.printStackTrace();
            this.logger.logError(e);
            return null;
        }
    }
}

