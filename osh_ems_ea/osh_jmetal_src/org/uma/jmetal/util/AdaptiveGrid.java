package org.uma.jmetal.util;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Arrays;
import java.util.List;

/**
 * This class defines an adaptive grid over a list of solutions as the one used by algorithm PAES.
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 */
public class AdaptiveGrid<S extends Solution<?>> {
    private final int bisections;
    private final int numberOfObjectives;

    private final int[] hypercubes;

    private final double[] gridLowerLimits;
    private final double[] gridUpperLimits;

    private final double[] divisionSize;
    private int mostPopulatedHypercube;

    /**
     * Indicates when an hypercube has solutions
     */
    private int[] occupied;

    /**
     * Constructor.
     * Creates an instance of AdaptiveGrid.
     *
     * @param bisections Number of bi-divisions of the objective space.
     * @param objectives Number of numberOfObjectives of the problem.
     */
    public AdaptiveGrid(int bisections, int objectives) {
        this.bisections = bisections;
        this.numberOfObjectives = objectives;
        this.gridLowerLimits = new double[this.numberOfObjectives];
        this.gridUpperLimits = new double[this.numberOfObjectives];
        this.divisionSize = new double[this.numberOfObjectives];
        this.hypercubes = new int[(int) Math.pow(2.0, this.bisections * this.numberOfObjectives)];

        Arrays.fill(this.hypercubes, 0);
    }

    /**
     * Updates the grid limits considering the solutions contained in a
     * <code>solutionList</code>.
     *
     * @param solutionList The <code>solutionList</code> considered.
     */
    private void updateLimits(List<S> solutionList) {
        for (int obj = 0; obj < this.numberOfObjectives; obj++) {
            this.gridLowerLimits[obj] = Double.MAX_VALUE;
            this.gridUpperLimits[obj] = Double.MIN_VALUE;
        }

        //Find the max and min limits of objetives into the population
        for (Solution<?> tmpIndividual : solutionList) {
            for (int obj = 0; obj < this.numberOfObjectives; obj++) {
                if (tmpIndividual.getObjective(obj) < this.gridLowerLimits[obj]) {
                    this.gridLowerLimits[obj] = tmpIndividual.getObjective(obj);
                }
                if (tmpIndividual.getObjective(obj) > this.gridUpperLimits[obj]) {
                    this.gridUpperLimits[obj] = tmpIndividual.getObjective(obj);
                }
            }
        }
    }

    /**
     * Updates the grid adding solutions contained in a specific
     * <code>solutionList</code>.
     * <b>REQUIRE</b> The grid limits must have been previously calculated.
     *
     * @param solutionList The <code>solutionList</code> considered.
     */
    private void addSolutionSet(List<S> solutionList) {
        //Calculate the location of all individuals and update the grid
        this.mostPopulatedHypercube = 0;
        int location;

        for (S s : solutionList) {
            location = this.location(s);
            this.hypercubes[location]++;
            if (this.hypercubes[location] > this.hypercubes[this.mostPopulatedHypercube]) {
                this.mostPopulatedHypercube = location;
            }
        }

        //The grid has been updated, so also update ocuppied's hypercubes
        this.calculateOccupied();
    }


    /**
     * Updates the grid limits and the grid content adding the solutions contained
     * in a specific <code>solutionList</code>.
     *
     * @param solutionList The <code>solutionList</code>.
     */
    public void updateGrid(List<S> solutionList) {
        //Update lower and upper limits
        this.updateLimits(solutionList);

        //Calculate the division size
        for (int obj = 0; obj < this.numberOfObjectives; obj++) {
            this.divisionSize[obj] = this.gridUpperLimits[obj] - this.gridLowerLimits[obj];
        }

        //Clean the hypercubes
        Arrays.fill(this.hypercubes, 0);

        //Add the population
        this.addSolutionSet(solutionList);
    }


