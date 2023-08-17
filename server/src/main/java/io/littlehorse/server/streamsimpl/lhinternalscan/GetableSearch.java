package io.littlehorse.server.streamsimpl.lhinternalscan;

import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;

public interface GetableSearch {
    InternalScan buildInternalScan(
        LHGlobalMetaStores stores,
        TagStorageType tagStorageType
    ) throws LHValidationError;
}
