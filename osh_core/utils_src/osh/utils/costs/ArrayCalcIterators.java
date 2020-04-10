package osh.utils.costs;

import osh.utils.functions.PrimitiveOperators;

/**
 * Represents a fmaily of iterators, over long and double arrays that look for the next change in values and applies
 * a supplied function to the values.
 *
 * @author Sebastian Kramer
 */
public class ArrayCalcIterators {

    /**
     * Iterates over a single key-value array and applies a supplied function after each value change found.
     *
     * @param keys collection of key arrays
     * @param firstValues first value array
     * @param start the starting point of the iteration
     * @param end the end point of the iteration
     * @param calcFunction the function to apply
     *
     * @return the sum of all function results
     */
    public static double uniIterator(
            long[][] keys,
            double[] firstValues,
            long start, long end, PrimitiveOperators.DoubleLongOperator calcFunction) {

        int[] currentIndices = new int[keys.length];

        for (int i = 0; i < currentIndices.length; i++) {
            while(keys[i][currentIndices[i] + 1] <= start) {
                currentIndices[i]++;
            }
        }

        double costs = 0.0;
        long minNextChange;

        while (start < end) {

            minNextChange = keys[0][currentIndices[0] + 1];

            for (int i = 1; i < currentIndices.length; i++) {
                minNextChange = Math.min(minNextChange, keys[i][currentIndices[i] + 1]);
            }

            if (minNextChange > end) {
                minNextChange = end;
            }

            costs += calcFunction.apply(firstValues[currentIndices[0]],minNextChange - start);
            start = minNextChange;
            //will break if minNextChange > end was true, but doesn't matter as the loop will end
            for (int i = 0; i < currentIndices.length; i++) {
                if (keys[i][currentIndices[i] + 1] == minNextChange) currentIndices[i]++;
            }
        }

        return costs;
    }

    /**
     * Iterates over two key-value arrays and applies a supplied function after each value change found.
     *
     * @param keys collection of key arrays
     * @param firstValues first value array
     * @param secondValues second value array
     * @param start the starting point of the iteration
     * @param end the end point of the iteration
     * @param calcFunction the function to apply
     *
     * @return the sum of all function results
     */
    public static double biIterator(
            long[][] keys,
            double[] firstValues,
            double[] secondValues,
            long start, long end, PrimitiveOperators.BiDoubleLongOperator calcFunction) {

        int[] currentIndices = new int[keys.length];

        for (int i = 0; i < currentIndices.length; i++) {
            while(keys[i][currentIndices[i] + 1] <= start) {
                currentIndices[i]++;
            }
        }

        double costs = 0.0;
        long minNextChange;

        while (start < end) {

            minNextChange = keys[0][currentIndices[0] + 1];

            for (int i = 1; i < currentIndices.length; i++) {
                minNextChange = Math.min(minNextChange, keys[i][currentIndices[i] + 1]);
            }

            if (minNextChange > end) {
                minNextChange = end;
            }

            costs += calcFunction.apply(firstValues[currentIndices[0]], secondValues[currentIndices[1]],
                    minNextChange - start);
            start = minNextChange;
            //will break if minNextChange > end was true, but doesn't matter as the loop will end
            for (int i = 0; i < currentIndices.length; i++) {
                if (keys[i][currentIndices[i] + 1] == minNextChange) currentIndices[i]++;
            }
        }

        return costs;
    }

    /**
     * Iterates over three key-value arrays and applies a supplied function after each value change found.
     *
     * @param keys collection of key arrays
     * @param firstValues first value array
     * @param secondValues second value array
     * @param thirdValues third value array
     * @param start the starting point of the iteration
     * @param end the end point of the iteration
     * @param calcFunction the function to apply
     *
     * @return the sum of all function results
     */
    public static double triIterator(
            long[][] keys,
            double[] firstValues,
            double[] secondValues,
            double[] thirdValues,
            long start, long end, PrimitiveOperators.TriDoubleLongOperator calcFunction) {

        int[] currentIndices = new int[keys.length];

        for (int i = 0; i < currentIndices.length; i++) {
            while(keys[i][currentIndices[i] + 1] <= start) {
                currentIndices[i]++;
            }
        }

        double costs = 0.0;
        long minNextChange;

        while (start < end) {

            minNextChange = keys[0][currentIndices[0] + 1];

            for (int i = 1; i < currentIndices.length; i++) {
                minNextChange = Math.min(minNextChange, keys[i][currentIndices[i] + 1]);
            }

            if (minNextChange > end) {
                minNextChange = end;
            }

            costs += calcFunction.apply(firstValues[currentIndices[0]], secondValues[currentIndices[1]],
                    thirdValues[currentIndices[2]],minNextChange - start);
            start = minNextChange;
            //will break if minNextChange > end was true, but doesn't matter as the loop will end
            for (int i = 0; i < currentIndices.length; i++) {
                if (keys[i][currentIndices[i] + 1] == minNextChange) currentIndices[i]++;
            }
        }

        return costs;
    }

