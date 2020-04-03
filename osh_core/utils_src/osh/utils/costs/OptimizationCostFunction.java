package osh.utils.costs;

import osh.configuration.oc.*;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.ErsatzACLoadProfile;
import osh.datatypes.power.PowerInterval;
import osh.utils.CostReturnType;
import osh.utils.dataStructures.Enum2DoubleMap;
import osh.utils.functions.PrimitiveOperators;
import osh.utils.physics.PhysicalConstants;

import java.util.*;

/**
 * Represents the internal cost function of the OSH, configurable with the {@link CostConfiguration} variables,
 * executing the calculation inside the optimization loop or in the {@link osh.simulation.SimulationEngine}.
 *
 * @author Sebastian Kramer
 */
public class OptimizationCostFunction {

    private final CostConfiguration costConfiguration;

    private long arraysCalculatedFor;

    private EnumMap<AncillaryCommodity, KeysValuesTuple> priceSignals;
    private EnumMap<AncillaryCommodity, KeysValuesTuple> upperPowerLimitSignals;
    private EnumMap<AncillaryCommodity, KeysValuesTuple> lowerPowerLimitSignals;

    private List<SingularCostFunctionConfiguration<?>> activeConfiguration, reactiveConfiguration, feedInConfiguration,
            autoConsumptionConfiguration, gasConfiguration, selfSufficiencyParameterList, selfConsumptionParameterList;

    /**
     * Constructs this cost function with the given cost configuration and all relevant signals.
     *
     * @param overlimitFactor the overlimit factor for pls violations
     * @param costConfiguration the cost configuration
     * @param priceSignals the price signals
     * @param powerLimitSignals the power limit signals
     * @param now the current time in epoch seconds
     */
    public OptimizationCostFunction(double overlimitFactor, CostConfiguration costConfiguration,
                                    Map<AncillaryCommodity, PriceSignal> priceSignals,
                                    Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals, long now) {

        this.costConfiguration = costConfiguration;
        CalculationFunctions.setOverlimitFactors(overlimitFactor);

        this.initializeConfigurations();
        this.processSignals(priceSignals, powerLimitSignals, now);
    }

