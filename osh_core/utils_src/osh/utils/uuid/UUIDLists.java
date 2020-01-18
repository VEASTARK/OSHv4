package osh.utils.uuid;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
public class UUIDLists {

    public static ArrayList<UUID> parseUUIDArray(String str) throws IllegalArgumentException {
        String parseString = str;
        while (parseString.startsWith("["))
            parseString = parseString.substring(1);

        while (parseString.endsWith("]"))
            parseString = parseString.substring(0, parseString.length() - 1);

        StringTokenizer strTok = new StringTokenizer(parseString, ",");
        ArrayList<UUID> uuidList = new ArrayList<>();

        while (strTok.hasMoreElements()) {
            UUID uuid = UUID.fromString(strTok.nextToken());
            uuidList.add(uuid);
        }

        return uuidList;
    }
}
