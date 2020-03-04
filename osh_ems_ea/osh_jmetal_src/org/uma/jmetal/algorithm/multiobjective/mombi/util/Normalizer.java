package org.uma.jmetal.algorithm.multiobjective.mombi.util;

import java.util.List;

public class Normalizer {
    private final List<Double> min;
    private final List<Double> max;

    public Normalizer(List<Double> min, List<Double> max) {
        this.min = min;
        this.max = max;
    }

    public Double normalize(Double input, int index) {
        double diff = this.max.get(index) - this.min.get(index);
        return (input - this.min.get(index)) / diff;
    }
}