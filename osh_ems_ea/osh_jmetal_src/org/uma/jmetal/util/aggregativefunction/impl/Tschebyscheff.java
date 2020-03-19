package org.uma.jmetal.util.aggregativefunction.impl;

import org.uma.jmetal.util.aggregativefunction.AggregativeFunction;
import org.uma.jmetal.util.point.impl.IdealPoint;

public class Tschebyscheff implements AggregativeFunction {
    private IdealPoint idealPoint;

    public Tschebyscheff() {
        this.idealPoint = null;
    }

    @Override
    public double compute(double[] vector, double[] weightVector) {
        double maxFun = -1.0e+30;

        for (int n = 0; n < vector.length; n++) {
            double diff = Math.abs(vector[n] - this.idealPoint.getValue(n));

            double feval;
            if (weightVector[n] == 0) {
                feval = 0.0001 * diff;
            } else {
                feval = diff * weightVector[n];
            }
            if (feval > maxFun) {
                maxFun = feval;
            }
        }

        return maxFun;
    }


    @Override
    public void update(double[] vector) {
        if (this.idealPoint == null) {
            this.idealPoint = new IdealPoint(vector.length);
        }
        this.idealPoint.update(vector);
    }
}
