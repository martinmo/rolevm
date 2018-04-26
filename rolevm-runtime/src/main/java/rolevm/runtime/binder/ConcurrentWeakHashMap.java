package rolevm.runtime.binder;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a {@link ConcurrentHashMap} with weak keys and reference-equality in
 * place of object-equality. Please note that some operations are unsupported.
 *
 *
 * @implNote using {@link java.util.Collections#synchronizedMap(Map)} to wrap a
 *           {@link java.util.WeakHashMap} was not fast enough for our use case
 *           and also does not use reference-equality comparison. Still, this
 *           implementation also has a drawback, because it must create a new
 *           {@link Key} object for every retrieval or modification.
 *
 * @param <K>
 *            the type of keys maintained by this map
 * @param <V>
 *            the type of mapped values
 *
 * @author Martin Morgenstern
 */
public class ConcurrentWeakHashMap<K, V> implements Map<K, V> {
    /** The underlying map to which we forward all calls. */
    private final Map<Key, V> storage = new ConcurrentHashMap<>();

    /** Queue used to listen for reclaimable {@link Key} objects. */
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    /**
     * Wraps the real keys in a {@link WeakReference} and provides the
     * reference-equality semantics when comparing keys via {@link #equals(Object)}
     * and {@link #hashCode()}.
     */
    private final class Key extends WeakReference<Object> {
        private final int hashCode;

        public Key(Object referent) {
            super(referent, queue);
            hashCode = System.identityHashCode(referent);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ConcurrentWeakHashMap.Key) {
                @SuppressWarnings("unchecked")
                Key other = (Key) o;
                return other.get() == get();
            }
            return false;
        }
    }

    /** Removes entries belonging to stale keys from this map. */
    private void removeStaleReferences() {
        for (Object x; (x = queue.poll()) != null;) {
            storage.remove(x);
        }
    }

    @Override
    public void clear() {
        removeStaleReferences();
        storage.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        removeStaleReferences();
        return storage.containsKey(new Key(key));
    }

    @Override
    public boolean containsValue(Object value) {
        removeStaleReferences();
        return storage.containsValue(value);
    }

    @Override
    public V get(Object key) {
        removeStaleReferences();
        return storage.get(new Key(key));
    }

    @Override
    public boolean isEmpty() {
        removeStaleReferences();
        return storage.isEmpty();
    }

    @Override
    public V put(K key, V value) {
        removeStaleReferences();
        return storage.put(new Key(key), value);
    }

    @Override
    public V remove(Object key) {
        removeStaleReferences();
        return storage.remove(new Key(key));
    }

    @Override
    public int size() {
        removeStaleReferences();
        return storage.size();
    }

    // unsupported operations

    /** Unsupported operation. */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    /** Unsupported operation. */
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /** Unsupported operation. */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    /** Unsupported operation. */
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }
}
