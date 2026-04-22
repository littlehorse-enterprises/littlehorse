package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.store.StoredGetable;

public class MetadataCache extends LHCache<String, StoredGetable<? extends Message, ? extends MetadataGetable<?>>> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MetadataCache.class);

    public MetadataCache() {}

    public void evictOrUpdate(StoredGetable<? extends Message, ? extends MetadataGetable<?>> value, String cacheKey)
            throws LHSerdeException {
        if (value == null) {
            super.evictCache(cacheKey);
        } else {
            super.updateCache(cacheKey, value);
        }
    }

    public void updateMissingKey(String missingKey) {
        super.updateCache(missingKey, null);
    }
}
