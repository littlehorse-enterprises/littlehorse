package io.littlehorse.server.streams.util;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LHCache<K, V> {

    private final ConcurrentHashMap<K, Optional<V>> cache = new ConcurrentHashMap<>();

    protected final V get(K key) {
        Optional<V> result = cache.get(key);
        return result != null ? result.orElse(null) : null;
    }

    protected final boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    protected final V computeIfAbsent(K key, Supplier<V> valueSupplier) {
        Optional<V> result = cache.computeIfAbsent(key, k -> Optional.ofNullable(valueSupplier.get()));
        return result.orElse(null);
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
