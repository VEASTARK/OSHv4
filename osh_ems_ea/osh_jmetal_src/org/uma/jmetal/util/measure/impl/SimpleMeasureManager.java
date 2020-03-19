package org.uma.jmetal.util.measure.impl;

import org.uma.jmetal.util.measure.Measure;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.PullMeasure;
import org.uma.jmetal.util.measure.PushMeasure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This {@link SimpleMeasureManager} provides a basic implementation to manage a
 * collection of {@link Measure}s. One can use the setXxxMeasure() methods to
 * configure the {@link MeasureManager} with the finest granularity, or exploit
 * the centralized {@link #setMeasure(Object, Measure)} to register a
 * {@link Measure} depending on the interfaces it implements, or even use the
 * massive {@link #setAllMeasures(Map)} to register a set of {@link Measure}s at
 * once. The corresponding removeXxx methods are also available for each case.
 * However, the only way to access a {@link Measure} is through the finest
 * granularity with {@link #getPullMeasure(Object)} and
 * {@link #getPushMeasure(Object)}.
 *
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 */
public class SimpleMeasureManager implements MeasureManager {

    /**
     * The {@link PullMeasure}s registered to this {@link SimpleMeasureManager}.
     */
    private final Map<Object, PullMeasure<?>> pullers = new HashMap<>();
    /**
     * The {@link PushMeasure}s registered to this {@link SimpleMeasureManager}.
     */
    private final Map<Object, PushMeasure<?>> pushers = new HashMap<>();

    /**
     * Provides the keys of all the {@link Measure}s which are supported by this
     * {@link SimpleMeasureManager}. If a key is provided, then at least one
     * version is available through {@link #getPullMeasure(Object)} or
     * {@link #getPushMeasure(Object)}.
     */
    @Override
    public Collection<Object> getMeasureKeys() {
        HashSet<Object> keys = new HashSet<>();
        keys.addAll(this.pullers.keySet());
        keys.addAll(this.pushers.keySet());
        return keys;
    }

    /**
     * @param key     the key of the {@link Measure}
     * @param measure the {@link PullMeasure} to register
     */
    public void setPullMeasure(Object key, PullMeasure<?> measure) {
        if (measure == null) {
            this.removePullMeasure(key);
        } else {
            this.pullers.put(key, measure);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PullMeasure<T> getPullMeasure(Object key) {
        return (PullMeasure<T>) this.pullers.get(key);
    }

    /**
     * @param key the key of the {@link PullMeasure} to remove
     */
    public void removePullMeasure(Object key) {
        this.pullers.remove(key);
    }

    /**
     * @param key     the key of the {@link Measure}
     * @param measure the {@link PushMeasure} to register
     */
    public void setPushMeasure(Object key, PushMeasure<?> measure) {
        if (measure == null) {
            this.removePushMeasure(key);
        } else {
            this.pushers.put(key, measure);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PushMeasure<T> getPushMeasure(Object key) {
        return (PushMeasure<T>) this.pushers.get(key);
    }

    /**
     * @param key the key of the {@link PushMeasure} to remove
     */
    public void removePushMeasure(Object key) {
        this.pushers.remove(key);
    }

    /**
     * This method call {@link #setPullMeasure(Object, PullMeasure)} or
     * {@link #setPushMeasure(Object, PushMeasure)} depending on the interfaces
     * implemented by the {@link Measure} given in argument. If both interfaces
     * are implemented, both methods are called, allowing to register all the
     * aspects of the {@link Measure} in one call.
     *
     * @param key     the key of the {@link Measure}
     * @param measure the {@link Measure} to register
     */
    public void setMeasure(Object key, Measure<?> measure) {
        if (measure instanceof PullMeasure) {
            this.setPullMeasure(key, (PullMeasure<?>) measure);
        }
        if (measure instanceof PushMeasure) {
            this.setPushMeasure(key, (PushMeasure<?>) measure);
        }
    }

    /**
     * This method removes an entire {@link Measure}, meaning that if both a
     * {@link PullMeasure} and a {@link PushMeasure} are registered for this
     * key, then both are removed.
     *
     * @param key the key of the {@link Measure} to remove
     */
    public void removeMeasure(Object key) {
        this.removePullMeasure(key);
        this.removePushMeasure(key);
    }

    /**
     * Massive equivalent of {@link #setMeasure(Object, Measure)}.
     *
     * @param measures the {@link Measure}s to register with their corresponding keys
     */
    public void setAllMeasures(
            Map<?, ? extends Measure<?>> measures) {
        for (Entry<?, ? extends Measure<?>> entry : measures
                .entrySet()) {
            this.setMeasure(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Massive equivalent to {@link #removeMeasure(Object)}.
     *
     * @param keys the keys of the {@link Measure}s to remove
     */
    public void removeAllMeasures(Iterable<?> keys) {
        for (Object key : keys) {
            this.removeMeasure(key);
        }
    }
}
