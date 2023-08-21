package io.littlehorse.server.streams.lhinternalscan;

import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.proto.TagStorageType;

public interface GetableSearch {
    InternalScan buildInternalScan(ReadOnlyMetadataStore stores, TagStorageType tagStorageType)
            throws LHValidationError;
}
