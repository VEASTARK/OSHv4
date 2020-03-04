package org.uma.jmetal.solution;

import org.uma.jmetal.util.binarySet.BinarySet;

/**
 * Interface representing a binary (bitset) solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface BinarySolution extends Solution<BinarySet> {
    int getNumberOfBits(int index);

    int getTotalNumberOfBits();
}
