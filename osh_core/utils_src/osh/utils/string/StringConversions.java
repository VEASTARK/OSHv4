package osh.utils.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Converting "arrays as string" back to primitive arrays
 *
 * @author Sebastian Kramer
 */
public class StringConversions {

    private static final Pattern WHITE_SPACES = Pattern.compile("\\s");
    private static final Pattern BRACKETS = Pattern.compile("[\\[\\]]");
    private static final Pattern COMMA_BRACKET = Pattern.compile(",?\\s?\\[");

    public static Long[] fromStringToLongArray(String s) {
        String trimmed = BRACKETS.matcher(WHITE_SPACES.matcher(s).replaceAll("")).replaceAll("");

        return Arrays.stream(trimmed.split(",")).map(Long::decode).toArray(Long[]::new);
    }

    public static Double[] fromStringToDoubleArray(String s) {
        String trimmed = BRACKETS.matcher(WHITE_SPACES.matcher(s).replaceAll("")).replaceAll("");

        return Arrays.stream(trimmed.split(",")).map(Double::parseDouble).toArray(Double[]::new);
    }

    public static String from2DimDoubleArrayToString(double[][] arr) {
        return Arrays.toString(Arrays.stream(arr).map(Arrays::toString).toArray(String[]::new));
    }

    public static Long[][] fromStringTo2DimLongArray(String s) {
        String trimmed = WHITE_SPACES.matcher(s).replaceAll("");

        StringTokenizer st = new StringTokenizer(trimmed, "]");
        ArrayList<Long[]> list = new ArrayList<>();

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            token = COMMA_BRACKET.matcher(token).replaceAll("");
            token = WHITE_SPACES.matcher(token).replaceAll("");
            list.add(fromStringToLongArray(token));
        }

        return list.toArray(Long[][]::new);
    }

    public static Integer[] fromStringToIntegerArray(String s) {
        String trimmed = BRACKETS.matcher(WHITE_SPACES.matcher(s).replaceAll("")).replaceAll("");

        return Arrays.stream(trimmed.split(",")).map(Integer::decode).toArray(Integer[]::new);
    }

    public static int[] fromStringToIntArray(String s) {
        Integer[] arr = StringConversions.fromStringToIntegerArray(s);
        int[] ret = new int[arr.length];
        for (int d0 = 0; d0 < ret.length; d0++) {
            ret[d0] = arr[d0];
        }

        return ret;
    }

    public static Integer[][] fromStringTo2DimIntegerArray(String s) {
        String trimmed = WHITE_SPACES.matcher(s).replaceAll("");

        StringTokenizer st = new StringTokenizer(trimmed, "]");
        ArrayList<Integer[]> list = new ArrayList<>();

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            token = COMMA_BRACKET.matcher(token).replaceAll("");
            token = WHITE_SPACES.matcher(token).replaceAll("");
            list.add(fromStringToIntegerArray(token));
        }

        return list.toArray(Integer[][]::new);
    }
}
