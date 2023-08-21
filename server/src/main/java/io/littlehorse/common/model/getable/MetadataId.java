package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.proto.LHStoreType;

public abstract class MetadataId<T extends Message, U extends Message, V extends GlobalGetable<U>>
        extends ObjectIdModel<T, U, V> {

    @Override
    public LHStoreType getStore() {
        return LHStoreType.METADATA;
    }
}
