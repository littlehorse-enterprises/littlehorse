package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.store.StoredGetable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataCache extends LHCache<String, StoredGetablePb> {

    public MetadataCache() {}

    public void evictOrUpdate(StoredGetable<? extends Message, ? extends MetadataGetable<?>> value, String cacheKey)
            throws LHSerdeError {
        if (value == null) {
            super.evictCache(cacheKey);
        } else {
            super.updateCache(cacheKey, value.toProto().build());
        }
    }

    public void updateMissingKey(String missingKey) {
        super.updateCache(missingKey, null);
    }
}
