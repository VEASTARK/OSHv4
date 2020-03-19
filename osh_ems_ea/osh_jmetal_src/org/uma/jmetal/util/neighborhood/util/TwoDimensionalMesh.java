package org.uma.jmetal.util.neighborhood.util;

import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.neighborhood.Neighborhood;

import java.util.ArrayList;
import java.util.List;

/**
 * Class defining a bi-dimensional mesh.
 */
@SuppressWarnings("serial")
public class TwoDimensionalMesh<S> implements Neighborhood<S> {
    private final int rows;
    private final int columns;
    private final int[][] neighborhood;
    private int[][] mesh;

    /**
     * Constructor.
     * Defines a neighborhood for list of solutions
     */
    public TwoDimensionalMesh(int rows, int columns, int[][] neighborhood) {
        this.rows = rows;
        this.columns = columns;

        this.neighborhood = neighborhood;

        this.createMesh();
    }

    private void createMesh() {
        // idea: if rows = 5, and columns=3, we need to fill the mesh
        // as follows
        // --------
        //|00-01-02|
        //|03-04-05|
        //|06-07-08|
        //|09-10-11|
        //|12-13-14|
        // --------

        this.mesh = new int[this.rows][this.columns];
        int solution = 0;
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                this.mesh[row][column] = solution++;
            }
        }
    }

    /**
     * Returns the row on the mesh where solution is located
     *
     * @param solution Represents the location of the solution
     * @return
     */
    private int getRow(int solution) {
        return solution / this.columns;
    }

    /**
     * Returns the column on the mesh where solution is located
     *
     * @param solution Represents the location of the solution
     * @return
     */
    private int getColumn(int solution) {
        return solution % this.columns;
    }

    /**
     * Returns the neighbor of solution
     *
     * @param solution Represents the location of the solution
     * @param neighbor Represents the neighbor we want to get as a shift of solution. The first component
     *                 represents the shift on rows, and the second the shift on column
     * @return
     */
    private int getNeighbor(int solution, int[] neighbor) {
        int row = this.getRow(solution);
        int col = this.getColumn((solution));

        int r;
        int c;

        r = (row + neighbor[0]) % this.rows;
        if (r < 0)
            r = this.rows - 1;

        c = (col + neighbor[1]) % this.columns;
        if (c < 0)
            c = this.columns - 1;

        return this.mesh[r][c];
    }

    /**
     * Returns a solutionSet containing the neighbors of a given solution
     *
     * @param solutionSet  From where neighbors will be obtained
     * @param solution     The solution for which the neighbors will be computed
     * @param neighborhood The list of neighbors we want to obtain as shift regarding to solution
     * @return
     */
    private List<S> findNeighbors(List<S> solutionSet, int solution, int[][] neighborhood) {
        List<S> neighbors = new ArrayList<>(neighborhood.length + 1);

        for (int[] neighbor : neighborhood) {
            int index = this.getNeighbor(solution, neighbor);
            neighbors.add(solutionSet.get(index));
        }

        return neighbors;
    }

    /**
     * Returns the north,south, east, and west solutions of a given solution
     *
     * @param solutionList     the solution set from where the neighbors are taken
     * @param solutionPosition Represents the position of the solution
     */
    public List<S> getNeighbors(List<S> solutionList, int solutionPosition) {
        if (solutionList == null) {
            throw new JMetalException("The solution list is null");
        } else if (solutionList.isEmpty()) {
            throw new JMetalException("The solution list is empty");
        } else if (solutionPosition < 0) {
            throw new JMetalException("The solution position value is negative: " + solutionPosition);
        } else if (solutionList.size() != this.rows * this.columns) {
            throw new JMetalException("The solution list size " + solutionList.size() + " is not"
                    + "equal to the grid size: " + this.rows + " * " + this.columns);
        } else if (solutionPosition >= solutionList.size()) {
            throw new JMetalException("The solution position value " + solutionPosition +
                    " is equal or greater than the solution list size "
                    + solutionList.size());
        }

        return this.findNeighbors(solutionList, solutionPosition, this.neighborhood);
    }
}

