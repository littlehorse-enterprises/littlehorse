package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.proto.LHStoreType;
import java.util.Optional;

public abstract class CoreObjectId<T extends Message, U extends Message, V extends CoreGetable<U>>
        extends ObjectIdModel<T, U, V> {

    @Override
    public LHStoreType getStore() {
        return LHStoreType.METADATA;
    }

    // Force the implementation.
    @Override
    public abstract Optional<String> getPartitionKey();
}
