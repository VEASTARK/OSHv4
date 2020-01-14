package osh.esc;

/**
 * Provides simple util functions in dealing with arrays
 *
 * @author Sebastian Kramer
 */
public class ArrayUtils {


    /**
     * Assigns the specified boolean value to each element of the specified array of booleans
     *
     * @param array the array to be filled
     * @param value the value to be stored in all elements of the array
     */
    public static void fillArrayBoolean(boolean[] array, boolean value) {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        //Value of i will be [1, 2, 4, 8, 16, 32, ..., len]
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, Math.min((len - i), i));
        }
    }

    /**
     * Assigns the specified double value to each element of the specified array of doubles
     *
     * @param array the array to be filled
     * @param value the value to be stored in all elements of the array
     */
    public static void fillArrayDouble(double[] array, double value) {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        //Value of i will be [1, 2, 4, 8, 16, 32, ..., len]
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, Math.min((len - i), i));
        }
    }
}
