package org.uma.jmetal.util.point.util.distance;

import org.uma.jmetal.util.point.Point;

/**
 * Interface representing classes for computing a distance between two points
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface PointDistance {
    double compute(Point pointA, Point pointB);
}
