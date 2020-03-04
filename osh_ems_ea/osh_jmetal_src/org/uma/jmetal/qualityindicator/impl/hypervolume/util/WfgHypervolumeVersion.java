package org.uma.jmetal.qualityindicator.impl.hypervolume.util;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.impl.ArrayPoint;
import org.uma.jmetal.util.point.util.comparator.PointComparator;
import org.uma.jmetal.util.solutionattribute.impl.HypervolumeContributionAttribute;

import java.util.Comparator;
import java.util.List;

/**
 * Created by ajnebro on 2/2/15.
 */
public class WfgHypervolumeVersion {
    static final int OPT = 2;
    final WfgHypervolumeFront[] fs;
    final boolean maximizing;
    private final Point referencePoint;
    private final int maxNumberOfPoints;
    private final Comparator<Point> pointComparator;
    private int currentDeep;
    private int currentDimension;

    public WfgHypervolumeVersion(int dimension, int maxNumberOfPoints) {
        this(dimension, maxNumberOfPoints, new ArrayPoint(dimension));
    }

    public WfgHypervolumeVersion(int dimension, int maxNumberOfPoints, Point referencePoint) {
        this.referencePoint = new ArrayPoint(referencePoint);
        this.maximizing = false;
        this.currentDeep = 0;
        this.currentDimension = dimension;
        this.maxNumberOfPoints = maxNumberOfPoints;
        this.pointComparator = new PointComparator();

        int maxd = this.maxNumberOfPoints - (OPT / 2 + 1);
        this.fs = new WfgHypervolumeFront[maxd];
        for (int i = 0; i < maxd; i++) {
            this.fs[i] = new WfgHypervolumeFront(maxNumberOfPoints, dimension);
        }
    }

    public static void main(String[] args) throws JMetalException {
        WfgHypervolumeFront front = new WfgHypervolumeFront();

        if (args.length == 0) {
            throw new JMetalException("Usage: WFGHV front [reference point]");
        }

        //if (args.length > 0) {
        // TODO: front.readFrontFromFile(args[0]);
        //}

        int dimensions = front.getPointDimensions();
        Point referencePoint;
        double[] points = new double[dimensions];

        if (args.length == (dimensions + 1)) {
            for (int i = 1; i <= dimensions; i++) {
                points[i - 1] = Double.parseDouble(args[i]);
            }
        } else {
            for (int i = 1; i <= dimensions; i++) {
                points[i - 1] = 0.0;
            }
        }

        referencePoint = new ArrayPoint(points);
        JMetalLogger.logger.info("Using reference point: " + referencePoint);

        WfgHypervolumeVersion wfghv =
                new WfgHypervolumeVersion(referencePoint.getDimension(), front.getNumberOfPoints(), referencePoint);

        System.out.println("HV: " + wfghv.getHV(front));
    }

    public double get2DHV(WfgHypervolumeFront front) {
        double hv = 0.0;

        hv = Math.abs((front.getPoint(0).getValue(0) - this.referencePoint.getValue(0)) *
                (front.getPoint(0).getValue(1) - this.referencePoint.getValue(1)));

        for (int i = 1; i < front.getNumberOfPoints(); i++) {
            hv += Math.abs((front.getPoint(i).getValue(0) - this.referencePoint.getValue(0)) *
                    (front.getPoint(i).getValue(1) - front.getPoint(i - 1).getValue(1)));

        }

        return hv;
    }

    public double getInclusiveHV(Point point) {
        double volume = 1;
        for (int i = 0; i < this.currentDimension; i++) {
            volume *= Math.abs(point.getValue(i) - this.referencePoint.getValue(i));
        }

        return volume;
    }

    public double getExclusiveHV(WfgHypervolumeFront front, int point) {
        double volume;

        volume = this.getInclusiveHV(front.getPoint(point));
        if (front.getNumberOfPoints() > point + 1) {
            this.makeDominatedBit(front, point);
            double v = this.getHV(this.fs[this.currentDeep - 1]);
            volume -= v;
            this.currentDeep--;
        }

        return volume;
    }

    public double getHV(WfgHypervolumeFront front) {
        double volume;
        front.sort(this.pointComparator);

        if (this.currentDimension == 2) {
            volume = this.get2DHV(front);
        } else {
            volume = 0.0;

            this.currentDimension--;
            int numberOfPoints = front.getNumberOfPoints();
            for (int i = numberOfPoints - 1; i >= 0; i--) {
                volume += Math.abs(front.getPoint(i).getValue(this.currentDimension) -
                        this.referencePoint.getValue(this.currentDimension)) *
                        this.getExclusiveHV(front, i);
            }
            this.currentDimension++;
        }

        return volume;
    }

