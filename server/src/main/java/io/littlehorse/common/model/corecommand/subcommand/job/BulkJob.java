package io.littlehorse.common.model.corecommand.subcommand.job;

import io.littlehorse.server.streams.store.LHIterKeyValue;

/**
 * Defines a contract for processing item in a bulk job.
 * Every
 */
public interface BulkJob {
    /**
     * Process one record from the range scan result
     * @param iterRecord
     * @return
     */
    String processOneRecord(LHIterKeyValue<?> iterRecord);
}
