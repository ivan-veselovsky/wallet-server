package edu.wallet.server;

/**
 * Created by ivan on 03.10.16.
 */
public interface EvictableValue<K> {
    void evicted(K key);
}
