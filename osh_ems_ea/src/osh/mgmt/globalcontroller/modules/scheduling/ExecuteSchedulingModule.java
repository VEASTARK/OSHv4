package osh.mgmt.globalcontroller.modules.scheduling;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.configuration.oc.VariableEncoding;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.logging.general.EALogObject;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.mgmt.globalcontroller.jmetal.builder.AlgorithmExecutor;
import osh.mgmt.globalcontroller.jmetal.builder.EAScheduleResult;
import osh.mgmt.globalcontroller.jmetal.esc.EnergySolver;
import osh.mgmt.globalcontroller.jmetal.esc.SolutionDistributor;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;
import osh.utils.costs.OptimizationCostFunction;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the executing of a planned or forced scheduling.
 *
 * @author Sebastian Kramer
 */
public class ExecuteSchedulingModule extends GlobalControllerModule {

    private AlgorithmExecutor algorithmExecutor;
    private EnergySolver energySolver;

    /**
     * Constructs this module with the given global data sotrage container.
     *
     * @param data the global data storage container for all modules
     */
    public ExecuteSchedulingModule(GlobalControllerDataStorage data) {
        super(data);
        this.PRIORITY = 1;
    }

    @Override
    public void onSystemIsUp() {
        super.onSystemIsUp();

        this.algorithmExecutor = new AlgorithmExecutor(this.getData().getEaConfiguration(), this.getData().getEARandomDistributor(),
                this.getData().getGlobalLogger());
        this.energySolver = new EnergySolver(this.getData().getGlobalLogger(), this.getData().getEaConfiguration(),
                this.getData().getStepSize(), this.getData().getStatus().getLogDir());
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
        final long nowEpoch = now.toEpochSecond();

        InterdependentProblemPart<?, ?>[] problemParts = new InterdependentProblemPart<?, ?>[problemPartsList.size()];
        problemParts = problemPartsList.toArray(problemParts);

        long maxHorizon = nowEpoch;

        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            if (problem instanceof ControllableIPP<?, ?>) {
                maxHorizon = Math.max(problem.getOptimizationHorizon(), maxHorizon);
            }
        }

        int counter = 0;
        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            problem.recalculateEncoding(nowEpoch, maxHorizon);
            problem.setId(counter);
            counter++;
        }

        boolean hasGUI = this.getData().getStatus().hasGUI();
        boolean isReal = !this.getData().getStatus().isSimulation();

        EAScheduleResult result;

        try {
            OptimizationCostFunction costFunction = new OptimizationCostFunction(
                    this.getData().getCostConfiguration(),
                    priceSignal,
                    powerLimitSignal,
                    nowEpoch);

            SolutionDistributor distributor = new SolutionDistributor();
            distributor.gatherVariableInformation(problemParts);

            boolean extensiveLogging =
                    (hasGUI || isReal) && !distributor.getVariableInformation(VariableEncoding.BINARY).needsNoVariables();

            result = this.energySolver.getSolution(
                    problemParts,
                    this.getData().getOptimizationESC(),
                    nowEpoch,
                    costFunction,
                    this.algorithmExecutor,
                    extensiveLogging);

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
        EALogObject logObject = this.algorithmExecutor.getEaLogger().shutdown();

        if (logObject != null) {
            logObject.setSender(this.getData().getUUID());
            this.getData().getOCRegistry().publish(EALogObject.class, logObject);
        }
    }
}
