package org.uma.jmetal.solution;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface representing a Solution
 *
 * @param <T> Type (Double, Integer, etc.)
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface Solution<T> extends Serializable {
    void setObjective(int index, double value);

    double getObjective(int index);

    double[] getObjectives();

    T getVariableValue(int index);

    List<T> getVariables();

    void setVariableValue(int index, T value);

    String getVariableValueString(int index);

    int getNumberOfVariables();

    int getNumberOfObjectives();

    Solution<T> copy();

    void setAttribute(Object id, Object value);

    Object getAttribute(Object id);

    Map<Object, Object> getAttributes();
}