    /**
     * Iterates over four key-value arrays and applies a supplied function after each value change found.
     *
     * @param keys collection of key arrays
     * @param firstValues first value array
     * @param secondValues second value array
     * @param thirdValues third value array
     * @param forthValues forth value array
     * @param start the starting point of the iteration
     * @param end the end point of the iteration
     * @param calcFunction the function to apply
     *
     * @return the sum of all function results
     */
    public static double quadIterator(
            long[][] keys,
            double[] firstValues,
            double[] secondValues,
            double[] thirdValues,
            double[] forthValues,
            long start, long end, PrimitiveOperators.QuadDoubleLongOperator calcFunction) {

        int[] currentIndices = new int[keys.length];

        for (int i = 0; i < currentIndices.length; i++) {
            while(keys[i][currentIndices[i] + 1] <= start) {
                currentIndices[i]++;
            }
        }

        double costs = 0.0;
        long minNextChange;

        while (start < end) {

            minNextChange = keys[0][currentIndices[0] + 1];

            for (int i = 1; i < currentIndices.length; i++) {
                minNextChange = Math.min(minNextChange, keys[i][currentIndices[i] + 1]);
            }

            if (minNextChange > end) {
                minNextChange = end;
            }

            costs += calcFunction.apply(firstValues[currentIndices[0]], secondValues[currentIndices[1]],
                    thirdValues[currentIndices[2]], forthValues[currentIndices[3]], minNextChange - start);
            start = minNextChange;
            //will break if minNextChange > end was true, but doesn't matter as the loop will end
            for (int i = 0; i < currentIndices.length; i++) {
                if (keys[i][currentIndices[i] + 1] == minNextChange) currentIndices[i]++;
            }
        }

        return costs;
    }

    /**
     * Iterates over five key-value arrays and applies a supplied function after each value change found.
     *
     * @param keys collection of key arrays
     * @param firstValues first value array
     * @param secondValues second value array
     * @param thirdValues third value array
     * @param forthValues forth value array
     * @param fifthValues fifth value array
     * @param start the starting point of the iteration
     * @param end the end point of the iteration
     * @param calcFunction the function to apply
     *
     * @return the sum of all function results
     */
    public static double quintIterator(
            long[][] keys,
            double[] firstValues,
            double[] secondValues,
            double[] thirdValues,
            double[] forthValues,
            double[] fifthValues,
            long start, long end, PrimitiveOperators.QuintDoubleLongOperator calcFunction) {

        int[] currentIndices = new int[keys.length];

        for (int i = 0; i < currentIndices.length; i++) {
            while(keys[i][currentIndices[i] + 1] <= start) {
                currentIndices[i]++;
            }
        }

        double costs = 0.0;
        long minNextChange;

        while (start < end) {

            minNextChange = keys[0][currentIndices[0] + 1];

            for (int i = 1; i < currentIndices.length; i++) {
                minNextChange = Math.min(minNextChange, keys[i][currentIndices[i] + 1]);
            }

            if (minNextChange > end) {
                minNextChange = end;
            }

            costs += calcFunction.apply(firstValues[currentIndices[0]], secondValues[currentIndices[1]],
                    thirdValues[currentIndices[2]], forthValues[currentIndices[3]],
                    fifthValues[currentIndices[4]], minNextChange - start);
            start = minNextChange;
            //will break if minNextChange > end was true, but doesn't matter as the loop will end
            for (int i = 0; i < currentIndices.length; i++) {
                if (keys[i][currentIndices[i] + 1] == minNextChange) currentIndices[i]++;
            }
        }

        return costs;
    }

    /**
     * Iterates over six key-value arrays and applies a supplied function after each value change found.
     *
     * @param keys collection of key arrays
     * @param firstValues first value array
     * @param secondValues second value array
     * @param thirdValues third value array
     * @param forthValues forth value array
     * @param fifthValues fifth value array
     * @param sixthValues sixth value array
     * @param start the starting point of the iteration
     * @param end the end point of the iteration
     * @param calcFunction the function to apply
     *
     * @return the sum of all function results
     */
    public static double hexIterator(
            long[][] keys,
            double[] firstValues,
            double[] secondValues,
            double[] thirdValues,
            double[] forthValues,
            double[] fifthValues,
            double[] sixthValues,
            long start, long end, PrimitiveOperators.HexDoubleLongOperator
                    calcFunction) {

        int[] currentIndices = new int[keys.length];

        for (int i = 0; i < currentIndices.length; i++) {
            while(keys[i][currentIndices[i] + 1] <= start) {
                currentIndices[i]++;
            }
        }

        double costs = 0.0;
        long minNextChange;

        while (start < end) {

            minNextChange = keys[0][currentIndices[0] + 1];

            for (int i = 1; i < currentIndices.length; i++) {
                minNextChange = Math.min(minNextChange, keys[i][currentIndices[i] + 1]);
            }

            if (minNextChange > end) {
                minNextChange = end;
            }

            costs += calcFunction.apply(firstValues[currentIndices[0]], secondValues[currentIndices[1]],
                    thirdValues[currentIndices[2]], forthValues[currentIndices[3]],
                    fifthValues[currentIndices[4]], sixthValues[currentIndices[5]], minNextChange - start);
            start = minNextChange;
            //will break if minNextChange > end was true, but doesn't matter as the loop will end
            for (int i = 0; i < currentIndices.length; i++) {
                if (keys[i][currentIndices[i] + 1] == minNextChange) currentIndices[i]++;
            }
        }

        return costs;
    }
}
