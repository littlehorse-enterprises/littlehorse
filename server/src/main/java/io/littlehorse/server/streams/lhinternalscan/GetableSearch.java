package io.littlehorse.server.streams.lhinternalscan;

import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.TagStorageType;

public interface GetableSearch {
    InternalScan buildInternalScan(ReadOnlyMetadataProcessorDAO readOnlyDao, TagStorageType tagStorageType)
            throws LHApiException;
}
