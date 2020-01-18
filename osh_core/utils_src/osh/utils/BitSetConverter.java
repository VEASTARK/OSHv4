package osh.utils;

import java.util.BitSet;

/**
 * @author Ingo Mauser
 */
public class BitSetConverter {

    /**
     * converts a bitset to a long value.
     *
     * @param bitset to be converted
     * @return long value from bitset
     */
    //tested
    public static long bitSet2long(BitSet bitset) {
        if (bitset.length() > Long.SIZE - 1) {
            throw new IllegalArgumentException("bitset is too big");
        }
        long ret = 0;
        int bitSize = bitset.length();
        for (int i = bitSize; i >= 0; i--) {
            ret <<= 1;
            if (bitset.get(i)) ret |= 1L;
        }

        return ret;
    }

    //tested
    public static long gray2long(BitSet gray) {
        BitSet binary = new BitSet();
        int bitSize = gray.length();

        if (bitSize > 0) {
            binary.set(bitSize - 1, gray.get(bitSize - 1));
        }

        for (int i = bitSize - 2; i >= 0; i--) {
            if (binary.get(i + 1) != gray.get(i)) //xor
                binary.set(i);
        }

        return bitSet2long(binary);
    }

    public static BitSet long2bitSet(long n) {
        if (n < 0) throw new IllegalArgumentException("n is negative");

        BitSet ret = new BitSet();
        long mask = 0x01L;

        for (int i = 0; i < 64; i++) {
            if ((n & mask) != 0L) ret.set(i);
            mask <<= 1;
        }

        return ret;
    }

}