    public void makeDominatedBit(WfgHypervolumeFront front, int p) {
        int z = front.getNumberOfPoints() - 1 - p;

        for (int i = 0; i < z; i++) {
            for (int j = 0; j < this.currentDimension; j++) {
                Point point1 = front.getPoint(p);
                Point point2 = front.getPoint(p + 1 + i);
                double worseValue = this.worse(point1.getValue(j), point2.getValue(j), false);
                Point point3 = this.fs[this.currentDeep].getPoint(i);
                point3.setValue(j, worseValue);
            }
        }

        Point t;
        this.fs[this.currentDeep].setNumberOfPoints(1);

        for (int i = 1; i < z; i++) {
            int j = 0;
            boolean keep = true;
            while (j < this.fs[this.currentDeep].getNumberOfPoints() && keep) {
                switch (this.dominates2way(this.fs[this.currentDeep].getPoint(i), this.fs[this.currentDeep].getPoint(j))) {
                    case -1:
                        t = this.fs[this.currentDeep].getPoint(j);
                        this.fs[this.currentDeep].setNumberOfPoints(this.fs[this.currentDeep].getNumberOfPoints() - 1);
                        this.fs[this.currentDeep].setPoint(j,
                                this.fs[this.currentDeep].getPoint(this.fs[this.currentDeep].getNumberOfPoints()));
                        this.fs[this.currentDeep].setPoint(this.fs[this.currentDeep].getNumberOfPoints(), t);
                        break;
                    case 0:
                        j++;
                        break;
                    default:
                        keep = false;
                        break;
                }
            }
            if (keep) {
                t = this.fs[this.currentDeep].getPoint(this.fs[this.currentDeep].getNumberOfPoints());
                this.fs[this.currentDeep].setPoint(this.fs[this.currentDeep].getNumberOfPoints(), this.fs[this.currentDeep].getPoint(i));
                this.fs[this.currentDeep].setPoint(i, t);
                this.fs[this.currentDeep].setNumberOfPoints(this.fs[this.currentDeep].getNumberOfPoints() + 1);
            }
        }

        this.currentDeep++;
    }

    public int getLessContributorHV(List<Solution<?>> solutionList) {
        WfgHypervolumeFront wholeFront = (WfgHypervolumeFront) this.loadFront(solutionList, -1);

        int index = 0;
        double contribution = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solutionList.size(); i++) {
            double[] v = new double[solutionList.get(i).getNumberOfObjectives()];
            for (int j = 0; j < v.length; j++) {
                v[j] = solutionList.get(i).getObjective(j);
            }

            double aux = this.getExclusiveHV(wholeFront, i);
            if ((aux) < contribution) {
                index = i;
                contribution = aux;
            }

            HypervolumeContributionAttribute<Solution<?>> hvc = new HypervolumeContributionAttribute<>();
            hvc.setAttribute(solutionList.get(i), aux);
            //solutionList.get(i).setCrowdingDistance(aux);
        }

        return index;
    }

    private Front loadFront(List<Solution<?>> solutionSet, int notLoadingIndex) {
        int numberOfPoints;
        if (notLoadingIndex >= 0 && notLoadingIndex < solutionSet.size()) {
            numberOfPoints = solutionSet.size() - 1;
        } else {
            numberOfPoints = solutionSet.size();
        }

        int dimensions = solutionSet.get(0).getNumberOfObjectives();

        Front front = new WfgHypervolumeFront(numberOfPoints, dimensions);

        int index = 0;
        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != notLoadingIndex) {
                Point point = new ArrayPoint(dimensions);
                for (int j = 0; j < dimensions; j++) {
                    point.setValue(j, solutionSet.get(i).getObjective(j));
                }
                front.setPoint(index++, point);
            }
        }

        return front;
    }

    private double worse(double x, double y, boolean maximizing) {
        double result;
        if (maximizing) {
            if (x > y) {
                result = y;
            } else {
                result = x;
            }
        } else {
            if (x > y) {
                result = x;
            } else {
                result = y;
            }
        }
        return result;
    }

    int dominates2way(Point p, Point q) {
        // returns -1 if p dominates q, 1 if q dominates p, 2 if p == q, 0 otherwise
        // ASSUMING MINIMIZATION

        // domination could be checked in either order

        for (int i = this.currentDimension - 1; i >= 0; i--) {
            if (p.getValue(i) < q.getValue(i)) {
                for (int j = i - 1; j >= 0; j--) {
                    if (q.getValue(j) < p.getValue(j)) {
                        return 0;
                    }
                }
                return -1;
            } else if (q.getValue(i) < p.getValue(i)) {
                for (int j = i - 1; j >= 0; j--) {
                    if (p.getValue(j) < q.getValue(j)) {
                        return 0;
                    }
                }
                return 1;
            }
        }
        return 2;
    }
}
