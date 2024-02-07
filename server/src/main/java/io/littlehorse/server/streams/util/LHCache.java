package io.littlehorse.server.streams.util;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LHCache<K, V> {

    private final ConcurrentHashMap<K, Optional<V>> cache = new ConcurrentHashMap<>();

    public V getOrCache(K key, Supplier<V> cacheable) {
        if (cache.containsKey(key)) {
            return cache.get(key).orElse(null);
        }
        V value = cacheable.get();
        if (value != null) {
            cache.put(key, Optional.ofNullable(value));
        }
        return value;
    }

    public V get(K key) {
        Optional<V> result = cache.get(key);
        return result != null ? result.orElse(null) : null;
    }

    public void updateCache(K key, V value) {
        cache.put(key, Optional.ofNullable(value));
    }

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    public void evictCache(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}
