package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.model.MetadataGetable;

public abstract class MetadataId<T extends Message, U extends Message, V extends MetadataGetable<U>>
        extends ObjectIdModel<T, U, V> {

    @Override
    public LHStore getStore() {
        return LHStore.GLOBAL_METADATA;
    }
}
