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
        synchronized (key.intern()) {
            StoredGetable<? extends Message, ? extends MetadataGetable<?>> result = super.get(key);
            if (result == null) {
                if (super.containsKey(key)) {
                    // we already know that the store does not contain this key
                    return null;
                }
                result = valueSupplier.get();
                super.updateCache(key, result);
            }
            return result;
        }
    }

    public void evict(String key) {
        synchronized (key.intern()) {
            super.evictCache(key);
        }
    }

    public void update(String key, StoredGetable<? extends Message, ? extends MetadataGetable<?>> value) {
        synchronized (key.intern()) {
            super.updateCache(key, value);
        }
    }
}
