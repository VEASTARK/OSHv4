package osh.mgmt.globalcontroller.jmetal;

import java.util.BitSet;
import java.util.List;

/**
 * @author Ingo Mauser
 */
public class SolutionWithFitness {

    final BitSet fullSet;
    final List<BitSet> bitSet;
    final double fitness;

    public SolutionWithFitness(BitSet fullSet, List<BitSet> bitSet, double fitness) {
        this.fullSet = fullSet;
        this.bitSet = bitSet;
        this.fitness = fitness;
    }


    public BitSet getFullSet() {
        return this.fullSet;
    }

    public List<BitSet> getBitSet() {
        return this.bitSet;
    }

    public double getFitness() {
        return this.fitness;
    }


}
