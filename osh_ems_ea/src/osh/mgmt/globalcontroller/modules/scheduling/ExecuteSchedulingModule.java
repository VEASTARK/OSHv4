package osh.mgmt.globalcontroller.modules.scheduling;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.uma.jmetal.solution.Solution;
import osh.core.OSHRandom;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.TemperaturePrediction;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;
import osh.mgmt.globalcontroller.jmetal.Fitness;
import osh.mgmt.globalcontroller.jmetal.IFitness;
import osh.mgmt.globalcontroller.jmetal.SolutionWithFitness;
import osh.mgmt.globalcontroller.jmetal.builder.EAScheduleResult;
import osh.mgmt.globalcontroller.jmetal.esc.EMProblemEvaluator;
import osh.mgmt.globalcontroller.jmetal.esc.JMetalEnergySolverGA;
import osh.mgmt.globalcontroller.jmetal.esc.SolutionDistributor;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;
import osh.simulation.database.DatabaseLoggerThread;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Represents the executing of a planned or forced scheduling.
 *
 * @author Sebastian Kramer
 */
public class ExecuteSchedulingModule extends GlobalControllerModule {

    /**
     * Constructs this module with the given global data sotrage container.
     *
     * @param data the global data storage container for all modules
     */
    public ExecuteSchedulingModule(GlobalControllerDataStorage data) {
        super(data);
        this.PRIORITY = 1;
    }

    /**
     * Executes the scheduling with the given signals and problem-parts.
     *
     * @param priceSignal the price signal
     * @param powerLimitSignal the power-limit signal
     * @param problemPartsList the problem parts
     *
     * @return the result of the scheduling combined with resulting commands/predictions and logging information
     */
    public DetailedOptimisationResults executeScheduling(
            EnumMap<AncillaryCommodity, PriceSignal> priceSignal,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignal,
            List<InterdependentProblemPart<?, ?>> problemPartsList) {

        this.getData().getGlobalLogger().logDebug("=== scheduling... ===");

        final ZonedDateTime now = this.getData().getNow();

        OSHRandom optimisationRunRandomGenerator = new OSHRandom(this.getData().getOptimizationMainRandomGenerator().getNextLong());

        // it is a good idea to use a specific random Generator for the EA,
        // to make it comparable with other optimizers...
        JMetalEnergySolverGA solver = new JMetalEnergySolverGA(
                this.getData().getGlobalLogger(),
                optimisationRunRandomGenerator,
                true,
                this.getData().getGaParameters(),
                now.toEpochSecond(),
                this.getData().getStepSize(),
                this.getData().getStatus().getLogDir());

        InterdependentProblemPart<?, ?>[] problemParts = new InterdependentProblemPart<?, ?>[problemPartsList.size()];
        problemParts = problemPartsList.toArray(problemParts);

        Solution<?> solution;
        SolutionWithFitness resultWithAll;

        long ignoreLoadProfileAfter = now.toEpochSecond();
        long maxHorizon = ignoreLoadProfileAfter;

        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            if (problem instanceof ControllableIPP<?, ?>) {
                maxHorizon = Math.max(problem.getOptimizationHorizon(), maxHorizon);
            }
        }

