package osh.datatypes.power;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.datatypes.commodity.AncillaryCommodity;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;

public class SimulatedSortedMap implements SortedMap<Long, LoadProfile<AncillaryCommodity>.Tick>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6471030830793159169L;

    private final ObjectArrayList<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>> entries
            = new ObjectArrayList<>();

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public LoadProfile<AncillaryCommodity>.Tick get(Object key) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public LoadProfile<AncillaryCommodity>.Tick put(Long key, LoadProfile<AncillaryCommodity>.Tick value) {
        this.entries.add(new SimpleEntry<>(key, value));
        return null;
    }

    @Override
    public LoadProfile<AncillaryCommodity>.Tick remove(Object key) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public void putAll(Map<? extends Long, ? extends LoadProfile<AncillaryCommodity>.Tick> m) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public void clear() {
        this.entries.clear();
    }

    @Override
    public Comparator<? super Long> comparator() {
        return null;
    }

    @Override
    public SortedMap<Long, LoadProfile<AncillaryCommodity>.Tick> subMap(Long fromKey, Long toKey) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public SortedMap<Long, LoadProfile<AncillaryCommodity>.Tick> headMap(Long toKey) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public SortedMap<Long, LoadProfile<AncillaryCommodity>.Tick> tailMap(Long fromKey) {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public Long firstKey() {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public Long lastKey() {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public Set<Long> keySet() {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public Collection<LoadProfile<AncillaryCommodity>.Tick> values() {
        throw new IllegalArgumentException("illegal method call");
    }

    @Override
    public Set<java.util.Map.Entry<Long, LoadProfile<AncillaryCommodity>.Tick>> entrySet() {
        return new EntrySet();
    }


    private class EntrySet implements Set<java.util.Map.Entry<Long, LoadProfile<AncillaryCommodity>.Tick>>, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -1278972548600805359L;

        @Override
        public int size() {
            return SimulatedSortedMap.this.entries.size();
        }

        @Override
        public boolean isEmpty() {
            return SimulatedSortedMap.this.entries.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public Iterator<java.util.Map.Entry<Long, LoadProfile<AncillaryCommodity>.Tick>> iterator() {
            return SimulatedSortedMap.this.entries.iterator();
        }

        @Override
        public Object[] toArray() {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public boolean add(java.util.Map.Entry<Long, LoadProfile<AncillaryCommodity>.Tick> e) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public boolean remove(Object o) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public boolean addAll(Collection<? extends java.util.Map.Entry<Long, LoadProfile<AncillaryCommodity>.Tick>> c) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new IllegalArgumentException("illegal method call");
        }

        @Override
        public void clear() {
            SimulatedSortedMap.this.entries.clear();
        }
    }
}
