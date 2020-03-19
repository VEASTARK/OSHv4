package org.uma.jmetal.util.aggregativefunction.impl;

import org.uma.jmetal.util.aggregativefunction.AggregativeFunction;
import org.uma.jmetal.util.point.impl.IdealPoint;

public class PenaltyBoundaryIntersection implements AggregativeFunction {
    private IdealPoint idealPoint;

    public PenaltyBoundaryIntersection() {
        this.idealPoint = null;
    }

    @Override
    public double compute(double[] vector, double[] weightVector) {
        double d1, d2, nl;
        double theta = 5.0;

        d1 = d2 = nl = 0.0;

        for (int i = 0; i < vector.length; i++) {
            d1 += (vector[i] - this.idealPoint.getValue(i)) * weightVector[i];
            nl += Math.pow(weightVector[i], 2.0);
        }
        nl = Math.sqrt(nl);
        d1 = Math.abs(d1) / nl;

        for (int i = 0; i < vector.length; i++) {
            d2 += Math.pow((vector[i] - this.idealPoint.getValue(i)) -
                    d1 * (weightVector[i] / nl), 2.0);
        }
        d2 = Math.sqrt(d2);

        return (d1 + theta * d2);
    }

    @Override
    public void update(double[] vector) {
        if (this.idealPoint == null) {
            this.idealPoint = new IdealPoint(vector.length);
        }
        this.idealPoint.update(vector);
    }
}
