package osh.datatypes.cruisecontrol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @param <Value>
 * @author Till Schuberth
 */
public class OptimizedDataStorage<Value extends OptimizedDataStorage.EqualData<? super Value>> {

    private final TreeMap<Long, Value> storage = new TreeMap<>();

    public TreeMap<Long, Value> getMap() {
        return this.storage;
    }

    public void add(long timestamp, Value value) {
        this.storage.put(timestamp, value);

        // check if this data point is the same as the two before
        //  and throw out the middle one if possible
        if (this.storage.size() >= 3) {
            List<Entry<Long, Value>> lastEntries = new ArrayList<>(3);
            Iterator<Entry<Long, Value>> it = this.storage.descendingMap().entrySet().iterator();
            for (int i = 0; i < 3; i++) lastEntries.add(i, it.next());

            if (lastEntries.get(0).getValue().equalData(lastEntries.get(1).getValue()) && lastEntries.get(1).getValue().equalData(lastEntries.get(2).getValue())) {
                this.storage.remove(lastEntries.get(1).getKey());
            }
        }
    }

    public interface EqualData<T> {
        boolean equalData(T o);
    }

}
