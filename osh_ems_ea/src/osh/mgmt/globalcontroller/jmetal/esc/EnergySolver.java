package osh.mgmt.globalcontroller.jmetal.esc;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import osh.configuration.oc.EAConfiguration;
import osh.configuration.system.DeviceTypes;
import osh.core.OSHRandom;
import osh.core.exceptions.OCManagerException;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.TemperaturePrediction;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.power.ErsatzACLoadProfile;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OptimizationEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.builder.AlgorithmExecutor;
import osh.mgmt.globalcontroller.jmetal.builder.EAScheduleResult;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;
import osh.mgmt.globalcontroller.jmetal.solution.AlgorithmSolutionCollection;
import osh.mgmt.globalcontroller.jmetal.solution.SolutionComparatorFactory;
import osh.utils.costs.OptimizationCostFunction;
import osh.utils.map.MapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Runs the configured algorithms on the optimization problem and returns the best results, as defined by the
 * configured solution ranking mechanism.
 *
 * @author Sebastian Kramer
 */
public class EnergySolver {

    private final IGlobalLogger globalLogger;
    private final EAConfiguration eaConfiguration;
    private final Comparator<Solution<?>> solutionRanking;
    private final String gaLogPath;
    private final int stepSize;

    /**
     * Constructs this solver with the given configuration values.
     *
     * @param globalLogger the global logger
     * @param eaConfiguration the configuration of the ea
     * @param stepSize the step-size to use inside the optimization
     * @param logDir the logging directory for the ea-output
     */
    public EnergySolver(
            IGlobalLogger globalLogger,
            EAConfiguration eaConfiguration,
            int stepSize,
            String logDir) {

        this.globalLogger = globalLogger;
        this.eaConfiguration = eaConfiguration;
        this.stepSize = stepSize;
        this.solutionRanking = SolutionComparatorFactory.getComparator(this.eaConfiguration.getSolutionRanking().getType(),
                        MapUtils.mapFromCPCollectionUnpacked(eaConfiguration.getSolutionRanking().getRankingParameters()));

        File f = new File(logDir);
        if (!f.exists())
            f.mkdirs();

        this.gaLogPath = logDir + "/gaLog.txt";
    }

