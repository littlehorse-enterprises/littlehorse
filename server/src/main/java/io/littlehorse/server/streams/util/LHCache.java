package io.littlehorse.server.streams.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LHCache<K, V> {

    private final Map<K, Optional<V>> cache;

    public LHCache() {
        cache = new ConcurrentHashMap<>();
    }

    /** Creates an access-ordered cache that holds at most {@code maxEntries}. */
    protected LHCache(int maxEntries) {
        cache = Collections.synchronizedMap(new BoundedLruMap<>(maxEntries));
    }

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

    private static class BoundedLruMap<K, V> extends LinkedHashMap<K, V> {

        private final int maxEntries;

        private BoundedLruMap(int maxEntries) {
            super(16, 0.75f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxEntries;
        }
    }
}
