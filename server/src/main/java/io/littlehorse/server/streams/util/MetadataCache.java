package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataCache extends LHCache<String, StoredGetable<? extends Message, ? extends MetadataGetable<?>>> {

    public MetadataCache() {}

    public StoredGetable<? extends Message, ? extends MetadataGetable<?>> getOrUpdate(
            String key, Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> valueSupplier) {
        return super.computeIfAbsent(key, valueSupplier);
    }

    public void evict(String key) {
        super.evictCache(key);
    }

    public void update(String key, StoredGetable<? extends Message, ? extends MetadataGetable<?>> value) {
        super.updateCache(key, value);
    }
}