    /**
     * Runs the ea algorithms on the given problem and returns the best result.
     *
     * @param problemParts the problem-parts of the problem
     * @param optimizationESC the energy-simulation-core for the optimization
     * @param optimizationStartPoint the starting point (in time) of the optimization
     * @param costFunction the cost function for the calculation
     * @param algorithmExecutor the algorithm executor to use
     * @param logExtensive if extensive log information should be produced (GUI, etc.)
     *
     * @return the best found solution in addition to (optional) logging information
     *
     * @throws FileNotFoundException if the file for the ea-log can't be found
     */
    public EAScheduleResult getSolution(
            InterdependentProblemPart<?, ?>[] problemParts,
            OptimizationEnergySimulationCore optimizationESC,
            long optimizationStartPoint,
            OptimizationCostFunction costFunction,
            AlgorithmExecutor algorithmExecutor,
            boolean logExtensive) throws FileNotFoundException {

        SolutionDistributor distributor = new SolutionDistributor();
        distributor.gatherVariableInformation(problemParts);
        IEALogger eaLogger = algorithmExecutor.getEaLogger();

        // calculate optimizationEndPoint
        long optimizationEndPoint = optimizationStartPoint;
        for (InterdependentProblemPart<?, ?> ex : problemParts) {
            if (ex instanceof ControllableIPP<?, ?>) {
                optimizationEndPoint = Math.max(ex.getOptimizationHorizon(), optimizationEndPoint);
            }
        }

        EMProblemEvaluator evaluator = new EMProblemEvaluator(
                problemParts,
                optimizationESC,
                distributor,
                optimizationStartPoint,
                optimizationEndPoint,
                costFunction,
                eaLogger,
                this.stepSize,
                this.eaConfiguration.getEaObjectives());

        PrintWriter pw = new PrintWriter(new FileOutputStream(
                new File(this.gaLogPath),
                true));

        eaLogger.attachWriter(pw);
        eaLogger.setTimestamp(optimizationStartPoint);

        try {
            AlgorithmSolutionCollection result = algorithmExecutor.runAlgorithms(distributor, evaluator);

            List<AlgorithmSolutionCollection.SolutionTypeUnion<?>> nonDomUnion = result.getNonDominatedSolutionList();
            FrontNormalizer normalizer = new FrontNormalizer(nonDomUnion);
            List<? extends Solution<?>> normNonDomUnion = normalizer.normalize(nonDomUnion);

            int[] id = {0};
            normNonDomUnion.forEach(s -> s.setAttribute("id", id[0]++));

            normNonDomUnion.sort(this.solutionRanking);

            for (int i = 0; i < normNonDomUnion.size(); i++) {
                int idx = (int) normNonDomUnion.get(i).getAttribute("id");
                nonDomUnion.get(idx).setAttribute("id", i);
            }
            nonDomUnion.sort(Comparator.comparing(item -> (int) item.getAttribute("id")));

            Solution<?> bestSolution = nonDomUnion.get(0).getSolution();

            //TODO: generate log objects for information about the results according to the new logging
            // configurations in EAConfiguration
            eaLogger.logEnd(bestSolution, nonDomUnion.get(0).getAlgorithmType());
            eaLogger.detachWriter();

            return this.constructDebuggingObjects(evaluator, bestSolution, optimizationStartPoint, optimizationEndPoint,
                    logExtensive, distributor, problemParts);

        } catch (OCManagerException | InterruptedException e) {
            e.printStackTrace();
            this.globalLogger.logError(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private EAScheduleResult constructDebuggingObjects(EMProblemEvaluator evaluator, Solution<?> solution,
                                                       long optimizationStartPoint, long optimizationEndPoint,
                                                       boolean extensiveLogging, SolutionDistributor distributor,
                                                       InterdependentProblemPart<?, ?>[] problemParts) {

        int maxLength = (int) Math.ceil((double) (optimizationEndPoint - optimizationStartPoint) / this.stepSize) + 2;
        ErsatzACLoadProfile ersatzProfile = new ErsatzACLoadProfile(maxLength);

        evaluator.evaluateFinalTime(solution, extensiveLogging, ersatzProfile);
        distributor.distributeSolution(solution, problemParts);

        TreeMap<Long, Double> predictedHotWaterTankTemperature = new TreeMap<>();
        TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<>();
        TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<>();
        List<Schedule> schedules = new ArrayList<>();

        if (extensiveLogging) {
            for (InterdependentProblemPart<?, ?> part : problemParts) {
                schedules.add(part.getFinalInterdependentSchedule());

                //extract prediction about tank temperatures of the hot-water tank
                if (part.getDeviceType() == DeviceTypes.HOTWATERSTORAGE) {
                    EAPredictionCommandExchange<TemperaturePrediction> prediction = (EAPredictionCommandExchange<TemperaturePrediction>) part.transformToFinalInterdependentPrediction(
                            part.getUUID(),
                            part.getUUID(),
                            null);

                    predictedHotWaterTankTemperature = prediction.getPrediction().getTemperatureStates();
                }

                //extract information about hot-water demand and supply
                if (part.getAllOutputCommodities().contains(Commodity.DOMESTICHOTWATERPOWER)
                        || part.getAllOutputCommodities().contains(Commodity.HEATINGHOTWATERPOWER)) {
                    SparseLoadProfile loadProfile = part.getLoadProfile();

                    for (long t = optimizationStartPoint; t < optimizationEndPoint + this.stepSize; t += this.stepSize) {
                        int domLoad = loadProfile.getLoadAt(Commodity.DOMESTICHOTWATERPOWER, t);
                        int heatLoad = loadProfile.getLoadAt(Commodity.HEATINGHOTWATERPOWER, t);

                        predictedHotWaterDemand.putIfAbsent(t, 0.0);
                        predictedHotWaterSupply.putIfAbsent(t, 0.0);

                        if (domLoad > 0) {
                            predictedHotWaterDemand.compute(t, (k, v) -> v == null ? domLoad : v + domLoad);
                        } else if (domLoad < 0) {
                            predictedHotWaterSupply.compute(t, (k, v) -> v == null ? domLoad : v + domLoad);
                        }

                        if (heatLoad > 0) {
                            predictedHotWaterDemand.compute(t, (k, v) -> v == null ? heatLoad : v + heatLoad);
                        } else if (heatLoad < 0) {
                            predictedHotWaterSupply.compute(t, (k, v) -> v == null ? heatLoad : v + heatLoad);
                        }
                    }
                }
            }
        }

        return new EAScheduleResult(predictedHotWaterTankTemperature, predictedHotWaterDemand, predictedHotWaterSupply,
                schedules, AncillaryCommodityLoadProfile.convertFromErsatzProfile(ersatzProfile), solution);

    }
}

