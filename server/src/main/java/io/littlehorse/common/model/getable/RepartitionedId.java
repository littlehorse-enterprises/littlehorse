package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.model.RepartitionedGetable;
import java.util.Optional;

public abstract class RepartitionedId<T extends Message, U extends Message, V extends RepartitionedGetable<U>>
        extends ObjectIdModel<T, U, V> {

    @Override
    public LHStore getStore() {
        return LHStore.REPARTITION;
    }

    // Force the implementation
    @Override
    public abstract Optional<String> getPartitionKey();
}
