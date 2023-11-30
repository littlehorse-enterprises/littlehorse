package io.littlehorse.server.streams.lhinternalscan;

import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.TagStorageType;

public interface GetableSearch {
    InternalScan buildInternalScan(TagStorageType tagStorageType) throws LHApiException;
}
