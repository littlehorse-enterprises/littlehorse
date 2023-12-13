package io.littlehorse.common.model.corecommand.subcommand.job;

import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.StoredGetable;

interface AbstractBulkJob<T> extends BulkJob {

    default String processOneRecord(LHIterKeyValue<?> iterRecord) {
        StoredGetable<?, ?> value = (StoredGetable<?, ?>) iterRecord.getValue();
        process((T) value.getStoredObject());
        return iterRecord.getKey();
    }

    void process(T record);
}
