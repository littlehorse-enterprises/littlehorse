package io.littlehorse.common;

import com.google.protobuf.Message;
// import io.littlehorse.common.proto.TagStorageType;

public abstract class Storeable<T extends Message> extends LHSerializable<T> {

    public abstract String getStoreKey();

    // @Deprecated(forRemoval = true)
    // public TagStorageType tagStorageType() {
    // return TagStorageType.LOCAL;
    // }
}
