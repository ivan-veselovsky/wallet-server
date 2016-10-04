package edu.wallet.server;

/**
 * TODO: clarify, if we do that.
 */
public interface EvictableValue<K> {
    void evicted(K key);
}
