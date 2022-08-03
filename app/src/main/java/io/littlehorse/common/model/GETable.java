package io.littlehorse.common.model;

import java.util.Date;
import java.util.List;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.server.model.internal.IndexEntry;

public abstract class GETable<T extends MessageOrBuilder> extends LHSerializable<T> {
    public abstract Date getCreatedAt();

    public abstract String getPartitionKey();

    public abstract String getStoreKey();

    public abstract List<IndexEntry> getIndexEntries();

    public static String getBaseStoreName(Class<? extends GETable<?>> cls) {
        return cls.getSimpleName();
    }
}

/*
 * Some random thoughts:
 * - each GETable has a partition key and an ID. They may be different.
 * - For example, we want TaskRun's for a WfRun to end up on the same host
 * - VariableValue's for a ThreadRun will end up on the same node as each other
 * - Will we query VariableValue's from the Scheduler topology or from the
 *   API topology?
 * 
 * Will we make it possible to deploy the Scheduler separately from the API?
 *   - yes we will.
 */
