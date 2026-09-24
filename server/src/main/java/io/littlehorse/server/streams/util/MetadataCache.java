package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.function.Supplier;

public class MetadataCache extends LHCache<String, StoredGetable<? extends Message, ? extends MetadataGetable<?>>> {

    static final int MAX_CAPACITY = 1_000;

    public MetadataCache() {
        super(MAX_CAPACITY);
    }

    public StoredGetable<? extends Message, ? extends MetadataGetable<?>> getOrUpdate(
            String key, Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> valueSupplier) {
        return computeIfAbsent(key, valueSupplier);
    }

    public void evict(String key) {
        evictCache(key);
    }

    public void update(String key, StoredGetable<? extends Message, ? extends MetadataGetable<?>> value) {
        updateCache(key, value);
    }
}
