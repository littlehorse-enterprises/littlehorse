package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.proto.TagStorageTypePb;

public abstract class Storeable<T extends Message> extends LHSerializable<T> {

    public abstract String getStoreKey();

    public TagStorageTypePb tagStorageTypePb() {
        return TagStorageTypePb.LOCAL;
    }
}
