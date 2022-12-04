package io.littlehorse.common.model;

import com.google.protobuf.MessageOrBuilder;

public abstract class Storeable<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    public abstract String getObjectId();
}
