package org.uma.jmetal.util.front.imp;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.fileinput.VectorFileUtils;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.impl.ArrayPoint;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class implements the {@link Front} interface by using an array of {@link Point} objects
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class ArrayFront implements Front {
    protected Point[] points;
    protected int numberOfPoints;
    private int pointDimensions;

    /**
     * Constructor
     */
    public ArrayFront() {
        this.points = null;
        this.numberOfPoints = 0;
        this.pointDimensions = 0;
    }

    /**
     * Constructor
     */
    public ArrayFront(List<? extends Solution<?>> solutionList) {
        if (solutionList == null) {
            throw new JMetalException("The list of solutions is null");
        } else if (solutionList.isEmpty()) {
            throw new JMetalException("The list of solutions is empty");
        }

        this.numberOfPoints = solutionList.size();
        this.pointDimensions = solutionList.get(0).getNumberOfObjectives();
        this.points = new Point[this.numberOfPoints];

        this.points = new Point[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            Point point = new ArrayPoint(this.pointDimensions);
            for (int j = 0; j < this.pointDimensions; j++) {
                point.setValue(j, solutionList.get(i).getObjective(j));
            }
            this.points[i] = point;
        }
    }

    /**
     * Copy Constructor
     */
    public ArrayFront(Front front) {
        if (front == null) {
            throw new JMetalException("The front is null");
        } else if (front.getNumberOfPoints() == 0) {
            throw new JMetalException("The front is empty");
        }
        this.numberOfPoints = front.getNumberOfPoints();
        this.pointDimensions = front.getPoint(0).getDimension();
        this.points = new Point[this.numberOfPoints];

        this.points = new Point[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.points[i] = new ArrayPoint(front.getPoint(i));
        }
    }

    /**
     * Constructor
     */
    public ArrayFront(int numberOfPoints, int dimensions) {
        this.numberOfPoints = numberOfPoints;
        this.pointDimensions = dimensions;
        this.points = new Point[this.numberOfPoints];

        for (int i = 0; i < this.numberOfPoints; i++) {
            Point point = new ArrayPoint(this.pointDimensions);
            for (int j = 0; j < this.pointDimensions; j++) {
                point.setValue(j, 0.0);
            }
            this.points[i] = point;
        }
    }

    /**
     * Constructor
     *
     * @param fileName File containing the data. Each line of the file is a list of objective values
     * @throws FileNotFoundException
     */
    public ArrayFront(String fileName) throws FileNotFoundException {
        this();
        InputStream inputStream = null;
        try {
            URL url = VectorFileUtils.class.getClassLoader().getResource(fileName);
            if (url != null) {
                String uri = Paths.get(url.toURI()).toString();
                inputStream = this.createInputStream(uri);
            } else {
                inputStream = this.createInputStream(fileName);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        List<Point> list = new ArrayList<>();
        int numberOfObjectives = 0;
        String aux;
        try {
            aux = br.readLine();

            while (aux != null) {
                StringTokenizer tokenizer = new StringTokenizer(aux);
                int i = 0;
                if (numberOfObjectives == 0) {
                    numberOfObjectives = tokenizer.countTokens();
                } else if (numberOfObjectives != tokenizer.countTokens()) {
                    throw new JMetalException("Invalid number of points read. "
                            + "Expected: " + numberOfObjectives + ", received: " + tokenizer.countTokens());
                }

                Point point = new ArrayPoint(numberOfObjectives);
                while (tokenizer.hasMoreTokens()) {
                    double value = Double.parseDouble(tokenizer.nextToken());
                    point.setValue(i, value);
                    i++;
                }
                list.add(point);
                aux = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            throw new JMetalException("Error reading file", e);
        } catch (NumberFormatException e) {
            throw new JMetalException("Format number exception when reading file", e);
        }

        this.numberOfPoints = list.size();
        this.points = new Point[list.size()];
        this.points = list.toArray(this.points);
        if (this.numberOfPoints == 0) {
            this.pointDimensions = 0;
        } else {
            this.pointDimensions = this.points[0].getDimension();
        }
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.points[i] = list.get(i);
        }
    }

    public InputStream createInputStream(String fileName) throws FileNotFoundException {
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        if (inputStream == null) {
            inputStream = new FileInputStream(fileName);
        }

        return inputStream;
    }

    @Override
    public int getNumberOfPoints() {
        return this.numberOfPoints;
    }

    @Override
    public int getPointDimensions() {
        return this.pointDimensions;
    }

    @Override
    public Point getPoint(int index) {
        if (index < 0) {
            throw new JMetalException("The index value is negative");
        } else if (index >= this.numberOfPoints) {
            throw new JMetalException(
                    "The index value (" + index + ") is greater than the number of " + "points (" + this.numberOfPoints + ")");
        }
        return this.points[index];
    }

    @Override
    public void setPoint(int index, Point point) {
        if (index < 0) {
            throw new JMetalException("The index value is negative");
        } else if (index >= this.numberOfPoints) {
            throw new JMetalException("The index value (" + index + ") is greater than the number of "
                    + "points (" + this.numberOfPoints + ")");
        } else if (point == null) {
            throw new JMetalException("The point is null");
        }
        this.points[index] = point;
    }

    @Override
    public void sort(Comparator<Point> comparator) {
        //Arrays.sort(points, comparator);
        Arrays.sort(this.points, 0, this.numberOfPoints, comparator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        ArrayFront that = (ArrayFront) o;

        if (this.numberOfPoints != that.numberOfPoints)
            return false;
        if (this.pointDimensions != that.pointDimensions)
            return false;
        return Arrays.equals(this.points, that.points);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.points);
        result = 31 * result + this.numberOfPoints;
        result = 31 * result + this.pointDimensions;
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.points);
    }
}
