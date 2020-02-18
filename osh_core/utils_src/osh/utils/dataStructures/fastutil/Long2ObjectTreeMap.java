package osh.utils.dataStructures.fastutil;

/*
 * Copyright (C) 2002-2017 Sebastiano Vigna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;

import java.util.*;

/**
 * An extension of the type-specific red-black tree map {@link Long2DoubleRBTreeMap} which provides some
 * functionality of {@link NavigableMap}.
 *
 * <p>
 * The iterators provided by the views of this class are type-specific
 * {@linkplain it.unimi.dsi.fastutil.BidirectionalIterator bidirectional
 * iterators}. Moreover, the iterator returned by {@code iterator()} can be
 * safely cast to a type-specific {@linkplain ListIterator list
 * iterator}.
 *
 */
public class Long2ObjectTreeMap<V> extends AbstractLong2ObjectSortedMap<V>
        implements
        java.io.Serializable,
        Cloneable {
    /** A reference to the root entry. */
    protected transient Entry<V> tree;
    /** Number of entries in this map. */
    protected int count;
    /** The first key in this map. */
    protected transient Entry<V> firstEntry;
    /** The last key in this map. */
    protected transient Entry<V> lastEntry;
    /** Cached set of entries. */
    protected transient ObjectSortedSet<Long2ObjectMap.Entry<V>> entries;
    /** Cached set of keys. */
    protected transient LongSortedSet keys;
    /** Cached collection of values. */
    protected transient ObjectCollection<V> values;
    /**
     * The value of this variable remembers, after a {@code put()} or a
     * {@code remove()}, whether the <em>domain</em> of the map has been modified.
     */
    protected transient boolean modified;
    /** This map's comparator, as provided in the constructor. */
    protected Comparator<? super Long> storedComparator;
    /**
     * This map's actual comparator; it may differ from {@link #storedComparator}
     * because it is always a type-specific comparator, so it could be derived from
     * the former by wrapping.
     */
    protected transient LongComparator actualComparator;
    public static final long INVALID_KEY = Long.MIN_VALUE;
    private static final long serialVersionUID = -7046029254386353129L;
    {
        this.allocatePaths();
    }
    /**
     * Creates a new empty tree map.
     */
    public Long2ObjectTreeMap() {
        this.tree = null;
        this.count = 0;
    }
    /**
     * Generates the comparator that will be actually used.
     *
     * <p>
     * When a given {@link Comparator} is specified and stored in
     * {@link #storedComparator}, we must check whether it is type-specific. If it
     * is so, we can used directly, and we store it in {@link #actualComparator}.
     * Otherwise, we adapt it using a helper static method.
     */
    private void setActualComparator() {
        this.actualComparator = LongComparators.asLongComparator(this.storedComparator);
    }
    /**
     * Creates a new empty tree map with the given comparator.
     *
     * @param c
     *            a (possibly type-specific) comparator.
     */
    public Long2ObjectTreeMap(final Comparator<? super Long> c) {
        this();
        this.storedComparator = c;
        this.setActualComparator();
    }
    /**
     * Creates a new tree map copying a given map.
     *
     * @param m
     *            a {@link Map} to be copied into the new tree map.
     */
    public Long2ObjectTreeMap(final Map<? extends Long, ? extends V> m) {
        this();
        this.putAll(m);
    }
    /**
     * Creates a new tree map copying a given sorted map (and its
     * {@link Comparator}).
     *
     * @param m
     *            a {@link SortedMap} to be copied into the new tree map.
     */
    public Long2ObjectTreeMap(final SortedMap<Long, V> m) {
        this(m.comparator());
        this.putAll(m);
    }
    /**
     * Creates a new tree map copying a given map.
     *
     * @param m
     *            a type-specific map to be copied into the new tree map.
     */
    public Long2ObjectTreeMap(final Long2ObjectMap<? extends V> m) {
        this();
        this.putAll(m);
    }
    /**
     * Creates a new tree map copying a given sorted map (and its
     * {@link Comparator}).
     *
     * @param m
     *            a type-specific sorted map to be copied into the new tree map.
     */
    public Long2ObjectTreeMap(final Long2ObjectSortedMap<V> m) {
        this(m.comparator());
        this.putAll(m);
    }
    /**
     * Creates a new tree map using the elements of two parallel arrays and the
     * given comparator.
     *
     * @param k
     *            the array of keys of the new tree map.
     * @param v
     *            the array of corresponding values in the new tree map.
     * @param c
     *            a (possibly type-specific) comparator.
     * @throws IllegalArgumentException
     *             if {@code k} and {@code v} have different lengths.
     */
    public Long2ObjectTreeMap(final long[] k, final V[] v, final Comparator<? super Long> c) {
        this(c);
        if (k.length != v.length)
            throw new IllegalArgumentException(
                    "The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
        for (int i = 0; i < k.length; i++)
            this.put(k[i], v[i]);
    }
    /**
     * Creates a new tree map using the elements of two parallel arrays.
     *
     * @param k
     *            the array of keys of the new tree map.
     * @param v
     *            the array of corresponding values in the new tree map.
     * @throws IllegalArgumentException
     *             if {@code k} and {@code v} have different lengths.
     */
    public Long2ObjectTreeMap(final long[] k, final V[] v) {
        this(k, v, null);
    }
    /*
     * The following methods implements some basic building blocks used by all
     * accessors. They are (and should be maintained) identical to those used in
     * RBTreeSet.drv.
     *
     * The put()/remove() code is derived from Ben Pfaff's GNU libavl
     * (http://www.msu.edu/~pfaffben/avl/). If you want to understand what's going
     * on, you should have a look at the literate code contained therein first.
     */
    /**
     * Compares two keys in the right way.
     *
     * <p>
     * This method uses the {@link #actualComparator} if it is non-{@code null}.
     * Otherwise, it resorts to primitive type comparisons or to
     * {@link Comparable#compareTo(Object) compareTo()}.
     *
     * @param k1
     *            the first key.
     * @param k2
     *            the second key.
     * @return a number smaller than, equal to or greater than 0, as usual (i.e.,
     *         when k1 &lt; k2, k1 = k2 or k1 &gt; k2, respectively).
     */

    final int compare(final long k1, final long k2) {
        return this.actualComparator == null ? (Long.compare((k1), (k2))) : this.actualComparator.compare(k1, k2);
    }
    /**
     * Returns the entry corresponding to the given key, if it is in the tree;
     * {@code null}, otherwise.
     *
     * @param k
     *            the key to search for.
     * @return the corresponding entry, or {@code null} if no entry with the given
     *         key exists.
     */
    final Entry<V> findKey(final long k) {
        Entry<V> e = this.tree;
        int cmp;
        while (e != null && (cmp = this.compare(k, e.key)) != 0)
            e = cmp < 0 ? e.left() : e.right();
        return e;
    }
    /**
     * Locates a key.
     *
     * @param k
     *            a key.
     * @return the last entry on a search for the given key; this will be the given
     *         key, if it present; otherwise, it will be either the smallest greater
     *         key or the greatest smaller key.
     */
    final Entry<V> locateKey(final long k) {
        Entry<V> e = this.tree, last = this.tree;
        int cmp = 0;
        while (e != null && (cmp = this.compare(k, e.key)) != 0) {
            last = e;
            e = cmp < 0 ? e.left() : e.right();
        }
        return cmp == 0 ? e : last;
    }

    /**
     * This vector remembers the path and the direction followed during the current
     * insertion. It suffices for about 2<sup>32</sup> entries.
     */
    private transient boolean[] dirPath;
    private transient Entry<V>[] nodePath;
    @SuppressWarnings("unchecked")
    private void allocatePaths() {
        this.dirPath = new boolean[64];
        this.nodePath = new Entry[64];
    }
    @Override
    public V put(final long k, final V v) {
        Entry<V> e = this.add(k);
        final V oldValue = e.value;
        e.value = v;
        return oldValue;
    }
    /**
     * Returns a node with key k in the balanced tree, creating one with defRetValue
     * if necessary.
     *
     * @param k
     *            the key
     * @return a node with key k. If a node with key k already exists, then that
     *         node is returned, otherwise a new node with defRetValue is created
     *         ensuring that the tree is balanced after creation of the node.
     */
    private Entry<V> add(final long k) {
        /*
         * After execution of this method, modified is true iff a new entry has been
         * inserted.
         */
        this.modified = false;
        int maxDepth = 0;
        Entry<V> e;
        if (this.tree == null) { // The case of the empty tree is treated separately.
            this.count++;
            e = this.tree = this.lastEntry = this.firstEntry = new Entry<>(k, this.defRetValue);
        } else {
            Entry<V> p = this.tree;
            int cmp, i = 0;
            while (true) {
                if ((cmp = this.compare(k, p.key)) == 0) {
                    // We clean up the node path, or we could have stale references later.
                    while (i-- != 0)
                        this.nodePath[i] = null;
                    return p;
                }
                this.nodePath[i] = p;
                if (this.dirPath[i++] = cmp > 0) {
                    if (p.succ()) {
                        this.count++;
                        e = new Entry<>(k, this.defRetValue);
                        if (p.right == null)
                            this.lastEntry = e;
                        e.left = p;
                        e.right = p.right;
                        p.right(e);
                        break;
                    }
                    p = p.right;
                } else {
                    if (p.pred()) {
                        this.count++;
                        e = new Entry<>(k, this.defRetValue);
                        if (p.left == null)
                            this.firstEntry = e;
                        e.right = p;
                        e.left = p.left;
                        p.left(e);
                        break;
                    }
                    p = p.left;
                }
            }
            this.modified = true;
            maxDepth = i--;
            while (i > 0 && !this.nodePath[i].black()) {
                if (!this.dirPath[i - 1]) {
                    Entry<V> y = this.nodePath[i - 1].right;
                    if (!this.nodePath[i - 1].succ() && !y.black()) {
                        this.nodePath[i].black(true);
                        y.black(true);
                        this.nodePath[i - 1].black(false);
                        i -= 2;
                    } else {
                        Entry<V> x;
                        if (!this.dirPath[i])
                            y = this.nodePath[i];
                        else {
                            x = this.nodePath[i];
                            y = x.right;
                            x.right = y.left;
                            y.left = x;
                            this.nodePath[i - 1].left = y;
                            if (y.pred()) {
                                y.pred(false);
                                x.succ(y);
                            }
                        }
                        x = this.nodePath[i - 1];
                        x.black(false);
                        y.black(true);
                        x.left = y.right;
                        y.right = x;
                        if (i < 2)
                            this.tree = y;
                        else {
                            if (this.dirPath[i - 2])
                                this.nodePath[i - 2].right = y;
                            else
                                this.nodePath[i - 2].left = y;
                        }
                        if (y.succ()) {
                            y.succ(false);
                            x.pred(y);
                        }
                        break;
                    }
                } else {
                    Entry<V> y = this.nodePath[i - 1].left;
                    if (!this.nodePath[i - 1].pred() && !y.black()) {
                        this.nodePath[i].black(true);
                        y.black(true);
                        this.nodePath[i - 1].black(false);
                        i -= 2;
                    } else {
                        Entry<V> x;
                        if (this.dirPath[i])
                            y = this.nodePath[i];
                        else {
                            x = this.nodePath[i];
                            y = x.left;
                            x.left = y.right;
                            y.right = x;
                            this.nodePath[i - 1].right = y;
                            if (y.succ()) {
                                y.succ(false);
                                x.pred(y);
                            }
                        }
                        x = this.nodePath[i - 1];
                        x.black(false);
                        y.black(true);
                        x.right = y.left;
                        y.left = x;
                        if (i < 2)
                            this.tree = y;
                        else {
                            if (this.dirPath[i - 2])
                                this.nodePath[i - 2].right = y;
                            else
                                this.nodePath[i - 2].left = y;
                        }
                        if (y.pred()) {
                            y.pred(false);
                            x.succ(y);
                        }
                        break;
                    }
                }
            }
        }
        this.tree.black(true);
        // We clean up the node path, or we could have stale references later.
        while (maxDepth-- != 0)
            this.nodePath[maxDepth] = null;
        return e;
    }
    /*
     * After execution of this method, {@link #modified} is true iff an entry has
     * been deleted.
     */

    @Override
    public V remove(final long k) {
        this.modified = false;
        if (this.tree == null)
            return this.defRetValue;
        Entry<V> p = this.tree;
        int cmp;
        int i = 0;
        final long kk = k;
        while (true) {
            if ((cmp = this.compare(kk, p.key)) == 0)
                break;
            this.dirPath[i] = cmp > 0;
            this.nodePath[i] = p;
            if (this.dirPath[i++]) {
                if ((p = p.right()) == null) {
                    // We clean up the node path, or we could have stale references later.
                    while (i-- != 0)
                        this.nodePath[i] = null;
                    return this.defRetValue;
                }
            } else {
                if ((p = p.left()) == null) {
                    // We clean up the node path, or we could have stale references later.
                    while (i-- != 0)
                        this.nodePath[i] = null;
                    return this.defRetValue;
                }
            }
        }
        if (p.left == null)
            this.firstEntry = p.next();
        if (p.right == null)
            this.lastEntry = p.prev();
        if (p.succ()) {
            if (p.pred()) {
                if (i == 0)
                    this.tree = p.left;
                else {
                    if (this.dirPath[i - 1])
                        this.nodePath[i - 1].succ(p.right);
                    else
                        this.nodePath[i - 1].pred(p.left);
                }
            } else {
                p.prev().right = p.right;
                if (i == 0)
                    this.tree = p.left;
                else {
                    if (this.dirPath[i - 1])
                        this.nodePath[i - 1].right = p.left;
                    else
                        this.nodePath[i - 1].left = p.left;
                }
            }
        } else {
            boolean color;
            Entry<V> r = p.right;
            if (r.pred()) {
                r.left = p.left;
                r.pred(p.pred());
                if (!r.pred())
                    r.prev().right = r;
                if (i == 0)
                    this.tree = r;
                else {
                    if (this.dirPath[i - 1])
                        this.nodePath[i - 1].right = r;
                    else
                        this.nodePath[i - 1].left = r;
                }
                color = r.black();
                r.black(p.black());
                p.black(color);
                this.dirPath[i] = true;
                this.nodePath[i++] = r;
            } else {
                Entry<V> s;
                int j = i++;
                while (true) {
                    this.dirPath[i] = false;
                    this.nodePath[i++] = r;
                    s = r.left;
                    if (s.pred())
                        break;
                    r = s;
                }
                this.dirPath[j] = true;
                this.nodePath[j] = s;
                if (s.succ())
                    r.pred(s);
                else
                    r.left = s.right;
                s.left = p.left;
                if (!p.pred()) {
                    p.prev().right = s;
                    s.pred(false);
                }
                s.right(p.right);
                color = s.black();
                s.black(p.black());
                p.black(color);
                if (j == 0)
                    this.tree = s;
                else {
                    if (this.dirPath[j - 1])
                        this.nodePath[j - 1].right = s;
                    else
                        this.nodePath[j - 1].left = s;
                }
            }
        }
        int maxDepth = i;
        if (p.black()) {
            for (; i > 0; i--) {
                if (this.dirPath[i - 1] && !this.nodePath[i - 1].succ() || !this.dirPath[i - 1] && !this.nodePath[i - 1].pred()) {
                    Entry<V> x = this.dirPath[i - 1] ? this.nodePath[i - 1].right : this.nodePath[i - 1].left;
                    if (!x.black()) {
                        x.black(true);
                        break;
                    }
                }
                if (!this.dirPath[i - 1]) {
                    Entry<V> w = this.nodePath[i - 1].right;
                    if (!w.black()) {
                        w.black(true);
                        this.nodePath[i - 1].black(false);
                        this.nodePath[i - 1].right = w.left;
                        w.left = this.nodePath[i - 1];
                        if (i < 2)
                            this.tree = w;
                        else {
                            if (this.dirPath[i - 2])
                                this.nodePath[i - 2].right = w;
                            else
                                this.nodePath[i - 2].left = w;
                        }
                        this.nodePath[i] = this.nodePath[i - 1];
                        this.dirPath[i] = false;
                        this.nodePath[i - 1] = w;
                        if (maxDepth == i++)
                            maxDepth++;
                        w = this.nodePath[i - 1].right;
                    }
                    if ((w.pred() || w.left.black()) && (w.succ() || w.right.black())) {
                        w.black(false);
                    } else {
                        if (w.succ() || w.right.black()) {
                            Entry<V> y = w.left;
                            y.black(true);
                            w.black(false);
                            w.left = y.right;
                            y.right = w;
                            w = this.nodePath[i - 1].right = y;
                            if (w.succ()) {
                                w.succ(false);
                                w.right.pred(w);
                            }
                        }
                        w.black(this.nodePath[i - 1].black());
                        this.nodePath[i - 1].black(true);
                        w.right.black(true);
                        this.nodePath[i - 1].right = w.left;
                        w.left = this.nodePath[i - 1];
                        if (i < 2)
                            this.tree = w;
                        else {
                            if (this.dirPath[i - 2])
                                this.nodePath[i - 2].right = w;
                            else
                                this.nodePath[i - 2].left = w;
                        }
                        if (w.pred()) {
                            w.pred(false);
                            this.nodePath[i - 1].succ(w);
                        }
                        break;
                    }
                } else {
                    Entry<V> w = this.nodePath[i - 1].left;
                    if (!w.black()) {
                        w.black(true);
                        this.nodePath[i - 1].black(false);
                        this.nodePath[i - 1].left = w.right;
                        w.right = this.nodePath[i - 1];
                        if (i < 2)
                            this.tree = w;
                        else {
                            if (this.dirPath[i - 2])
                                this.nodePath[i - 2].right = w;
                            else
                                this.nodePath[i - 2].left = w;
                        }
                        this.nodePath[i] = this.nodePath[i - 1];
                        this.dirPath[i] = true;
                        this.nodePath[i - 1] = w;
                        if (maxDepth == i++)
                            maxDepth++;
                        w = this.nodePath[i - 1].left;
                    }
                    if ((w.pred() || w.left.black()) && (w.succ() || w.right.black())) {
                        w.black(false);
                    } else {
                        if (w.pred() || w.left.black()) {
                            Entry<V> y = w.right;
                            y.black(true);
                            w.black(false);
                            w.right = y.left;
                            y.left = w;
                            w = this.nodePath[i - 1].left = y;
                            if (w.pred()) {
                                w.pred(false);
                                w.left.succ(w);
                            }
                        }
                        w.black(this.nodePath[i - 1].black());
                        this.nodePath[i - 1].black(true);
                        w.left.black(true);
                        this.nodePath[i - 1].left = w.right;
                        w.right = this.nodePath[i - 1];
                        if (i < 2)
                            this.tree = w;
                        else {
                            if (this.dirPath[i - 2])
                                this.nodePath[i - 2].right = w;
                            else
                                this.nodePath[i - 2].left = w;
                        }
                        if (w.succ()) {
                            w.succ(false);
                            this.nodePath[i - 1].pred(w);
                        }
                        break;
                    }
                }
            }
            if (this.tree != null)
                this.tree.black(true);
        }
        this.modified = true;
        this.count--;
        // We clean up the node path, or we could have stale references later.
        while (maxDepth-- != 0)
            this.nodePath[maxDepth] = null;
        return p.value;
    }
    @Override
    public boolean containsValue(final Object v) {
        final ValueIterator i = new ValueIterator();
        Object ev;
        int j = this.count;
        while (j-- != 0) {
            ev = i.next();
            if (Objects.equals(ev, v))
                return true;
        }
        return false;
    }
    @Override
    public void clear() {
        this.count = 0;
        this.tree = null;
        this.entries = null;
        this.values = null;
        this.keys = null;
        this.firstEntry = this.lastEntry = null;
    }
    /**
     * This class represent an entry in a tree map.
     *
     * <p>
     * We use the only "metadata", i.e., {@link Entry#info}, to store information
     * about color, predecessor status and successor status.
     *
     * <p>
     * Note that since the class is recursive, it can be considered equivalently a
     * tree.
     */
    private static final class Entry<V> extends BasicEntry<V> implements Cloneable {
        /** The the bit in this mask is true, the node is black. */
        private static final int BLACK_MASK = 1;
        /** If the bit in this mask is true, {@link #right} points to a successor. */
        private static final int SUCC_MASK = 1 << 31;
        /** If the bit in this mask is true, {@link #left} points to a predecessor. */
        private static final int PRED_MASK = 1 << 30;
        /** The pointers to the left and right subtrees. */
        Entry<V> left, right;
        /**
         * This integers holds different information in different bits (see
         * {@link #SUCC_MASK} and {@link #PRED_MASK}.
         */
        int info;
        Entry() {
            super((0), (null));
        }
        /**
         * Creates a new entry with the given key and value.
         *
         * @param k
         *            a key.
         * @param v
         *            a value.
         */
        Entry(final long k, final V v) {
            super(k, v);
            this.info = SUCC_MASK | PRED_MASK;
        }
        /**
         * Returns the left subtree.
         *
         * @return the left subtree ({@code null} if the left subtree is empty).
         */
        Entry<V> left() {
            return (this.info & PRED_MASK) != 0 ? null : this.left;
        }
        /**
         * Returns the right subtree.
         *
         * @return the right subtree ({@code null} if the right subtree is empty).
         */
        Entry<V> right() {
            return (this.info & SUCC_MASK) != 0 ? null : this.right;
        }
        /**
         * Checks whether the left pointer is really a predecessor.
         *
         * @return true if the left pointer is a predecessor.
         */
        boolean pred() {
            return (this.info & PRED_MASK) != 0;
        }
        /**
         * Checks whether the right pointer is really a successor.
         *
         * @return true if the right pointer is a successor.
         */
        boolean succ() {
            return (this.info & SUCC_MASK) != 0;
        }
        /**
         * Sets whether the left pointer is really a predecessor.
         *
         * @param pred
         *            if true then the left pointer will be considered a predecessor.
         */
        void pred(final boolean pred) {
            if (pred)
                this.info |= PRED_MASK;
            else
                this.info &= ~PRED_MASK;
        }
        /**
         * Sets whether the right pointer is really a successor.
         *
         * @param succ
         *            if true then the right pointer will be considered a successor.
         */
        void succ(final boolean succ) {
            if (succ)
                this.info |= SUCC_MASK;
            else
                this.info &= ~SUCC_MASK;
        }
        /**
         * Sets the left pointer to a predecessor.
         *
         * @param pred
         *            the predecessr.
         */
        void pred(final Entry<V> pred) {
            this.info |= PRED_MASK;
            this.left = pred;
        }
        /**
         * Sets the right pointer to a successor.
         *
         * @param succ
         *            the successor.
         */
        void succ(final Entry<V> succ) {
            this.info |= SUCC_MASK;
            this.right = succ;
        }
        /**
         * Sets the left pointer to the given subtree.
         *
         * @param left
         *            the new left subtree.
         */
        void left(final Entry<V> left) {
            this.info &= ~PRED_MASK;
            this.left = left;
        }
        /**
         * Sets the right pointer to the given subtree.
         *
         * @param right
         *            the new right subtree.
         */
        void right(final Entry<V> right) {
            this.info &= ~SUCC_MASK;
            this.right = right;
        }
        /**
         * Returns whether this node is black.
         *
         * @return true iff this node is black.
         */
        boolean black() {
            return (this.info & BLACK_MASK) != 0;
        }
        /**
         * Sets whether this node is black.
         *
         * @param black
         *            if true, then this node becomes black; otherwise, it becomes red..
         */
        void black(final boolean black) {
            if (black)
                this.info |= BLACK_MASK;
            else
                this.info &= ~BLACK_MASK;
        }
        /**
         * Computes the next entry in the set order.
         *
         * @return the next entry ({@code null}) if this is the last entry).
         */
        Entry<V> next() {
            Entry<V> next = this.right;
            if ((this.info & SUCC_MASK) == 0)
                while ((next.info & PRED_MASK) == 0)
                    next = next.left;
            return next;
        }
        /**
         * Computes the previous entry in the set order.
         *
         * @return the previous entry ({@code null}) if this is the first entry).
         */
        Entry<V> prev() {
            Entry<V> prev = this.left;
            if ((this.info & PRED_MASK) == 0)
                while ((prev.info & SUCC_MASK) == 0)
                    prev = prev.right;
            return prev;
        }
        @Override
        public V setValue(final V value) {
            final V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        @Override
        @SuppressWarnings("unchecked")
        public Entry<V> clone() {
            Entry<V> c;
            try {
                c = (Entry<V>) super.clone();
            } catch (CloneNotSupportedException cantHappen) {
                throw new InternalError();
            }
            c.key = this.key;
            c.value = this.value;
            c.info = this.info;
            return c;
        }
        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<Long, V> e = (Map.Entry<Long, V>) o;
            return ((this.key) == ((e.getKey()).longValue())) && Objects.equals(this.value, (e.getValue()));
        }
        @Override
        public int hashCode() {
            return it.unimi.dsi.fastutil.HashCommon.long2int(this.key) ^ ((this.value) == null ? 0 : (this.value).hashCode());
        }
        @Override
        public String toString() {
            return this.key + "=>" + this.value;
        }
        /*
         * public void prettyPrint() { prettyPrint(0); }
         *
         * public void prettyPrint(int level) { if (pred()) { for (int i = 0; i < level;
         * i++) System.err.print("  "); System.err.println("pred: " + left); } else if
         * (left != null) left.prettyPrint(level +1); for (int i = 0; i < level; i++)
         * System.err.print("  "); System.err.println(key + "=" + value + " (" +
         * balance() + ")"); if (succ()) { for (int i = 0; i < level; i++)
         * System.err.print("  "); System.err.println("succ: " + right); } else if
         * (right != null) right.prettyPrint(level + 1); }
         */
    }
    /*
     * public void prettyPrint() { System.err.println("size: " + count); if (tree !=
     * null) tree.prettyPrint(); }
     */

    /**
     * This class provides a basic but complete type-specific entry class for all
     * those maps implementations that do not have entries on their own (e.g., most
     * immutable maps).
     *
     * <p>
     * This class does not implement {@link Map.Entry#setValue(Object)
     * setValue()}, as the modification would not be reflected in the base map.
     */
    public static class BasicEntry<K> implements Long2ObjectMap.Entry<K> {
        protected long key;
        protected K value;
        public BasicEntry() {
        }
        public BasicEntry(final Long key, final K value) {
            this.key = key;
            this.value = (value);
        }
        public BasicEntry(final long key, final K value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public long getLongKey() {
            return this.key;
        }
        @Override
        public K getValue() {
            return this.value;
        }
        @Override
        public K setValue(final K value) {
            throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            if (o instanceof Long2ObjectMap.Entry) {
                final Long2ObjectMap.Entry<K> e = (Long2ObjectMap.Entry<K>) o;
                return ((this.key) == (e.getLongKey())) && Objects.equals(this.value, e.getValue());
            }
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final Object key = e.getKey();
            if (key == null || !(key instanceof Long))
                return false;
            final Object value = e.getValue();
            return ((this.key) == (((Long) (key)).longValue())) && Objects.equals(this.value, (value));
        }
        @Override
        public int hashCode() {
            return it.unimi.dsi.fastutil.HashCommon.long2int(this.key) ^ ((this.value) == null ? 0 : (this.value).hashCode());
        }
        @Override
        public String toString() {
            return this.key + "->" + this.value;
        }
    }

    @Override
    public boolean containsKey(final long k) {
        return this.findKey(k) != null;
    }
    @Override
    public int size() {
        return this.count;
    }
    @Override
    public boolean isEmpty() {
        return this.count == 0;
    }

    @Override
    public V get(final long k) {
        final Entry<V> e = this.findKey(k);
        return e == null ? this.defRetValue : e.value;
    }
    @Override
    public long firstLongKey() {
        if (this.tree == null)
            throw new NoSuchElementException();
        return this.firstEntry.key;
    }
    @Override
    public long lastLongKey() {
        if (this.tree == null)
            throw new NoSuchElementException();
        return this.lastEntry.key;
    }

    public final long floorKey(long key) {
        Entry en = this.locateKey(key);
        if (en.key > key) {
            Entry prev = en.prev();
            return prev == null ? INVALID_KEY : prev.key;
        } else {
            return en.key;
        }
    }

    public final long ceilingKey(long key) {
        Entry en = this.locateKey(key);
        if (en.key < key) {
            Entry next = en.next();
            return next == null ? INVALID_KEY : next.key;
        } else {
            return en.key;
        }
    }

    public final long lowerKey(long key) {
        Entry en = this.locateKey(key);
        if (en.key >= key) {
            Entry prev = en.prev();
            return prev == null ? INVALID_KEY : prev.key;
        } else {
            return en.key;
        }
    }

    public final long higherKey(long key) {
        Entry en = this.locateKey(key);
        if (en.key <= key) {
            Entry next = en.next();
            return next == null ? INVALID_KEY : next.key;
        } else {
            return en.key;
        }
    }

    public final BasicEntry<V> floorEntry(long key) {
        if (this.count == 0) return null;
        Entry<V> en = this.locateKey(key);
        if (en.key > key) {
            return en.prev();
        } else {
            return en;
        }

    }

    public final BasicEntry<V> ceilingEntry(long key) {
        if (this.count == 0) return null;
        Entry<V> en = this.locateKey(key);
        if (en.key < key) {
            return en.next();
        } else {
            return en;
        }
    }

    public final BasicEntry<V> lowerEntry(long key) {
        if (this.count == 0) return null;
        Entry<V> en = this.locateKey(key);
        if (en.key >= key) {
            return en.prev();
        } else {
            return en;
        }

    }

    public final BasicEntry<V> higherEntry(long key) {
        if (this.count == 0) return null;
        Entry<V> en = this.locateKey(key);
        if (en.key <= key) {
            return en.next();
        } else {
            return en;
        }
    }


    /**
     * An abstract iterator on the whole range.
     *
     * <p>
     * This class can iterate in both directions on a threaded tree.
     */
    private class TreeIterator {
        /**
         * The entry that will be returned by the next call to
         * {@link ListIterator#previous()} (or {@code null} if no previous
         * entry exists).
         */
        Entry<V> prev;
        /**
         * The entry that will be returned by the next call to
         * {@link ListIterator#next()} (or {@code null} if no next entry
         * exists).
         */
        Entry<V> next;
        /**
         * The last entry that was returned (or {@code null} if we did not iterate or
         * used {@link #remove()}).
         */
        Entry<V> curr;
        /**
         * The current index (in the sense of a {@link ListIterator}). Note
         * that this value is not meaningful when this {@link TreeIterator} has been
         * created using the nonempty constructor.
         */
        int index = 0;
        TreeIterator() {
            this.next = Long2ObjectTreeMap.this.firstEntry;
        }
        TreeIterator(final long k) {
            if ((this.next = Long2ObjectTreeMap.this.locateKey(k)) != null) {
                if (Long2ObjectTreeMap.this.compare(this.next.key, k) <= 0) {
                    this.prev = this.next;
                    this.next = this.next.next();
                } else
                    this.prev = this.next.prev();
            }
        }
        public boolean hasNext() {
            return this.next != null;
        }
        public boolean hasPrevious() {
            return this.prev != null;
        }
        void updateNext() {
            this.next = this.next.next();
        }
        Entry<V> nextEntry() {
            if (!this.hasNext())
                throw new NoSuchElementException();
            this.curr = this.prev = this.next;
            this.index++;
            this.updateNext();
            return this.curr;
        }
        void updatePrevious() {
            this.prev = this.prev.prev();
        }
        Entry<V> previousEntry() {
            if (!this.hasPrevious())
                throw new NoSuchElementException();
            this.curr = this.next = this.prev;
            this.index--;
            this.updatePrevious();
            return this.curr;
        }
        public int nextIndex() {
            return this.index;
        }
        public int previousIndex() {
            return this.index - 1;
        }
        public void remove() {
            if (this.curr == null)
                throw new IllegalStateException();
            /*
             * If the last operation was a next(), we are removing an entry that preceeds
             * the current index, and thus we must decrement it.
             */
            if (this.curr == this.prev)
                this.index--;
            this.next = this.prev = this.curr;
            this.updatePrevious();
            this.updateNext();
            Long2ObjectTreeMap.this.remove(this.curr.key);
            this.curr = null;
        }
        public int skip(final int n) {
            int i = n;
            while (i-- != 0 && this.hasNext())
                this.nextEntry();
            return n - i - 1;
        }
        public int back(final int n) {
            int i = n;
            while (i-- != 0 && this.hasPrevious())
                this.previousEntry();
            return n - i - 1;
        }
    }
    /**
     * An iterator on the whole range.
     *
     * <p>
     * This class can iterate in both directions on a threaded tree.
     */
    private class EntryIterator extends TreeIterator implements ObjectListIterator<Long2ObjectMap.Entry<V>> {
        EntryIterator() {
        }
        EntryIterator(final long k) {
            super(k);
        }
        @Override
        public Long2ObjectMap.Entry<V> next() {
            return this.nextEntry();
        }
        @Override
        public Long2ObjectMap.Entry<V> previous() {
            return this.previousEntry();
        }
    }
    @Override
    public ObjectSortedSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
        if (this.entries == null)
            this.entries = new AbstractObjectSortedSet<Long2ObjectMap.Entry<V>>() {
                final Comparator<? super Long2ObjectMap.Entry<V>> comparator = (Comparator<Long2ObjectMap.Entry<V>>) (x,
                                                                                                                      y) -> Long2ObjectTreeMap.this.actualComparator.compare(x.getLongKey(), y.getLongKey());
                @Override
                public Comparator<? super Long2ObjectMap.Entry<V>> comparator() {
                    return this.comparator;
                }
                @Override
                public ObjectBidirectionalIterator<Long2ObjectMap.Entry<V>> iterator() {
                    return new EntryIterator();
                }
                @Override
                public ObjectBidirectionalIterator<Long2ObjectMap.Entry<V>> iterator(
                        final Long2ObjectMap.Entry<V> from) {
                    return new EntryIterator(from.getLongKey());
                }
                @Override

                public boolean contains(final Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                    if (e.getKey() == null || !(e.getKey() instanceof Long))
                        return false;
                    final Entry<V> f = Long2ObjectTreeMap.this.findKey(((Long) (e.getKey())).longValue());
                    return e.equals(f);
                }
                @Override

                public boolean remove(final Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                    if (e.getKey() == null || !(e.getKey() instanceof Long))
                        return false;
                    final Entry<V> f = Long2ObjectTreeMap.this.findKey(((Long) (e.getKey())).longValue());
                    if (f == null || !Objects.equals(f.getValue(), (e.getValue())))
                        return false;
                    Long2ObjectTreeMap.this.remove(f.key);
                    return true;
                }
                @Override
                public int size() {
                    return Long2ObjectTreeMap.this.count;
                }
                @Override
                public void clear() {
                    Long2ObjectTreeMap.this.clear();
                }
                @Override
                public Long2ObjectMap.Entry<V> first() {
                    return Long2ObjectTreeMap.this.firstEntry;
                }
                @Override
                public Long2ObjectMap.Entry<V> last() {
                    return Long2ObjectTreeMap.this.lastEntry;
                }
                @Override
                public ObjectSortedSet<Long2ObjectMap.Entry<V>> subSet(Long2ObjectMap.Entry<V> from,
                                                                       Long2ObjectMap.Entry<V> to) {
                    return Long2ObjectTreeMap.this.subMap(from.getLongKey(), to.getLongKey()).long2ObjectEntrySet();
                }
                @Override
                public ObjectSortedSet<Long2ObjectMap.Entry<V>> headSet(Long2ObjectMap.Entry<V> to) {
                    return Long2ObjectTreeMap.this.headMap(to.getLongKey()).long2ObjectEntrySet();
                }
                @Override
                public ObjectSortedSet<Long2ObjectMap.Entry<V>> tailSet(Long2ObjectMap.Entry<V> from) {
                    return Long2ObjectTreeMap.this.tailMap(from.getLongKey()).long2ObjectEntrySet();
                }
            };
        return this.entries;
    }
    /**
     * An iterator on the whole range of keys.
     *
     * <p>
     * This class can iterate in both directions on the keys of a threaded tree. We
     * simply override the
     * {@link ListIterator#next()}/{@link ListIterator#previous()}
     * methods (and possibly their type-specific counterparts) so that they return
     * keys instead of entries.
     */
    private final class KeyIterator extends TreeIterator implements LongListIterator {
        public KeyIterator() {
        }
        public KeyIterator(final long k) {
            super(k);
        }
        @Override
        public long nextLong() {
            return this.nextEntry().key;
        }
        @Override
        public long previousLong() {
            return this.previousEntry().key;
        }
    }

    /** A keyset implementation using a more direct implementation for iterators. */
    private class KeySet extends AbstractLong2ObjectSortedMap<V>.KeySet {
        @Override
        public LongBidirectionalIterator iterator() {
            return new KeyIterator();
        }
        @Override
        public LongBidirectionalIterator iterator(final long from) {
            return new KeyIterator(from);
        }
    }
    /**
     * Returns a type-specific sorted set view of the keys contained in this map.
     *
     * <p>
     * In addition to the semantics of {@link Map#keySet()}, you can
     * safely cast the set returned by this call to a type-specific sorted set
     * interface.
     *
     * @return a type-specific sorted set view of the keys contained in this map.
     */
    @Override
    public LongSortedSet keySet() {
        if (this.keys == null)
            this.keys = new KeySet();
        return this.keys;
    }
    /**
     * An iterator on the whole range of values.
     *
     * <p>
     * This class can iterate in both directions on the values of a threaded tree.
     * We simply override the
     * {@link ListIterator#next()}/{@link ListIterator#previous()}
     * methods (and possibly their type-specific counterparts) so that they return
     * values instead of entries.
     */
    private final class ValueIterator extends TreeIterator implements ObjectListIterator<V> {
        @Override
        public V next() {
            return (V) this.nextEntry().value;
        }
        @Override
        public V previous() {
            return (V) this.previousEntry().value;
        }
    }

    /**
     * Returns a type-specific collection view of the values contained in this map.
     *
     * <p>
     * In addition to the semantics of {@link Map#values()}, you can
     * safely cast the collection returned by this call to a type-specific
     * collection interface.
     *
     * @return a type-specific collection view of the values contained in this map.
     */
    @Override
    public ObjectCollection<V> values() {
        if (this.values == null)
            this.values = new AbstractObjectCollection<V>() {
                @Override
                public ObjectIterator<V> iterator() {
                    return new ValueIterator();
                }
                @Override
                public boolean contains(final Object k) {
                    return Long2ObjectTreeMap.this.containsValue(k);
                }
                @Override
                public int size() {
                    return Long2ObjectTreeMap.this.count;
                }
                @Override
                public void clear() {
                    Long2ObjectTreeMap.this.clear();
                }
            };
        return this.values;
    }
    @Override
    public LongComparator comparator() {
        return this.actualComparator;
    }
    @Override
    public Long2ObjectSortedMap<V> headMap(long to) {
        return new Submap((0), true, to, false);
    }
    @Override
    public Long2ObjectSortedMap<V> tailMap(long from) {
        return new Submap(from, false, (0), true);
    }
    @Override
    public Long2ObjectSortedMap<V> subMap(long from, long to) {
        return new Submap(from, false, to, false);
    }
    /**
     * A submap with given range.
     *
     * <p>
     * This class represents a submap. One has to specify the left/right limits
     * (which can be set to -&infin; or &infin;). Since the submap is a view on the
     * map, at a given moment it could happen that the limits of the range are not
     * any longer in the main map. Thus, things such as
     * {@link SortedMap#firstKey()} or {@link Collection#size()}
     * must be always computed on-the-fly.
     */
    private final class Submap extends AbstractLong2ObjectSortedMap<V> implements java.io.Serializable {
        private static final long serialVersionUID = -7046029254386353129L;
        /** The start of the submap range, unless {@link #bottom} is true. */
        long from;
        /** The end of the submap range, unless {@link #top} is true. */
        long to;
        /** If true, the submap range starts from -&infin;. */
        boolean bottom;
        /** If true, the submap range goes to &infin;. */
        boolean top;
        /** Cached set of entries. */
        protected transient ObjectSortedSet<Entry<V>> entries;
        /** Cached set of keys. */
        protected transient LongSortedSet keys;
        /** Cached collection of values. */
        protected transient ObjectCollection<V> values;
        /**
         * Creates a new submap with given key range.
         *
         * @param from
         *            the start of the submap range.
         * @param bottom
         *            if true, the first parameter is ignored and the range starts from
         *            -&infin;.
         * @param to
         *            the end of the submap range.
         * @param top
         *            if true, the third parameter is ignored and the range goes to
         *            &infin;.
         */
        public Submap(final long from, final boolean bottom, final long to, final boolean top) {
            if (!bottom && !top && Long2ObjectTreeMap.this.compare(from, to) > 0)
                throw new IllegalArgumentException("Start key (" + from + ") is larger than end key (" + to + ")");
            this.from = from;
            this.bottom = bottom;
            this.to = to;
            this.top = top;
            this.defRetValue = Long2ObjectTreeMap.this.defRetValue;
        }
        @Override
        public void clear() {
            final SubmapIterator i = new SubmapIterator();
            while (i.hasNext()) {
                i.nextEntry();
                i.remove();
            }
        }
        /**
         * Checks whether a key is in the submap range.
         *
         * @param k
         *            a key.
         * @return true if is the key is in the submap range.
         */
        final boolean in(final long k) {
            return (this.bottom || Long2ObjectTreeMap.this.compare(k, this.from) >= 0)
                    && (this.top || Long2ObjectTreeMap.this.compare(k, this.to) < 0);
        }
        @Override
        public ObjectSortedSet<Entry<V>> long2ObjectEntrySet() {
            if (this.entries == null)
                this.entries = new AbstractObjectSortedSet<Entry<V>>() {
                    @Override
                    public ObjectBidirectionalIterator<Entry<V>> iterator() {
                        return new SubmapEntryIterator();
                    }
                    @Override
                    public ObjectBidirectionalIterator<Entry<V>> iterator(
                            final Entry<V> from) {
                        return new SubmapEntryIterator(from.getLongKey());
                    }
                    @Override
                    public Comparator<? super Entry<V>> comparator() {
                        return Long2ObjectTreeMap.this.long2ObjectEntrySet().comparator();
                    }
                    @Override

                    public boolean contains(final Object o) {
                        if (!(o instanceof Map.Entry))
                            return false;
                        final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                        if (e.getKey() == null || !(e.getKey() instanceof Long))
                            return false;
                        final Long2ObjectTreeMap.Entry<V> f = Long2ObjectTreeMap.this.findKey(((Long) (e.getKey())).longValue());
                        return f != null && Submap.this.in(f.key) && e.equals(f);
                    }
                    @Override

                    public boolean remove(final Object o) {
                        if (!(o instanceof Map.Entry))
                            return false;
                        final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                        if (e.getKey() == null || !(e.getKey() instanceof Long))
                            return false;
                        final Long2ObjectTreeMap.Entry<V> f = Long2ObjectTreeMap.this.findKey(((Long) (e.getKey())).longValue());
                        if (f != null && Submap.this.in(f.key))
                            Submap.this.remove(f.key);
                        return f != null;
                    }
                    @Override
                    public int size() {
                        int c = 0;
                        for (Iterator<?> i = this.iterator(); i.hasNext(); i.next())
                            c++;
                        return c;
                    }
                    @Override
                    public boolean isEmpty() {
                        return !new SubmapIterator().hasNext();
                    }
                    @Override
                    public void clear() {
                        Submap.this.clear();
                    }
                    @Override
                    public Entry<V> first() {
                        return Submap.this.firstEntry();
                    }
                    @Override
                    public Entry<V> last() {
                        return Submap.this.lastEntry();
                    }
                    @Override
                    public ObjectSortedSet<Entry<V>> subSet(Entry<V> from,
                                                            Entry<V> to) {
                        return Submap.this.subMap(from.getLongKey(), to.getLongKey()).long2ObjectEntrySet();
                    }
                    @Override
                    public ObjectSortedSet<Entry<V>> headSet(Entry<V> to) {
                        return Submap.this.headMap(to.getLongKey()).long2ObjectEntrySet();
                    }
                    @Override
                    public ObjectSortedSet<Entry<V>> tailSet(Entry<V> from) {
                        return Submap.this.tailMap(from.getLongKey()).long2ObjectEntrySet();
                    }
                };
            return this.entries;
        }
        private class KeySet extends AbstractLong2ObjectSortedMap<V>.KeySet {
            @Override
            public LongBidirectionalIterator iterator() {
                return new SubmapKeyIterator();
            }
            @Override
            public LongBidirectionalIterator iterator(final long from) {
                return new SubmapKeyIterator(from);
            }
        }
        @Override
        public LongSortedSet keySet() {
            if (this.keys == null)
                this.keys = new KeySet();
            return this.keys;
        }
        @Override
        public ObjectCollection<V> values() {
            if (this.values == null)
                this.values = new AbstractObjectCollection<V>() {
                    @Override
                    public ObjectIterator<V> iterator() {
                        return new SubmapValueIterator();
                    }
                    @Override
                    public boolean contains(final Object k) {
                        return Submap.this.containsValue(k);
                    }
                    @Override
                    public int size() {
                        return Submap.this.size();
                    }
                    @Override
                    public void clear() {
                        Submap.this.clear();
                    }
                };
            return this.values;
        }
        @Override

        public boolean containsKey(final long k) {
            return this.in(k) && Long2ObjectTreeMap.this.containsKey(k);
        }
        @Override
        public boolean containsValue(final Object v) {
            final SubmapIterator i = new SubmapIterator();
            Object ev;
            while (i.hasNext()) {
                ev = i.nextEntry().value;
                if (Objects.equals(ev, v))
                    return true;
            }
            return false;
        }
        @Override

        public V get(final long k) {
            final Long2ObjectTreeMap.Entry<V> e;
            final long kk = k;
            return this.in(kk) && (e = Long2ObjectTreeMap.this.findKey(kk)) != null ? e.value : this.defRetValue;
        }
        @Override
        public V put(final long k, final V v) {
            Long2ObjectTreeMap.this.modified = false;
            if (!this.in(k))
                throw new IllegalArgumentException("Key (" + k + ") out of range ["
                        + (this.bottom ? "-" : String.valueOf(this.from)) + ", " + (this.top ? "-" : String.valueOf(this.to)) + ")");
            final V oldValue = Long2ObjectTreeMap.this.put(k, v);
            return Long2ObjectTreeMap.this.modified ? this.defRetValue : oldValue;
        }
        @Override

        public V remove(final long k) {
            Long2ObjectTreeMap.this.modified = false;
            if (!this.in(k))
                return this.defRetValue;
            final V oldValue = Long2ObjectTreeMap.this.remove(k);
            return Long2ObjectTreeMap.this.modified ? oldValue : this.defRetValue;
        }
        @Override
        public int size() {
            final SubmapIterator i = new SubmapIterator();
            int n = 0;
            while (i.hasNext()) {
                n++;
                i.nextEntry();
            }
            return n;
        }
        @Override
        public boolean isEmpty() {
            return !new SubmapIterator().hasNext();
        }
        @Override
        public LongComparator comparator() {
            return Long2ObjectTreeMap.this.actualComparator;
        }
        @Override
        public Long2ObjectSortedMap<V> headMap(final long to) {
            if (this.top)
                return new Submap(this.from, this.bottom, to, false);
            return Long2ObjectTreeMap.this.compare(to, this.to) < 0 ? new Submap(this.from, this.bottom, to, false) : this;
        }
        @Override
        public Long2ObjectSortedMap<V> tailMap(final long from) {
            if (this.bottom)
                return new Submap(from, false, this.to, this.top);
            return Long2ObjectTreeMap.this.compare(from, this.from) > 0 ? new Submap(from, false, this.to, this.top) : this;
        }
        @Override
        public Long2ObjectSortedMap<V> subMap(long from, long to) {
            if (this.top && this.bottom)
                return new Submap(from, false, to, false);
            if (!this.top)
                to = Long2ObjectTreeMap.this.compare(to, this.to) < 0 ? to : this.to;
            if (!this.bottom)
                from = Long2ObjectTreeMap.this.compare(from, this.from) > 0 ? from : this.from;
            if (!this.top && !this.bottom && from == this.from && to == this.to)
                return this;
            return new Submap(from, false, to, false);
        }
        /**
         * Locates the first entry.
         *
         * @return the first entry of this submap, or {@code null} if the submap is
         *         empty.
         */
        public Long2ObjectTreeMap.Entry<V> firstEntry() {
            if (Long2ObjectTreeMap.this.tree == null)
                return null;
            // If this submap goes to -infinity, we return the main map first entry;
            // otherwise, we locate the start of the map.
            Long2ObjectTreeMap.Entry<V> e;
            if (this.bottom)
                e = Long2ObjectTreeMap.this.firstEntry;
            else {
                e = Long2ObjectTreeMap.this.locateKey(this.from);
                // If we find either the start or something greater we're OK.
                if (Long2ObjectTreeMap.this.compare(e.key, this.from) < 0)
                    e = e.next();
            }
            // Finally, if this submap doesn't go to infinity, we check that the resulting
            // key isn't greater than the end.
            if (e == null || !this.top && Long2ObjectTreeMap.this.compare(e.key, this.to) >= 0)
                return null;
            return e;
        }
        /**
         * Locates the last entry.
         *
         * @return the last entry of this submap, or {@code null} if the submap is
         *         empty.
         */
        public Long2ObjectTreeMap.Entry<V> lastEntry() {
            if (Long2ObjectTreeMap.this.tree == null)
                return null;
            // If this submap goes to infinity, we return the main map last entry;
            // otherwise, we locate the end of the map.
            Long2ObjectTreeMap.Entry<V> e;
            if (this.top)
                e = Long2ObjectTreeMap.this.lastEntry;
            else {
                e = Long2ObjectTreeMap.this.locateKey(this.to);
                // If we find something smaller than the end we're OK.
                if (Long2ObjectTreeMap.this.compare(e.key, this.to) >= 0)
                    e = e.prev();
            }
            // Finally, if this submap doesn't go to -infinity, we check that the resulting
            // key isn't smaller than the start.
            if (e == null || !this.bottom && Long2ObjectTreeMap.this.compare(e.key, this.from) < 0)
                return null;
            return e;
        }
        @Override
        public long firstLongKey() {
            Long2ObjectTreeMap.Entry<V> e = this.firstEntry();
            if (e == null)
                throw new NoSuchElementException();
            return e.key;
        }
        @Override
        public long lastLongKey() {
            Long2ObjectTreeMap.Entry<V> e = this.lastEntry();
            if (e == null)
                throw new NoSuchElementException();
            return e.key;
        }
        /**
         * An iterator for subranges.
         *
         * <p>
         * This class inherits from {@link TreeIterator}, but overrides the methods that
         * update the pointer after a {@link ListIterator#next()} or
         * {@link ListIterator#previous()}. If we would move out of the range
         * of the submap we just overwrite the next or previous entry with {@code null}.
         */
        private class SubmapIterator extends TreeIterator {
            SubmapIterator() {
                this.next = Submap.this.firstEntry();
            }
            SubmapIterator(final long k) {
                this();
                if (this.next != null) {
                    if (!Submap.this.bottom && Long2ObjectTreeMap.this.compare(k, this.next.key) < 0)
                        this.prev = null;
                    else if (!Submap.this.top && Long2ObjectTreeMap.this.compare(k, (this.prev = Submap.this.lastEntry()).key) >= 0)
                        this.next = null;
                    else {
                        this.next = Long2ObjectTreeMap.this.locateKey(k);
                        if (Long2ObjectTreeMap.this.compare(this.next.key, k) <= 0) {
                            this.prev = this.next;
                            this.next = this.next.next();
                        } else
                            this.prev = this.next.prev();
                    }
                }
            }
            @Override
            void updatePrevious() {
                this.prev = this.prev.prev();
                if (!Submap.this.bottom && this.prev != null && Long2ObjectTreeMap.this.compare(this.prev.key, Submap.this.from) < 0)
                    this.prev = null;
            }
            @Override
            void updateNext() {
                this.next = this.next.next();
                if (!Submap.this.top && this.next != null && Long2ObjectTreeMap.this.compare(this.next.key, Submap.this.to) >= 0)
                    this.next = null;
            }
        }
        private class SubmapEntryIterator extends SubmapIterator
                implements
                ObjectListIterator<Entry<V>> {
            SubmapEntryIterator() {
            }
            SubmapEntryIterator(final long k) {
                super(k);
            }
            @Override
            public Entry<V> next() {
                return this.nextEntry();
            }
            @Override
            public Entry<V> previous() {
                return this.previousEntry();
            }
        }
        /**
         * An iterator on a subrange of keys.
         *
         * <p>
         * This class can iterate in both directions on a subrange of the keys of a
         * threaded tree. We simply override the
         * {@link ListIterator#next()}/{@link ListIterator#previous()}
         * methods (and possibly their type-specific counterparts) so that they return
         * keys instead of entries.
         */
        private final class SubmapKeyIterator extends SubmapIterator implements LongListIterator {
            public SubmapKeyIterator() {
                super();
            }
            public SubmapKeyIterator(long from) {
                super(from);
            }
            @Override
            public long nextLong() {
                return this.nextEntry().key;
            }
            @Override
            public long previousLong() {
                return this.previousEntry().key;
            }
        }

        /**
         * An iterator on a subrange of values.
         *
         * <p>
         * This class can iterate in both directions on the values of a subrange of the
         * keys of a threaded tree. We simply override the
         * {@link ListIterator#next()}/{@link ListIterator#previous()}
         * methods (and possibly their type-specific counterparts) so that they return
         * values instead of entries.
         */
        private final class SubmapValueIterator extends SubmapIterator implements ObjectListIterator<V> {
            @Override
            public V next() {
                return (V) this.nextEntry().value;
            }
            @Override
            public V previous() {
                return (V) this.previousEntry().value;
            }
        }
    }
    /**
     * Returns a deep copy of this tree map.
     *
     * <p>
     * This method performs a deep copy of this tree map; the data stored in the
     * set, however, is not cloned. Note that this makes a difference only for
     * object keys.
     *
     * @return a deep copy of this tree map.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Long2ObjectTreeMap<V> clone() {
        Long2ObjectTreeMap<V> c;
        try {
            c = (Long2ObjectTreeMap<V>) super.clone();
        } catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.keys = null;
        c.values = null;
        c.entries = null;
        c.allocatePaths();
        if (this.count != 0) {
            // Also this apparently unfathomable code is derived from GNU libavl.
            Entry<V> e, p, q, rp = new Entry<>(), rq = new Entry<>();
            p = rp;
            rp.left(this.tree);
            q = rq;
            rq.pred(null);
            while (true) {
                if (!p.pred()) {
                    e = p.left.clone();
                    e.pred(q.left);
                    e.succ(q);
                    q.left(e);
                    p = p.left;
                    q = q.left;
                } else {
                    while (p.succ()) {
                        p = p.right;
                        if (p == null) {
                            q.right = null;
                            c.tree = rq.left;
                            c.firstEntry = c.tree;
                            while (c.firstEntry.left != null)
                                c.firstEntry = c.firstEntry.left;
                            c.lastEntry = c.tree;
                            while (c.lastEntry.right != null)
                                c.lastEntry = c.lastEntry.right;
                            return c;
                        }
                        q = q.right;
                    }
                    p = p.right;
                    q = q.right;
                }
                if (!p.succ()) {
                    e = p.right.clone();
                    e.succ(q.right);
                    e.pred(q);
                    q.right(e);
                }
            }
        }
        return c;
    }
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        int n = this.count;
        EntryIterator i = new EntryIterator();
        Entry<V> e;
        s.defaultWriteObject();
        while (n-- != 0) {
            e = i.nextEntry();
            s.writeLong(e.key);
            s.writeObject(e.value);
        }
    }
    /**
     * Reads the given number of entries from the input stream, returning the
     * corresponding tree.
     *
     * @param s
     *            the input stream.
     * @param n
     *            the (positive) number of entries to read.
     * @param pred
     *            the entry containing the key that preceeds the first key in the
     *            tree.
     * @param succ
     *            the entry containing the key that follows the last key in the
     *            tree.
     */
    @SuppressWarnings("unchecked")
    private Entry<V> readTree(final java.io.ObjectInputStream s, final int n, final Entry<V> pred, final Entry<V> succ)
            throws java.io.IOException, ClassNotFoundException {
        if (n == 1) {
            final Entry<V> top = new Entry<>(s.readLong(), (V) s.readObject());
            top.pred(pred);
            top.succ(succ);
            top.black(true);
            return top;
        }
        if (n == 2) {
            /*
             * We handle separately this case so that recursion will always* be on nonempty
             * subtrees.
             */
            final Entry<V> top = new Entry<>(s.readLong(), (V) s.readObject());
            top.black(true);
            top.right(new Entry<>(s.readLong(), (V) s.readObject()));
            top.right.pred(top);
            top.pred(pred);
            top.right.succ(succ);
            return top;
        }
        // The right subtree is the largest one.
        final int rightN = n / 2, leftN = n - rightN - 1;
        final Entry<V> top = new Entry<>();
        top.left(this.readTree(s, leftN, pred, top));
        top.key = s.readLong();
        top.value = (V) s.readObject();
        top.black(true);
        top.right(this.readTree(s, rightN, top, succ));
        if (n + 2 == ((n + 2) & -(n + 2)))
            top.right.black(false); // Quick test for determining whether n + 2 is a power of 2.
        return top;
    }
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        /*
         * The storedComparator is now correctly set, but we must restore on-the-fly the
         * actualComparator.
         */
        this.setActualComparator();
        this.allocatePaths();
        if (this.count != 0) {
            this.tree = this.readTree(s, this.count, null, null);
            Entry<V> e;
            e = this.tree;
            while (e.left() != null)
                e = e.left();
            this.firstEntry = e;
            e = this.tree;
            while (e.right() != null)
                e = e.right();
            this.lastEntry = e;
        }
    }
}

