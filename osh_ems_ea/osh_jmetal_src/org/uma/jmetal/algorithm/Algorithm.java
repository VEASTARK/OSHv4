package org.uma.jmetal.algorithm;

import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.util.naming.DescribedEntity;

import java.io.Serializable;
import java.util.List;

/**
 * Interface representing an algorithm
 *
 * @param <Result> Result
 * @author Antonio J. Nebro
 * @version 0.1
 */
public interface Algorithm<Result> extends Runnable, Serializable, DescribedEntity {
    void run();

    Result getResult();

    List<StoppingRule> getStoppingRules();

    default void addStoppingRule(StoppingRule stoppingRule) {
        getStoppingRules().add(stoppingRule);
    }
}