    /**
     * Updates the grid limits and the grid content adding a new
     * <code>Solution</code>.
     * If the solution falls out of the grid bounds, the limits and content of the
     * grid must be re-calculated.
     *
     * @param solution    <code>Solution</code> considered to update the grid.
     * @param solutionSet <code>SolutionSet</code> used to update the grid.
     */
    public void updateGrid(S solution, List<S> solutionSet) {

        int location = this.location(solution);
        if (location == -1) {
            //Re-build the Adaptative-Grid
            //Update lower and upper limits
            this.updateLimits(solutionSet);

            //Actualize the lower and upper limits whit the individual
            for (int obj = 0; obj < this.numberOfObjectives; obj++) {
                if (solution.getObjective(obj) < this.gridLowerLimits[obj]) {
                    this.gridLowerLimits[obj] = solution.getObjective(obj);
                }
                if (solution.getObjective(obj) > this.gridUpperLimits[obj]) {
                    this.gridUpperLimits[obj] = solution.getObjective(obj);
                }
            }

            //Calculate the division size
            for (int obj = 0; obj < this.numberOfObjectives; obj++) {
                this.divisionSize[obj] = this.gridUpperLimits[obj] - this.gridLowerLimits[obj];
            }

            //Clean the hypercube
            Arrays.fill(this.hypercubes, 0);

            //add the population
            this.addSolutionSet(solutionSet);
        }
    }

    /**
     * Calculates the hypercube of a solution
     *
     * @param solution The <code>Solution</code>.
     */
    public int location(S solution) {
        //Create a int [] to store the range of each objective
        int[] position = new int[this.numberOfObjectives];

        //Calculate the position for each objective
        for (int obj = 0; obj < this.numberOfObjectives; obj++) {
            if ((solution.getObjective(obj) > this.gridUpperLimits[obj])
                    || (solution.getObjective(obj) < this.gridLowerLimits[obj])) {
                return -1;
            } else if (solution.getObjective(obj) == this.gridLowerLimits[obj]) {
                position[obj] = 0;
            } else if (solution.getObjective(obj) == this.gridUpperLimits[obj]) {
                position[obj] = ((int) Math.pow(2.0, this.bisections)) - 1;
            } else {
                double tmpSize = this.divisionSize[obj];
                double value = solution.getObjective(obj);
                double account = this.gridLowerLimits[obj];
                int ranges = (int) Math.pow(2.0, this.bisections);
                for (int b = 0; b < this.bisections; b++) {
                    tmpSize /= 2.0;
                    ranges /= 2;
                    if (value > (account + tmpSize)) {
                        position[obj] += ranges;
                        account += tmpSize;
                    }
                }
            }
        }

        //Calculate the location into the hypercubes
        int location = 0;
        for (int obj = 0; obj < this.numberOfObjectives; obj++) {
            location += position[obj] * Math.pow(2.0, obj * this.bisections);
        }
        return location;
    }

    /**
     * Returns the value of the most populated hypercube.
     *
     * @return The hypercube with the maximum number of solutions.
     */
    public int getMostPopulatedHypercube() {
        return this.mostPopulatedHypercube;
    }

    /**
     * Returns the number of solutions into a specific hypercube.
     *
     * @param location Number of the hypercube.
     * @return The number of solutions into a specific hypercube.
     */
    public int getLocationDensity(int location) {
        return this.hypercubes[location];
    }

    /**
     * Decreases the number of solutions into a specific hypercube.
     *
     * @param location Number of hypercube.
     */
    public void removeSolution(int location) {
        //Decrease the solutions in the location specified.
        this.hypercubes[location]--;

        //Update the most populated hypercube
        if (location == this.mostPopulatedHypercube) {
            for (int i = 0; i < this.hypercubes.length; i++) {
                if (this.hypercubes[i] > this.hypercubes[this.mostPopulatedHypercube]) {
                    this.mostPopulatedHypercube = i;
                }
            }
        }

        //If hypercubes[location] now becomes to zero, then update ocuppied hypercubes
        if (this.hypercubes[location] == 0) {
            this.calculateOccupied();
        }
    }

    /**
     * Increases the number of solutions into a specific hypercube.
     *
     * @param location Number of hypercube.
     */
    public void addSolution(int location) {
        //Increase the solutions in the location specified.
        this.hypercubes[location]++;

        //Update the most populated hypercube
        if (this.hypercubes[location] > this.hypercubes[this.mostPopulatedHypercube]) {
            this.mostPopulatedHypercube = location;
        }

        //if hypercubes[location] becomes to one, then recalculate
        //the occupied hypercubes
        if (this.hypercubes[location] == 1) {
            this.calculateOccupied();
        }
    }

