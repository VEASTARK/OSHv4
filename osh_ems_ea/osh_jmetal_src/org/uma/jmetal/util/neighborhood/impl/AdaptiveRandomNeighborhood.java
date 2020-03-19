package org.uma.jmetal.util.neighborhood.impl;

import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.neighborhood.Neighborhood;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the adaptive random neighborhood (topology) defined by M. Clerc.
 * Each solution in a solution list must have a neighborhood composed by it itself and
 * K random selected neighbors (the same solution can be chosen several times).
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class AdaptiveRandomNeighborhood<S> implements Neighborhood<S> {
    private final int solutionListSize;
    private final int numberOfRandomNeighbours;
    private final BoundedRandomGenerator<Integer> randomGenerator;
    private List<List<Integer>> neighbours;

    /**
     * Constructor
     *
     * @param solutionListSize         The expected size of the list of solutions
     * @param numberOfRandomNeighbours The number of neighbors per solution
     */
    public AdaptiveRandomNeighborhood(int solutionListSize, int numberOfRandomNeighbours) {
        this(solutionListSize, numberOfRandomNeighbours, (a, b) -> JMetalRandom.getInstance().nextInt(a, b));
    }

    /**
     * Constructor
     *
     * @param solutionListSize         The expected size of the list of solutions
     * @param numberOfRandomNeighbours The number of neighbors per solution
     * @param randomGenerator          the {@link BoundedRandomGenerator} to use for the randomisation
     */
    public AdaptiveRandomNeighborhood(int solutionListSize, int numberOfRandomNeighbours, BoundedRandomGenerator<Integer> randomGenerator) {
        if (numberOfRandomNeighbours < 0) {
            throw new JMetalException("The number of neighbors is negative: " + numberOfRandomNeighbours);
        } else if (solutionListSize <= numberOfRandomNeighbours) {
            throw new JMetalException("The population size: " + solutionListSize + " is " +
                    "less or equal to the number of requested neighbors: " + numberOfRandomNeighbours);
        }

        this.solutionListSize = solutionListSize;
        this.numberOfRandomNeighbours = numberOfRandomNeighbours;
        this.randomGenerator = randomGenerator;

        this.createNeighborhoods();
        this.addRandomNeighbors();
    }

    /**
     * Initialize all the neighborhoods, adding the current solution to them.
     */
    private void createNeighborhoods() {
        this.neighbours = new ArrayList<>(this.solutionListSize);

        for (int i = 0; i < this.solutionListSize; i++) {
            this.neighbours.add(new ArrayList<>());
            this.neighbours.get(i).add(i);
        }
    }

    /**
     * Add random neighbors to all the neighborhoods
     */
    private void addRandomNeighbors() {
        for (int i = 0; i < this.solutionListSize; i++) {
            while (this.neighbours.get(i).size() <= this.numberOfRandomNeighbours) {
                int random = this.randomGenerator.getRandomValue(0, this.solutionListSize - 1);
                this.neighbours.get(i).add(random);
            }
        }
    }

    private List<S> getIthNeighborhood(List<S> solutionList, int index) {
        List<S> neighborhood = new ArrayList<>();
        for (int i = 0; i < (this.numberOfRandomNeighbours + 1); i++) {
            int neighboursIndex = this.neighbours.get(index).get(i);
            neighborhood.add(solutionList.get(neighboursIndex));
        }

        return neighborhood;
    }

    /**
     * Recomputes the neighbors
     */
    public void recompute() {
        this.createNeighborhoods();
        this.addRandomNeighbors();
    }

    @Override
    public List<S> getNeighbors(List<S> solutionList, int solutionIndex) {
        if (solutionList == null) {
            throw new JMetalException("The solution list is null");
        } else if (solutionList.size() != this.solutionListSize) {
            throw new JMetalException("The solution list size: " + solutionList.size() + " is" +
                    " different to the value: " + this.solutionListSize);
        } else if (solutionIndex < 0) {
            throw new JMetalException("The solution position value is negative: " + solutionIndex);
        } else if (solutionIndex >= solutionList.size()) {
            throw new JMetalException("The solution position value: " + solutionIndex +
                    " is equal or greater than the solution list size: "
                    + solutionList.size());
        }

        return this.getIthNeighborhood(solutionList, solutionIndex);
    }
}
