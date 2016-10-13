package edu.wallet.server.model;

import edu.wallet.server.EvictableValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Maps values by keys.
 * Values are created lazily using {@link ValueFactory}.
 * <p>
 * Despite of the name, does not depend on any Hadoop classes.
 */
public class LazyConcurrentMap<K, V> {
    /**
     * The map storing the actual values.
     */
    private final ConcurrentMap<K, ValueWrapper> map;

    /**
     * The factory passed in by the client. Will be used for lazy value creation.
     */
    private final ValueFactory<K, V> factory;

    /**
     * Constructor.
     *
     * @param factory the factory to create new values lazily.
     */
    public LazyConcurrentMap(ValueFactory<K, V> factory, ConcurrentMap<K, ?> m) {
        this.factory = factory;

        this.map = (ConcurrentMap) m;
    }

    /**
     * Gets cached or creates a new value of V.
     * Never returns null.
     *
     * @param k the key to associate the value with.
     * @return the cached or newly created value, never null.
     */
    public V getOrCreate(K k) {
        ValueWrapper w = map.get(k);

        if (w == null) {
            final ValueWrapper wNew = new ValueWrapper(k);

            w = map.putIfAbsent(k, wNew);

            if (w == null) {
                wNew.init();

                w = wNew;
            }
        }

        V v = w.getValue();

        assert v != null;

        return v;
    }

    public Collection<V> values() {
        Collection<ValueWrapper> c1 = map.values();

        List<V> c2 = new ArrayList<>(c1.size());

        for (ValueWrapper vw : c1) {
            c2.add(vw.getValue());
        }
        return Collections.unmodifiableList(c2);
    }

    public static class CompletableFuture<V> extends FutureTask<V> {

        public CompletableFuture() {
            super(new Runnable() {
                @Override
                public void run() {
                }
            }, null);
        }

        @Override
        public void set(V v) {
            super.set(v);
        }

        @Override
        public void setException(Throwable t) {
            super.setException(t);
        }
    }

    /**
     * Helper class that drives the lazy value creation.
     */
    private class ValueWrapper implements EvictableValue<K> {
        /**
         * Future.
         */
        private final CompletableFuture<V> fut = new CompletableFuture<>();

        /**
         * the key
         */
        private final K key;

        /**
         * Creates new wrapper.
         */
        private ValueWrapper(K key) {
            this.key = key;
        }

        /**
         * Initializes the value using the factory.
         */
        private void init() {
            try {
                final V v0 = factory.createValue(key);

                if (v0 == null)
                    throw new RuntimeException("Failed to create non-null value. [key=" + key + ']');

                fut.set(v0);
            } catch (Throwable e) {
                fut.setException(e);
            }
        }

        /**
         * Gets the available value or blocks until the value is initialized.
         *
         * @return the value, never null.
         */
        V getValue() {
            try {
                return fut.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void evicted(K key) {
            // Noop for now.
        }
    }

    /**
     * Interface representing the factory that creates map values.
     *
     * @param <K> the type of the key.
     * @param <V> the type of the value.
     */
    public interface ValueFactory<K, V> {
        /**
         * Creates the new value. Should never return null.
         *
         * @param key the key to create value for
         * @return the value.
         * @throws IOException On failure.
         */
        public V createValue(K key) throws IOException;
    }
}