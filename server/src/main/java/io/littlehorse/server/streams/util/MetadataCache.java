package io.littlehorse.server.streams.util;

import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataCache extends LHCache<String, StoredGetablePb> {

    public MetadataCache() {}

    public void evictOrUpdate(StoredGetablePb value, String cacheKey) throws LHSerdeError {
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
