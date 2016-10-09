package edu.wallet.server.model;

import edu.wallet.server.EvictableValue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Class ConcurrentLinkedHashMap from jsr166 and Google libs (same class?) represents both linked and bounded
 * (limited in size) concurrent map.
 * Unfortunately, we cannot use 3rd party solutions, so let's try to bicycle there.
 * <p>
 * This class implements {@link  ConcurrentMap}, but to simplify the implementation it only
 * supports #putIfAbsent(K, V) semantic, and no removal operations.
 * The map is cleared implicitly. It removes least recently added keys to preserve the limit constraint.
 * To simplify the implementation we introduce notions of hard and soft limits, see the constructor
 * comment for more detail.
 */
public class LimitedConcurrentMap<K, V extends EvictableValue> implements ConcurrentMap<K, V> {

    private final ConcurrentMap<K, V> map;

    private final int softLimit;

    /*
     * A queue to maintain the order of keys insertion:
     */
    private final ConcurrentLinkedQueue<K> queue = new ConcurrentLinkedQueue<>();

    /**
     * Constructor.
     *
     * @param m          The underlying concurent map.
     * @param hardLimit  The Hard limit that really should not be ever exceeded.
     * @param numThreads Expected number of threads that will work with this map.
     */
    public LimitedConcurrentMap(ConcurrentMap<K, V> m, int hardLimit, int numThreads) {
        this.map = m;

        this.softLimit = hardLimit - numThreads;

        if (softLimit < 0) {
            throw new IllegalArgumentException("hardLimit too small: " + hardLimit
                    + ". It should be at least 1 more than th enumber of threads, which is " + numThreads);
        }
    }

    /**
     * Simple eviction implementation.
     */
    private void evictIfNeeded() {
        int size = map.size();

        if (size > softLimit) {
            K victimKey = queue.poll(); // remove head

            if (victimKey != null) {
                V evicted = map.remove(victimKey);

                if (evicted != null)
                    evicted.evicted(victimKey);
            }
        }
    }

    public V putIfAbsent(K key, V value) {
        evictIfNeeded(); // pre-eviction

        final V old = map.putIfAbsent(key, value);

        if (old == null) { // we have put the new key:
            queue.offer(key); // add the newly put key to the tail.

            evictIfNeeded(); // post-eviction
        }

        return old;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public V get(Object key) {
        return map.get(key);
    }

    // --------------------------------------

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
