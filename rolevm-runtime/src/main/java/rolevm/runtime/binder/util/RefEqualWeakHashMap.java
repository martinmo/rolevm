/* Copyright notice: This file is based on code from OpenJDK 9. */
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package rolevm.runtime.binder.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Custom hash table based implementation of the {@code Map} interface, with
 * <em>weak keys</em> and <em>reference-equality</em> in place of
 * object-equality when keys (and values) are compared.
 *
 * <p>
 * This class is a fork of {@link java.util.WeakHashMap}. Note that most
 * {@link Map} operations have been removed and throw an
 * {@link UnsupportedOperationException}.
 *
 * @param <K>
 *            the type of keys maintained by this map
 * @param <V>
 *            the type of mapped values
 *
 * @author Doug Lea (java.util.WeakHashMap)
 * @author Josh Bloch (java.util.WeakHashMap)
 * @author Mark Reinhold (java.util.WeakHashMap)
 * @author Martin Morgenstern (RefEqualWeakHashMap)
 * @see System#identityHashCode(Object)
 * @see java.util.HashMap
 * @see java.util.IdentityHashMap
 * @see java.util.WeakHashMap
 * @see java.lang.ref.WeakReference
 */
public class RefEqualWeakHashMap<K, V> implements Map<K, V> {

    /**
     * The default initial capacity -- MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by
     * either of the constructors with arguments. MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    Entry<K, V>[] table;

    /**
     * The number of key-value mappings contained in this weak hash map.
     */
    private int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     */
    private int threshold;

    /**
     * The load factor for the hash table.
     */
    private final float loadFactor;

