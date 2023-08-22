package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.model.CoreGetable;
import java.util.Optional;

public abstract class CoreObjectId<T extends Message, U extends Message, V extends CoreGetable<U>>
        extends ObjectIdModel<T, U, V> {

    @Override
    public LHStore getStore() {
        return LHStore.CORE;
    }

    // Force the implementation.
    @Override
    public abstract Optional<String> getPartitionKey();
}
