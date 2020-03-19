package org.uma.jmetal.util.naming.impl;

import org.uma.jmetal.util.naming.DescribedEntity;

import java.util.*;

public class DescribedEntitySet<Entity extends DescribedEntity> implements
        Set<Entity> {

    private final Map<String, Entity> map = new HashMap<>();

    @Override
    public boolean add(Entity e) {
        Entity stored = this.map.get(e.getName());
        if (stored == null) {
            this.map.put(e.getName(), e);
            return true;
        } else if (stored.equals(e)) {
            return false;
        } else {
            throw new IllegalArgumentException("Cannot add " + e
                    + ", conflicting name with " + stored);
        }
    }

    @Override
    public boolean addAll(Collection<? extends Entity> c) {
        boolean isModified = false;
        for (Entity entity : c) {
            isModified |= this.add(entity);
        }
        return isModified;
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity> E get(String name) {
        return (E) this.map.get(name);
    }

    @Override
    public boolean remove(Object o) {
        return this.map.values().remove(o);
    }

    public boolean remove(String name) {
        return this.map.keySet().remove(name);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.map.values().removeAll(c);
    }

    @Override
    public boolean contains(Object o) {
        return this.map.containsValue(o);
    }

    public boolean contains(String name) {
        return this.map.containsKey(name);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.map.values().containsAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.map.values().retainAll(c);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Iterator<Entity> iterator() {
        return this.map.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return this.map.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.map.values().toArray(a);
    }

    @Override
    public String toString() {
        TreeSet<String> displaySet = new TreeSet<>((s1, s2) -> {
            int comparison = s1.compareToIgnoreCase(s2);
            return comparison == 0 ? s1.compareTo(s2) : comparison;
        });
        displaySet.addAll(this.map.keySet());
        return displaySet.toString();
    }
}
