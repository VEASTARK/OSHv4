package osh.utils.uuid;

import osh.en50523.EN50523Brand;
import osh.en50523.EN50523Company;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;


/**
 * Generation of UUIDs
 *
 * @author Kaibin Bao, Ingo Mauser
 */
public class UUIDGenerationHelperMiele {

    /* MIELE@HOME AND DIN EN 50523 STUFF */

    /**
     * Generates the higher part of Miele-UUIDs
     *
     * @param mieleUID
     * @return
     */
    public static long getMieleUUIDHigherPart(int mieleUID) {
        return getHomeApplianceUUIDHigherPart(
                mieleUID,
                EN50523Company.MIELE.getCompanyID(),
                EN50523Brand.MIELE.getBrandID());
    }

    public static long getMieleUUIDLowerPart(int deviceType50523, InetAddress address) throws Exception {
        return getHomeApplianceUUIDLowerPart(deviceType50523, address);
    }


    /* GENERAL HOME / HOUSEHOLD APPLIANCE */

    public static long getHomeApplianceUUIDHigherPart(int first32Bit, short companyID, short brandID) {
        return (((first32Bit & 0xFFFFFFFFL) << 32) |
                ((companyID & 0xFFFFL) << 16) |
                ((brandID & 0xFFFFL)));
    }


    public static long getHomeApplianceUUIDLowerPart(int deviceType50523, InetAddress address) throws Exception {
        byte[] b_addr = address.getAddress();

        if (address instanceof Inet4Address || address instanceof Inet6Address)
            return getUUIDLowerPart(b_addr, deviceType50523);
        else {
            throw new Exception("Unknown IP version");
        }
    }

    /* GENERIC STUFF */

    private static long getUUIDLowerPart(byte[] low_array, int high_part) {
        // keep brackets and 0xff because of negative numbers
        if (low_array.length >= 8) {
            return (((long) high_part) << 32) ^ // just don't ask why...
                    ((((long) low_array[0] & 0xff) << 48) |
                            (((long) low_array[1] & 0xff) << 32) |
                            (((long) low_array[2] & 0xff) << 24) | // EUI-64 ... byte 4..5 are skipped
                            (((long) low_array[5] & 0xff) << 16) |
                            (((long) low_array[6] & 0xff) << 8) |
                            (((long) low_array[7] & 0xff)));
        } else if (low_array.length >= 4) {
            return (((long) high_part) << 32) |
                    (((long) low_array[0] & 0xff) << 24) |
                    (((long) low_array[1] & 0xff) << 16) |
                    (((long) low_array[2] & 0xff) << 8) |
                    (((long) low_array[3] & 0xff));
        } else {
            return (((long) high_part) << 32);
        }
    }


}
