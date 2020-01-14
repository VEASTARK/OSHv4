package osh.utils.hex;

import java.util.regex.Pattern;

/**
 * @author Ingo Mauser
 */
public class HexUtilities {


    private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]+");

    public static boolean checkTextIfHex(String text) {

        return HEX_PATTERN.matcher(text).matches();
    }


}
