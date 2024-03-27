package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataCache extends LHCache<String, MetadataCache.CachedRecord> {

    public MetadataCache() {}

    public void evictOrUpdate(CachedRecord value, String cacheKey) throws LHSerdeError {
        if (value == null) {
            super.evictCache(cacheKey);
        } else {
            super.updateCache(cacheKey, value);
        }
    }

    public void updateMissingKey(String missingKey) {
        super.updateCache(missingKey, null);
    }

    public record CachedRecord<U extends Message, T extends LHSerializable<U>>(Class<T> clazzModel, U record) {}
}
