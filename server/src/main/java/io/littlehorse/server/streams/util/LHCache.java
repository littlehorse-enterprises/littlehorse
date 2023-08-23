package io.littlehorse.server.streams.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LHCache<K, V> {

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    public V getOrCache(K key, Supplier<V> cacheable) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        V value = cacheable.get();
        if (value != null) {
            cache.put(key, value);
        }
        return value;
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void updateCache(K key, V value) {
        cache.put(key, value);
    }

    public void evictCache(K key) {
        cache.remove(key);
    }
}