    /**
     * Reference queue for cleared WeakEntries
     */
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] newTable(int n) {
        return (Entry<K, V>[]) new Entry<?, ?>[n];
    }

    /**
     * Constructs a new, empty {@code RefEqualWeakHashMap} with the default initial
     * capacity (16) and load factor (0.75).
     */
    public RefEqualWeakHashMap() {
        int capacity = DEFAULT_INITIAL_CAPACITY;
        table = newTable(capacity);
        loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int) (capacity * loadFactor);
    }

    // internal utilities

    /**
     * Value representing null keys inside tables.
     */
    private static final Object NULL_KEY = new Object();

    /**
     * Use NULL_KEY for key if it is null.
     */
    private static Object maskNull(Object key) {
        return (key == null) ? NULL_KEY : key;
    }

    /**
     * Returns internal representation of null key back to caller as null.
     */
    static Object unmaskNull(Object key) {
        return (key == NULL_KEY) ? null : key;
    }

    /**
     * Returns index for hash code h.
     */
    private static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    /**
     * Expunges stale entries from the table.
     */
    private void expungeStaleEntries() {
        for (Object x; (x = queue.poll()) != null;) {
            synchronized (queue) {
                @SuppressWarnings("unchecked")
                Entry<K, V> e = (Entry<K, V>) x;
                int i = indexFor(e.hash, table.length);

                Entry<K, V> prev = table[i];
                Entry<K, V> p = prev;
                while (p != null) {
                    Entry<K, V> next = p.next;
                    if (p == e) {
                        if (prev == e)
                            table[i] = next;
                        else
                            prev.next = next;
                        // Must not null out e.next;
                        // stale entries may be in use by a HashIterator
                        e.value = null; // Help GC
                        size--;
                        break;
                    }
                    prev = p;
                    p = next;
                }
            }
        }
    }

    /**
     * Returns the table after first expunging stale entries.
     */
    private Entry<K, V>[] getTable() {
        expungeStaleEntries();
        return table;
    }

    /**
     * Returns the number of key-value mappings in this map. This result is a
     * snapshot, and may not reflect unprocessed entries that will be removed before
     * next attempted access because they are no longer referenced.
     */
    public int size() {
        if (size == 0)
            return 0;
        expungeStaleEntries();
        return size;
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings. This result
     * is a snapshot, and may not reflect unprocessed entries that will be removed
     * before next attempted access because they are no longer referenced.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if
     * this map contains no mapping for the key.
     *
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value
     * {@code v} such that {@code key == k}, then this method returns {@code v};
     * otherwise it returns {@code null}. (There can be at most one such mapping.)
     *
     * <p>
     * A return value of {@code null} does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}. The {@link #containsKey containsKey}
     * operation may be used to distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
    public V get(Object key) {
        Object k = maskNull(key);
        int h = System.identityHashCode(k);
        int index = indexFor(h, table.length);
        Entry<K, V> e = table[index];
        while (e != null) {
            if (e.hash == h && k == e.get())
                return e.value;
            e = e.next;
        }
        return null;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     *
     * @param key
     *            The key whose presence in this map is to be tested
     * @return {@code true} if there is a mapping for {@code key}; {@code false}
     *         otherwise
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in this map. Returns null
     * if the map contains no mapping for this key.
     */
    Entry<K, V> getEntry(Object key) {
        Object k = maskNull(key);
        int h = System.identityHashCode(k);
        int index = indexFor(h, table.length);
        Entry<K, V> e = table[index];
        while (e != null && !(e.hash == h && k == e.get()))
            e = e.next;
        return e;
    }

    /**
     * Associates the specified value with the specified key in this map. If the map
     * previously contained a mapping for this key, the old value is replaced.
     *
     * @param key
     *            key with which the specified value is to be associated.
     * @param value
     *            value to be associated with the specified key.
     * @return the previous value associated with {@code key}, or {@code null} if
     *         there was no mapping for {@code key}. (A {@code null} return can also
     *         indicate that the map previously associated {@code null} with
     *         {@code key}.)
     */
    public V put(K key, V value) {
        Object k = maskNull(key);
        int h = System.identityHashCode(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);

        for (Entry<K, V> e = tab[i]; e != null; e = e.next) {
            if (h == e.hash && k == e.get()) {
                V oldValue = e.value;
                if (value != oldValue)
                    e.value = value;
                return oldValue;
            }
        }

        Entry<K, V> e = tab[i];
        tab[i] = new Entry<>(k, value, queue, h, e);
        if (++size >= threshold)
            resize(tab.length * 2);
        return null;
    }

    /**
     * Rehashes the contents of this map into a new array with a larger capacity.
     * This method is called automatically when the number of keys in this map
     * reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map,
     * but sets threshold to Integer.MAX_VALUE. This has the effect of preventing
     * future calls.
     *
     * @param newCapacity
     *            the new capacity, MUST be a power of two; must be greater than
     *            current capacity unless current capacity is MAXIMUM_CAPACITY (in
     *            which case value is irrelevant).
     */
    void resize(int newCapacity) {
        Entry<K, V>[] oldTable = getTable();
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry<K, V>[] newTable = newTable(newCapacity);
        transfer(oldTable, newTable);
        table = newTable;

        /*
         * If ignoring null elements and processing ref queue caused massive shrinkage,
         * then restore old table. This should be rare, but avoids unbounded expansion
         * of garbage-filled tables.
         */
        if (size >= threshold / 2) {
            threshold = (int) (newCapacity * loadFactor);
        } else {
            expungeStaleEntries();
            transfer(newTable, oldTable);
            table = oldTable;
        }
    }

    /** Transfers all entries from src to dest tables */
    private void transfer(Entry<K, V>[] src, Entry<K, V>[] dest) {
        for (int j = 0; j < src.length; ++j) {
            Entry<K, V> e = src[j];
            src[j] = null;
            while (e != null) {
                Entry<K, V> next = e.next;
                Object key = e.get();
                if (key == null) {
                    e.next = null; // Help GC
                    e.value = null; // " "
                    size--;
                } else {
                    int i = indexFor(e.hash, dest.length);
                    e.next = dest[i];
                    dest[i] = e;
                }
                e = next;
            }
        }
    }

    /**
     * Removes the mapping for a key from this weak hash map if it is present. More
     * formally, if this map contains a mapping from key {@code k} to value
     * {@code v} such that <code>key == k</code>, that mapping is removed. (The map
     * can contain at most one such mapping.)
     *
     * <p>
     * Returns the value to which this map previously associated the key, or
     * {@code null} if the map contained no mapping for the key. A return value of
     * {@code null} does not <i>necessarily</i> indicate that the map contained no
     * mapping for the key; it's also possible that the map explicitly mapped the
     * key to {@code null}.
     *
     * <p>
     * The map will not contain a mapping for the specified key once the call
     * returns.
     *
     * @param key
     *            key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or {@code null} if
     *         there was no mapping for {@code key}
     */
    public V remove(Object key) {
        Object k = maskNull(key);
        int h = System.identityHashCode(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);
        Entry<K, V> prev = tab[i];
        Entry<K, V> e = prev;

        while (e != null) {
            Entry<K, V> next = e.next;
            if (h == e.hash && k == e.get()) {
                size--;
                if (prev == e)
                    tab[i] = next;
                else
                    prev.next = next;
                return e.value;
            }
            prev = e;
            e = next;
        }

        return null;
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this
     * call returns.
     */
    public void clear() {
        // clear out ref queue. We don't need to expunge entries
        // since table is getting cleared.
        while (queue.poll() != null)
            ;

        Arrays.fill(table, null);
        size = 0;

        // Allocation of array may have caused GC, which may have caused
        // additional entries to go stale. Removing these entries from the
        // reference queue will make them eligible for reclamation.
        while (queue.poll() != null)
            ;
    }

    /**
     * The entries in this hash table extend WeakReference, using its main ref field
     * as the key.
     */
    private static class Entry<K, V> extends WeakReference<Object> implements Map.Entry<K, V> {
        V value;
        final int hash;
        Entry<K, V> next;

        /**
         * Creates new entry.
         */
        Entry(Object key, V value, ReferenceQueue<Object> queue, int hash, Entry<K, V> next) {
            super(key, queue);
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) RefEqualWeakHashMap.unmaskNull(get());
        }

        public V getValue() {
            return value;
        }

        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            K k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2) {
                V v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2)
                    return true;
            }
            return false;
        }

        public int hashCode() {
            K k = getKey();
            V v = getValue();
            return System.identityHashCode(k) ^ System.identityHashCode(v);
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    // unsupported operations

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }
}
