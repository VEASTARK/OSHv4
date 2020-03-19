package org.uma.jmetal.util.point.impl;

import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.point.Point;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class representing a point (i.e, an array of double values)
 *
 * @author Antonio J. Nebro
 */
public class ArrayPoint implements Point {
    protected double[] point;

    /**
     * Default constructor
     */
    public ArrayPoint() {
        this.point = null;
    }

    /**
     * Constructor
     *
     * @param dimension Dimension of the point
     */
    public ArrayPoint(int dimension) {
        this.point = new double[dimension];

        for (int i = 0; i < dimension; i++) {
            this.point[i] = 0.0;
        }
    }

    /**
     * Copy constructor
     *
     * @param point
     */
    public ArrayPoint(Point point) {
        if (point == null) {
            throw new JMetalException("The point is null");
        }

        this.point = new double[point.getDimension()];

        for (int i = 0; i < point.getDimension(); i++) {
            this.point[i] = point.getValue(i);
        }
    }

    /**
     * Constructor from an array of double values
     *
     * @param point
     */
    public ArrayPoint(double[] point) {
        if (point == null) {
            throw new JMetalException("The array of values is null");
        }

        this.point = new double[point.length];
        System.arraycopy(point, 0, this.point, 0, point.length);
    }

    /**
     * Constructor reading the values from a file
     *
     * @param fileName
     */
    public ArrayPoint(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis);
        try (BufferedReader br = new BufferedReader(isr)) {

            List<Double> auxiliarPoint = new ArrayList<>();
            String aux = br.readLine();
            while (aux != null) {
                StringTokenizer st = new StringTokenizer(aux);

                while (st.hasMoreTokens()) {
                    Double value = Double.parseDouble(st.nextToken());
                    auxiliarPoint.add(value);
                }
                aux = br.readLine();
            }

            this.point = new double[auxiliarPoint.size()];
            for (int i = 0; i < auxiliarPoint.size(); i++) {
                this.point[i] = auxiliarPoint.get(i);
            }

        }
    }

    @Override
    public int getDimension() {
        return this.point.length;
    }

    @Override
    public double[] getValues() {
        return this.point;
    }

    @Override
    public double getValue(int index) {
        if ((index < 0) || (index >= this.point.length)) {
            throw new JMetalException("Index value invalid: " + index +
                    ". The point length is: " + this.point.length);
        }
        return this.point[index];
    }

    @Override
    public void setValue(int index, double value) {
        if ((index < 0) || (index >= this.point.length)) {
            throw new JMetalException("Index value invalid: " + index +
                    ". The point length is: " + this.point.length);
        }
        this.point[index] = value;
    }

    @Override
    public void update(double[] point) {
        if (point.length != this.point.length) {
            throw new JMetalException("The point to be update have a dimension of " + point.length + " "
                    + "while the parameter point has a dimension of " + point.length);
        }
        System.arraycopy(point, 0, this.point, 0, point.length);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (double anObjectives_ : this.point) {
            result.append(anObjectives_).append(" ");
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        ArrayPoint that = (ArrayPoint) o;

        return Arrays.equals(this.point, that.point);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.point);
    }
}
