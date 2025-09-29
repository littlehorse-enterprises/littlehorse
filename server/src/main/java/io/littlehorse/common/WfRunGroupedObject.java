package io.littlehorse.common;

import io.littlehorse.common.model.getable.objectId.WfRunIdModel;

/**
 * See Proposal #9
 */
public interface WfRunGroupedObject {
    /**
     * Returns the id of the WfRun to which this object should be grouped in RocksDB.
     */
    WfRunIdModel getGroupingWfRunId();

    /**
     * Returns the part of the RocksDB Key that is _after_ the grouping WfRunId and
     * the storeable type.
     */
    String getKeySuffix();
}
