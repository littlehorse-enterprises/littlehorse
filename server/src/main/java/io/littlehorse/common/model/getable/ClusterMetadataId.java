package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.model.ClusterMetadataGetable;

public abstract class ClusterMetadataId<T extends Message, U extends Message, V extends ClusterMetadataGetable<U>>
        extends ObjectIdModel<T, U, V> {

    @Override
    public LHStore getStore() {
        // This is the same as the regular tenant-scoped Metadata, such as WfSpec/TaskDef.
        return LHStore.GLOBAL_METADATA;
    }
}
