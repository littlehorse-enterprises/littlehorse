package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Caches {@link StoredGetable}s of {@link MetadataGetable} metadata (TaskDef, WfSpec, etc).
 *
 * <p>The cache keeps at most {@link #MAX_ENTRIES} entries, evicting the least-recently-used
 * entry once that limit is exceeded.
 */
@Slf4j
public class MetadataCache extends LHCache<String, StoredGetable<? extends Message, ? extends MetadataGetable<?>>> {

    /**
     * Maximum number of entries retained before LRU eviction kicks in.
     */
    public static final int MAX_ENTRIES = 5000;

    private final Map<String, Optional<StoredGetable<? extends Message, ? extends MetadataGetable<?>>>> cache =
            Collections.synchronizedMap(new LruMap<>(MAX_ENTRIES));

    public MetadataCache() {}

    /**
     * A {@link LinkedHashMap} that evicts its least-recently-used entry once it exceeds {@code maxEntries}.
     */
    private static class LruMap<K, V> extends LinkedHashMap<K, V> {

        private final int maxEntries;

        LruMap(int maxEntries) {
            super(16, 0.75f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxEntries;
        }
    }

    public StoredGetable<? extends Message, ? extends MetadataGetable<?>> getOrUpdate(
            String key, Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> valueSupplier) {
        synchronized (cache) {
            Optional<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> existing = cache.get(key);
            if (existing != null) {
                return existing.orElse(null);
            }
            StoredGetable<? extends Message, ? extends MetadataGetable<?>> value = valueSupplier.get();
            cache.put(key, Optional.ofNullable(value));
            return value;
        }
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void update(String key, StoredGetable<? extends Message, ? extends MetadataGetable<?>> value) {
        cache.put(key, Optional.ofNullable(value));
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }
}
