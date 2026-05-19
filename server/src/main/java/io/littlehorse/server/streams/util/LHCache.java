package io.littlehorse.server.streams.util;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LHCache<K, V> {

    private final ConcurrentHashMap<K, Optional<V>> cache = new ConcurrentHashMap<>();

    protected final V get(K key) {
        Optional<V> result = cache.get(key);
        return result != null ? result.orElse(null) : null;
    }

    protected final boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    protected final void updateCache(K key, V value) {
        cache.put(key, Optional.ofNullable(value));
    }

    protected final void evictCache(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
