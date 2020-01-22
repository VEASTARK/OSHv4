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
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Assigns the specified double value to each element of the specified array of doubles
     *
     * @param array the array to be filled
     * @param value the value to be stored in all elements of the array
     */
    public static void fillArrayDouble(double[] array, double value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }
}