        int counter = 0;
        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            problem.recalculateEncoding(now.toEpochSecond(), maxHorizon);
            problem.setId(counter);
            counter++;
        }
        ignoreLoadProfileAfter = Math.max(ignoreLoadProfileAfter, maxHorizon);

        boolean hasGUI = this.getData().getStatus().hasGUI();

        EAScheduleResult result;

        try {
            IFitness fitnessFunction = new Fitness(
                    this.getData().getGlobalLogger(),
                    this.getData().getEpsOptimizationObjective(),
                    this.getData().getPlsOptimizationObjective(),
                    this.getData().getVarOptimizationObjective(),
                    this.getData().getUpperOverlimitFactor(),
                    this.getData().getLowerOverlimitFactor());

            resultWithAll = solver.getSolution(
                    problemParts,
                    this.getData().getOcESC(),
                    priceSignal,
                    powerLimitSignal,
                    now.toEpochSecond(),
                    fitnessFunction,
                    this.getData().getEaLogger());
            solution = resultWithAll.getSolution();

            SolutionDistributor distributor = new SolutionDistributor();
            distributor.gatherVariableInformation(problemParts);

            EMProblemEvaluator problem = new EMProblemEvaluator(
                    problemParts,
                    this.getData().getOcESC(),
                    distributor,
                    priceSignal,
                    powerLimitSignal,
                    now.toEpochSecond(),
                    ignoreLoadProfileAfter,
                    fitnessFunction,
                    this.getData().getEaLogger(),
                    this.getData().getStepSize());

            boolean extensiveLogging = hasGUI && !distributor.getVariableInformation(VariableEncoding.BINARY).needsNoVariables();
            AncillaryCommodityLoadProfile ancillaryMeter = new AncillaryCommodityLoadProfile();

            problem.evaluateFinalTime(solution, (DatabaseLoggerThread.isLogEA() | extensiveLogging), ancillaryMeter);

            distributor.distributeSolution(solution, problemParts);


            TreeMap<Long, Double> predictedHotWaterTankTemperature = new TreeMap<>();
            TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<>();
            TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<>();
            List<Schedule> schedules = new ArrayList<>();

            if (extensiveLogging) {
                for (InterdependentProblemPart<?, ?> part : problemParts) {
                    schedules.add(part.getFinalInterdependentSchedule());

                    //extract prediction about tank temperatures of the hot-water tank
                    if (part.getUUID().equals(this.getData().getHotWaterTankID())) {
                        @SuppressWarnings("unchecked")
                        EAPredictionCommandExchange<TemperaturePrediction> prediction = (EAPredictionCommandExchange<TemperaturePrediction>) part.transformToFinalInterdependentPrediction(
                                this.getData().getUUID(),
                                part.getUUID(),
                                now);

                        predictedHotWaterTankTemperature = prediction.getPrediction().getTemperatureStates();
                    }

                    //extract information about hot-water demand and supply
                    if (part.getAllOutputCommodities().contains(Commodity.DOMESTICHOTWATERPOWER)
                            || part.getAllOutputCommodities().contains(Commodity.HEATINGHOTWATERPOWER)) {
                        SparseLoadProfile loadProfile = part.getLoadProfile();

                        for (long t = now.toEpochSecond(); t < maxHorizon + this.getData().getStepSize(); t += this.getData().getStepSize()) {
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

            result = new EAScheduleResult(predictedHotWaterTankTemperature, predictedHotWaterDemand,
                    predictedHotWaterSupply, schedules, ancillaryMeter, solution,
                    distributor.getVariableInformation(VariableEncoding.BINARY).needsNoVariables());


        } catch (Exception e) {
            e.printStackTrace();
            this.getData().getGlobalLogger().logError(e);
            return null;
        }

        Map<UUID, EASolutionCommandExchange<?>> solutionExchanges = new Object2ObjectOpenHashMap<>();
        Map<UUID, EAPredictionCommandExchange<?>> predictionExchanges = new Object2ObjectOpenHashMap<>();

        for (InterdependentProblemPart<?, ?> part : problemParts) {
            LocalController lc = this.getData().getLocalController(part.getUUID());

            if (lc != null) {
                solutionExchanges.put(part.getUUID(), part.transformToFinalInterdependentPhenotype(
                        this.getData().getUUID(),
                        part.getUUID(),
                        now));
            }
//			this sends a prediction of the waterTemperatures to the waterTankObserver, so the waterTank can trigger a reschedule
//			when the actual temperatures are too different to the prediction
            if (part.transformToFinalInterdependentPrediction() != null) {
                predictionExchanges.put(part.getUUID(),
                        part.transformToFinalInterdependentPrediction(
                                this.getData().getUUID(),
                                part.getUUID(),
                                now));
            }
        }

        this.getData().getGlobalLogger().logDebug("===    EA done    ===");

        return new DetailedOptimisationResults(solutionExchanges, predictionExchanges, result);
    }

    @Override
    public void onSystemShutdown() {
        this.getData().getEaLogger().shutdown();
    }
}
