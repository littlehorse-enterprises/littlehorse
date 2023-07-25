package io.littlehorse.server.streamsimpl.lhinternalscan;

import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;

public interface GetableSearchStrategy {
    InternalScan buildInternalScan(
        LHGlobalMetaStores stores,
        TagStorageTypePb tagStorageType
    ) throws LHValidationError;
}