    /**
     * Initializes the internal functions based on the cost configuration.
     *
     */
    private void initializeConfigurations() {
        //reset all lists
        this.activeConfiguration = new ArrayList<>();
        this.reactiveConfiguration = new ArrayList<>();
        this.feedInConfiguration = new ArrayList<>();
        this.autoConsumptionConfiguration = new ArrayList<>();
        this.gasConfiguration = new ArrayList<>();
        this.selfSufficiencyParameterList = new ArrayList<>();
        this.selfConsumptionParameterList = new ArrayList<>();

        //active power pricing
        if (this.costConfiguration.getActivePlsCosts() == ActivePlsCosts.FULL) {
            List<SingularArgumentTuple> arguments = new ArrayList<>();
            arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            arguments.add(new SingularArgumentTuple(ArgumentType.UPPER_LIMIT, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            arguments.add(new SingularArgumentTuple(ArgumentType.LOWER_LIMIT, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));

            this.activeConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_quad,
                    CalculationFunctions::priceAbsPowerLimitFunction));
        } else if (this.costConfiguration.getActivePlsCosts() == ActivePlsCosts.UPPER) {
            List<SingularArgumentTuple> arguments = new ArrayList<>();
            arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            arguments.add(new SingularArgumentTuple(ArgumentType.UPPER_LIMIT, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));

            this.activeConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_tri,
                    CalculationFunctions::pricePositivePowerLimitFunction));
        } else {
            List<SingularArgumentTuple> arguments = new ArrayList<>();
            arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));

            this.activeConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_bi,
                    CalculationFunctions::pricePositivePowerFunction));
        }

        //reactive power pricing
        if (this.costConfiguration.getReactiveCosts() != ReactiveCosts.NONE) {
            if (this.costConfiguration.getReactivePlsCosts() == ReactivePlsCosts.FULL) {
                List<SingularArgumentTuple> arguments = new ArrayList<>();
                arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.REACTIVEPOWEREXTERNAL));
                arguments.add(new SingularArgumentTuple(ArgumentType.UPPER_LIMIT, AncillaryCommodity.REACTIVEPOWEREXTERNAL));
                arguments.add(new SingularArgumentTuple(ArgumentType.LOWER_LIMIT, AncillaryCommodity.REACTIVEPOWEREXTERNAL));
                arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.REACTIVEPOWEREXTERNAL));

                this.reactiveConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_quad,
                        CalculationFunctions::priceAbsPowerLimitFunction));
            } else {
                List<SingularArgumentTuple> arguments = new ArrayList<>();
                arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.REACTIVEPOWEREXTERNAL));
                arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.REACTIVEPOWEREXTERNAL));

                this.reactiveConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_bi,
                        CalculationFunctions::priceAbsPowerFunction));
            }
        }

        //feed in pricing
        if (this.costConfiguration.getFeedInCosts() != FeedInCosts.NONE) {
            if (this.costConfiguration.getActivePlsCosts() != ActivePlsCosts.FULL) {
                if (this.costConfiguration.getFeedInCosts() == FeedInCosts.BOTH
                        || this.costConfiguration.getFeedInCosts() == FeedInCosts.PV) {
                    List<SingularArgumentTuple> arguments = new ArrayList<>();
                    arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.PVACTIVEPOWERFEEDIN));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.PVACTIVEPOWERFEEDIN));

                    this.feedInConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_bi,
                            CalculationFunctions::priceNegativePowerFunction));
                }
                if (this.costConfiguration.getFeedInCosts() == FeedInCosts.BOTH
                        || this.costConfiguration.getFeedInCosts() == FeedInCosts.CHP) {
                    List<SingularArgumentTuple> arguments = new ArrayList<>();
                    arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.CHPACTIVEPOWERFEEDIN));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.CHPACTIVEPOWERFEEDIN));

                    this.feedInConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_bi,
                            CalculationFunctions::priceNegativePowerFunction));
                }
            } else {
                if (this.costConfiguration.getFeedInCosts() == FeedInCosts.BOTH) {
                    List<SingularArgumentTuple> arguments = new ArrayList<>();
                    arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.PVACTIVEPOWERFEEDIN));
                    arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.CHPACTIVEPOWERFEEDIN));
                    arguments.add(new SingularArgumentTuple(ArgumentType.LOWER_LIMIT, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.PVACTIVEPOWERFEEDIN));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.CHPACTIVEPOWERFEEDIN));

                    this.feedInConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_hex,
                            CalculationFunctions::twoPriceNegativePowerLimitFunction));
                } else if (this.costConfiguration.getFeedInCosts() == FeedInCosts.PV) {
                    List<SingularArgumentTuple> arguments = new ArrayList<>();
                    arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.PVACTIVEPOWERFEEDIN));
                    arguments.add(new SingularArgumentTuple(ArgumentType.LOWER_LIMIT, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.PVACTIVEPOWERFEEDIN));

                    this.feedInConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_quad,
                            CalculationFunctions::priceNegativePowerLimitFunction));
                } else {
                    List<SingularArgumentTuple> arguments = new ArrayList<>();
                    arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.CHPACTIVEPOWERFEEDIN));
                    arguments.add(new SingularArgumentTuple(ArgumentType.LOWER_LIMIT, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
                    arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.CHPACTIVEPOWERFEEDIN));

                    this.feedInConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_quad,
                            CalculationFunctions::priceNegativePowerLimitFunction));
                }
            }
        }

        //auto-consumption costs
        if (this.costConfiguration.getAutoConsumptionCosts() != AutoConsumptionCosts.NONE) {
            if (this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.BOTH
                    || this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.PV) {
                List<SingularArgumentTuple> arguments = new ArrayList<>();
                arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION));
                arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION));

                this.autoConsumptionConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_bi,
                        CalculationFunctions::pricePowerFunction));
            }
            if (this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.BOTH
                    || this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.CHP) {
                List<SingularArgumentTuple> arguments = new ArrayList<>();
                arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION));
                arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION));

                this.autoConsumptionConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_bi,
                        CalculationFunctions::pricePowerFunction));
            }
        }

        //natural gas costs
        List<SingularArgumentTuple> arguments = new ArrayList<>();
        arguments.add(new SingularArgumentTuple(ArgumentType.PRICE, AncillaryCommodity.NATURALGASPOWEREXTERNAL));
        arguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.NATURALGASPOWEREXTERNAL));

        this.gasConfiguration.add(new SingularCostFunctionConfiguration<>(arguments, this::execute_bi,
                CalculationFunctions::pricePowerFunction));

        if (this.costConfiguration.getSelfSufficiencyRatio() == SelfSufficiencyRatio.NORMAL) {
            List<SingularArgumentTuple> selfSufficiencyArguments = new ArrayList<>();
            selfSufficiencyArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            selfSufficiencyArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION));
            selfSufficiencyArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION));
            selfSufficiencyArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION));

            this.selfSufficiencyParameterList.add(new SingularCostFunctionConfiguration<>(selfSufficiencyArguments, this::execute_quad,
                    CalculationFunctions::positiveQuadPowerFractionFunction));
        }

        if (this.costConfiguration.getSelfConsumptionRatio() == SelfConsumptionRatio.NORMAL) {
            List<SingularArgumentTuple> selfConsumptionArguments = new ArrayList<>();
            selfConsumptionArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.ACTIVEPOWEREXTERNAL));
            selfConsumptionArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION));
            selfConsumptionArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION));
            selfConsumptionArguments.add(new SingularArgumentTuple(ArgumentType.POWER, AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION));

            this.selfConsumptionParameterList.add(new SingularCostFunctionConfiguration<>(selfConsumptionArguments, this::execute_quad,
                    CalculationFunctions::negativeQuadPowerFractionFunction));
        }
    }

    /**
     * Calculates the costs resulting from the supplied load profile and the current cost configuration.
     *
     * @param start the starting point for the calculation
     * @param end the ending point for the calculation
     * @param ancillaryLoadProfile the load profile
     *
     * @return the costs resulting from the supplied load profile and the current cost configuration as a map
     */
    public Enum2DoubleMap<CostReturnType> calculateCosts(
            long start,
            long end,
            ErsatzACLoadProfile ancillaryLoadProfile) {

        if (this.arraysCalculatedFor != start) {
            throw new UnsupportedOperationException("Cost function in use in the optimization loop must be created " +
                    "new for every optimization run.");
        }

        return this.calcInternalCosts(start, end, ancillaryLoadProfile);
    }

    private Enum2DoubleMap<CostReturnType> calcInternalCosts(
            long start,
            long end,
            ErsatzACLoadProfile ancillaryMeter) {

        double electricity = 0.0, gas = 0.0, selfSufficiency = 0.0, selfConsumption = 0.0;

        electricity += this.execute(ancillaryMeter, start, end, this.activeConfiguration);
        electricity += this.execute(ancillaryMeter, start, end, this.reactiveConfiguration);
        electricity += this.execute(ancillaryMeter, start, end, this.feedInConfiguration);
        electricity += this.execute(ancillaryMeter, start, end, this.autoConsumptionConfiguration);

        gas += this.execute(ancillaryMeter, start, end, this.gasConfiguration);

        //minimization problem, but higher ssr is more desirable -> negate
        selfSufficiency -= this.execute(ancillaryMeter, start, end, this.selfSufficiencyParameterList);

        //minimization problem, but higher scr is more desirable -> negate
        selfConsumption -= this.execute(ancillaryMeter, start, end, this.selfConsumptionParameterList);

        Enum2DoubleMap<CostReturnType> costs = new Enum2DoubleMap<>(CostReturnType.class);

        costs.put(CostReturnType.ELECTRICITY, electricity);
        costs.put(CostReturnType.GAS, gas);

        costs.put(CostReturnType.ELECTRICITY, electricity / PhysicalConstants.factor_wsToKWh);
        costs.put(CostReturnType.GAS, gas / PhysicalConstants.factor_wsToKWh);

        costs.put(CostReturnType.SELF_SUFFICIENCY_RATIO, selfSufficiency / (end - start));
        costs.put(CostReturnType.SELF_CONSUMPTION_RATIO, selfConsumption / (end - start));

        return costs;
    }

    /**
     * Converts the given price and power-limit signals into the internal {@link KeysValuesTuple} format and stores
     * them.
     *
     * @param priceSignals the price signals
     * @param powerLimitSignals the power-limit signals
     * @param now the current time in epoch seconds
     */
    private void processSignals(Map<AncillaryCommodity, PriceSignal> priceSignals, Map<AncillaryCommodity,
            PowerLimitSignal> powerLimitSignals, long now) {

        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
        this.upperPowerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
        this.lowerPowerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

        for (AncillaryCommodity ac : AncillaryCommodity.values()) {
            if (priceSignals != null && priceSignals.containsKey(ac)) {
                this.priceSignals.put(ac, this.convertPriceMap(priceSignals.get(ac).cloneAfter(now).getPrices()));
            }
            if (powerLimitSignals != null && powerLimitSignals.containsKey(ac)) {
                KeysValuesTuple[] powerLimits =
                        this.convertPowerLimitMap(powerLimitSignals.get(ac).cloneAfter(now).getLimits());
                this.upperPowerLimitSignals.put(ac, powerLimits[0]);
                this.lowerPowerLimitSignals.put(ac, powerLimits[1]);
            }
        }

        this.arraysCalculatedFor = now;
    }

    /**
     * Converts all entries of the provided sorted map into key and value arrays and returns them in the internal
     * {@link KeysValuesTuple} format.
     *
     *
     * @param sortedMap the sorted map
     * @return the contents of the sorted map in the {@link KeysValuesTuple} format
     */
    private KeysValuesTuple convertPriceMap(SortedMap<Long, Double> sortedMap) {
        long[] keys = new long[sortedMap.size() + 1];
        double[] values = new double[sortedMap.size() + 1];

        int i = 0;
        for (Map.Entry<Long, Double> en : sortedMap.entrySet()) {
            keys[i] = en.getKey();
            values[i] = en.getValue();
            i++;
        }
        keys[i] = Long.MAX_VALUE;
        values[i] = Double.NaN;

        return new KeysValuesTuple(keys, values);
    }

    /**
     * Converts all entries of the provided sorted map containing {@link PowerInterval} into key and upper and
     * lower limit value arrays and returns them as two of the the internal {@link KeysValuesTuple} format.
     *
     *
     * @param sortedMap the sorted map
     * @return the contents of the sorted map in the {@link KeysValuesTuple} format split into upper and lower limits
     */
    private KeysValuesTuple[] convertPowerLimitMap(SortedMap<Long, PowerInterval> sortedMap) {
        long[] keys = new long[sortedMap.size() + 1];
        double[] upperValues = new double[sortedMap.size() + 1];
        double[] lowerValues = new double[sortedMap.size() + 1];

        int i = 0;
        for (Map.Entry<Long, PowerInterval> en : sortedMap.entrySet()) {
            keys[i] = en.getKey();
            upperValues[i] = en.getValue().getPowerUpperLimit();
            lowerValues[i] = en.getValue().getPowerLowerLimit();
            i++;
        }
        keys[i] = Long.MAX_VALUE;
        upperValues[i] = Double.NaN;
        lowerValues[i] = Double.NaN;

        return new KeysValuesTuple[] {new KeysValuesTuple(keys, upperValues), new KeysValuesTuple(keys, lowerValues)};
    }

    private double execute(ErsatzACLoadProfile ancillaryMeter, long start, long end,
                           List<SingularCostFunctionConfiguration<?>> costFunctions) {
        double costs = 0.0;

        for (SingularCostFunctionConfiguration<?> costFunction : costFunctions) {
            costs += costFunction.calculate(ancillaryMeter, start, end);
        }

        return costs;
    }

    private double execute_uni(ErsatzACLoadProfile ancillaryMeter, long start, long end,
                               List<SingularArgumentTuple> argumentList,
                               PrimitiveOperators.DoubleLongOperator function) {
        long[][] keys = new long[1][];

        return ArrayCalcIterators.uniIterator(
                keys,
                this.getValues(keys, 0, argumentList.get(0), ancillaryMeter),
                start,
                end,
                function);
    }

    private double execute_bi(ErsatzACLoadProfile ancillaryMeter, long start, long end,
                              List<SingularArgumentTuple> argumentList,
                              PrimitiveOperators.BiDoubleLongOperator function) {
        long[][] keys = new long[2][];

        return ArrayCalcIterators.biIterator(
                keys,
                this.getValues(keys, 0, argumentList.get(0), ancillaryMeter),
                this.getValues(keys, 1, argumentList.get(1), ancillaryMeter),
                start,
                end,
                function);
    }

    private double execute_tri(ErsatzACLoadProfile ancillaryMeter, long start, long end,
                               List<SingularArgumentTuple> argumentList,
                               PrimitiveOperators.TriDoubleLongOperator function) {
        long[][] keys = new long[3][];

        return ArrayCalcIterators.triIterator(
                keys,
                this.getValues(keys, 0, argumentList.get(0), ancillaryMeter),
                this.getValues(keys, 1, argumentList.get(1), ancillaryMeter),
                this.getValues(keys, 2, argumentList.get(2), ancillaryMeter),
                start,
                end,
                function);
    }

    private double execute_quad(ErsatzACLoadProfile ancillaryMeter, long start, long end,
                                List<SingularArgumentTuple> argumentList,
                                PrimitiveOperators.QuadDoubleLongOperator function) {
        long[][] keys = new long[4][];

        return ArrayCalcIterators.quadIterator(
                keys,
                this.getValues(keys, 0, argumentList.get(0), ancillaryMeter),
                this.getValues(keys, 1, argumentList.get(1), ancillaryMeter),
                this.getValues(keys, 2, argumentList.get(2), ancillaryMeter),
                this.getValues(keys, 3, argumentList.get(3), ancillaryMeter),
                start,
                end,
                function);
    }

    private double execute_quint(ErsatzACLoadProfile ancillaryMeter, long start, long end,
                                 List<SingularArgumentTuple> argumentList,
                                 PrimitiveOperators.QuintDoubleLongOperator function) {
        long[][] keys = new long[5][];

        return ArrayCalcIterators.quintIterator(
                keys,
                this.getValues(keys, 0, argumentList.get(0), ancillaryMeter),
                this.getValues(keys, 1, argumentList.get(1), ancillaryMeter),
                this.getValues(keys, 2, argumentList.get(2), ancillaryMeter),
                this.getValues(keys, 3, argumentList.get(3), ancillaryMeter),
                this.getValues(keys, 4, argumentList.get(4), ancillaryMeter),
                start,
                end,
                function);
    }

    private double execute_hex(ErsatzACLoadProfile ancillaryMeter, long start, long end,
                               List<SingularArgumentTuple> argumentList,
                               PrimitiveOperators.HexDoubleLongOperator function) {
        long[][] keys = new long[6][];

        return ArrayCalcIterators.hexIterator(
                keys,
                this.getValues(keys, 0, argumentList.get(0), ancillaryMeter),
                this.getValues(keys, 1, argumentList.get(1), ancillaryMeter),
                this.getValues(keys, 2, argumentList.get(2), ancillaryMeter),
                this.getValues(keys, 3, argumentList.get(3), ancillaryMeter),
                this.getValues(keys, 4, argumentList.get(4), ancillaryMeter),
                this.getValues(keys, 5, argumentList.get(5), ancillaryMeter),
                start,
                end,
                function);
    }

    private double[] getValues(long[][] keys, int index, SingularArgumentTuple argumentTuple,
                               ErsatzACLoadProfile ancillaryMeter) {
        switch (argumentTuple.argumentType) {
            case PRICE:
                keys[index] = this.priceSignals.get(argumentTuple.commodity).keys;
                return this.priceSignals.get(argumentTuple.commodity).values;
            case UPPER_LIMIT:
                keys[index] = this.upperPowerLimitSignals.get(argumentTuple.commodity).keys;
                return this.upperPowerLimitSignals.get(argumentTuple.commodity).values;
            case LOWER_LIMIT:
                keys[index] = this.lowerPowerLimitSignals.get(argumentTuple.commodity).keys;
                return this.lowerPowerLimitSignals.get(argumentTuple.commodity).values;
            case POWER:
                keys[index] = ancillaryMeter.getKeyFor(argumentTuple.commodity);
                return ancillaryMeter.getValueFor(argumentTuple.commodity);
            default:
                return null;
        }
    }

    private double getSignalValueAt(KeysValuesTuple signalValues, long time) {
        int index = Arrays.binarySearch(signalValues.keys, time);
        if (index < 0) index = ((index + 1) * -1) - 1;
        if (index < 0 || index >= signalValues.keys.length) {
            throw new IllegalArgumentException();
        }
        return signalValues.values[index];
    }

    /**
     * Represents an internal container for a simple key-value mapping expressed in array form.
     *
     * @author Sebastian Kramer
     */
    private static class KeysValuesTuple {

        private final long[] keys;
        private final double[] values;

        /**
         * Constructs this container with the given keys and values.
         *
         * @param keys an array of all keys
         * @param values an array of all matched values
         */
        public KeysValuesTuple(long[] keys, double[] values) {
            this.keys = keys;
            this.values = values;
        }
    }

    /**
     * Represents the configuration for a single function execution.
     *
     * @param <T> the type of the calculation function
     */
    private static class SingularCostFunctionConfiguration<T> {

        private final List<SingularArgumentTuple> argumentList;
        private final IteratorFunction<T> iteratorFunction;
        private final T calculationFunction;

        /**
         * Constructs this function configuration with the given argument lists, the iterator function and
         * calculation function.
         *
         * @param argumentList the list of all argument configurations
         * @param iteratorFunction the function that iterates over the all arguments
         * @param calculationFunction the function to apply inside the iteration
         */
        public SingularCostFunctionConfiguration(List<SingularArgumentTuple> argumentList,
                                                 IteratorFunction<T> iteratorFunction, T calculationFunction) {
            this.argumentList = argumentList;
            this.iteratorFunction = iteratorFunction;
            this.calculationFunction = calculationFunction;
        }

        /**
         * Executes this function configuration and returns the result.
         *
         * @param ancillaryLoadProfile the load profile for the execution
         * @param start the starting point for the calculation
         * @param end the end point for the calculation
         *
         * @return the result of the function execution
         */
        public double calculate(ErsatzACLoadProfile ancillaryLoadProfile, long start, long end) {
            return this.iteratorFunction.apply(ancillaryLoadProfile, start, end, this.argumentList, this.calculationFunction);
        }
    }

    @FunctionalInterface
    private interface IteratorFunction<T> {
        double apply(ErsatzACLoadProfile s, long t, long u, List<SingularArgumentTuple> v, T w);
    }

    /**
     * Represents a simple tuple of an argument type with a corresponding commodity value.
     */
    private static class SingularArgumentTuple {

        private final ArgumentType argumentType;
        private final AncillaryCommodity commodity;

        /**
         * Constructs this tuple with the given argument type and commodity value
         *
         * @param argumentType the argument type
         * @param commodity the commodity value
         */
        public SingularArgumentTuple(ArgumentType argumentType, AncillaryCommodity commodity) {
            this.argumentType = argumentType;
            this.commodity = commodity;
        }
    }

    private enum ArgumentType {
        PRICE,
        UPPER_LIMIT,
        LOWER_LIMIT,
        POWER
    }
}