    /**
     * Returns the number of bi-divisions performed in each objective.
     *
     * @return the number of bi-divisions.
     */
    public int getBisections() {
        return this.bisections;
    }

    /**
     * Returns a String representing the grid.
     *
     * @return The String.
     */
    public String toString() {
        StringBuilder result = new StringBuilder("Grid\n");
        for (int obj = 0; obj < this.numberOfObjectives; obj++) {
            result.append("Objective ").append(obj).append(" ").append(this.gridLowerLimits[obj]).append(" ").append(this.gridUpperLimits[obj]).append("\n");
        }
        return result.toString();
    }

    /**
     * Returns a random hypercube using a rouleteWheel method.
     *
     * @return the number of the selected hypercube.
     */
    public int rouletteWheel() {
        return this.rouletteWheel((a, b) -> JMetalRandom.getInstance().nextDouble(a, b));
    }

    /**
     * Returns a random hypercube using a rouleteWheel method.
     *
     * @param randomGenerator the {@link BoundedRandomGenerator} to use for the roulette
     * @return the number of the selected hypercube.
     */
    public int rouletteWheel(BoundedRandomGenerator<Double> randomGenerator) {
        //Calculate the inverse sum
        double inverseSum = 0.0;
        for (int hypercube : this.hypercubes) {
            if (hypercube > 0) {
                inverseSum += 1.0 / hypercube;
            }
        }

        //Calculate a random value between 0 and sumaInversa
        double random = randomGenerator.getRandomValue(0.0, inverseSum);
        int hypercube = 0;
        double accumulatedSum = 0.0;
        while (hypercube < this.hypercubes.length) {
            if (this.hypercubes[hypercube] > 0) {
                accumulatedSum += 1.0 / this.hypercubes[hypercube];
            }

            if (accumulatedSum > random) {
                return hypercube;
            }

            hypercube++;
        }

        return hypercube;
    }

    /**
     * Calculates the number of hypercubes having one or more solutions.
     * return the number of hypercubes with more than zero solutions.
     */
    public void calculateOccupied() {
        int total = 0;
        for (int hypercube : this.hypercubes) {
            if (hypercube > 0) {
                total++;
            }
        }

        this.occupied = new int[total];
        int base = 0;
        for (int i = 0; i < this.hypercubes.length; i++) {
            if (this.hypercubes[i] > 0) {
                this.occupied[base] = i;
                base++;
            }
        }
    }

    /**
     * Returns the number of hypercubes with more than zero solutions.
     *
     * @return the number of hypercubes with more than zero solutions.
     */
    public int occupiedHypercubes() {
        return this.occupied.length;
    }


    /**
     * Returns a random hypercube that has more than zero solutions.
     *
     * @return The hypercube.
     */
    public int randomOccupiedHypercube() {
        return this.randomOccupiedHypercube((a, b) -> JMetalRandom.getInstance().nextInt(a, b));
    }

    /**
     * Returns a random hypercube that has more than zero solutions.
     *
     * @param randomGenerator the {@link BoundedRandomGenerator} to use for selecting the hypercube
     * @return The hypercube.
     */
    public int randomOccupiedHypercube(BoundedRandomGenerator<Integer> randomGenerator) {
        int rand = randomGenerator.getRandomValue(0, this.occupied.length - 1);
        return this.occupied[rand];
    }

    /**
     * Return the average number of solutions in the occupied hypercubes
     */
    public double getAverageOccupation() {
        this.calculateOccupied();
        double result;

        if (this.occupiedHypercubes() == 0) {
            result = 0.0;
        } else {
            double sum = 0.0;

            for (int value : this.occupied) {
                sum += this.hypercubes[value];
            }

            result = sum / this.occupiedHypercubes();
        }
        return result;
    }

    /* Getters */
    public int[] getHypercubes() {
        return this.hypercubes;
    }
}

