package osh.utils.costs;

import osh.utils.functions.PrimitiveOperators;

/**
 *
 *
 * @author Sebastian Kramer
 */
public class ArrayCalcIterators {

    public static double iterator(
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
