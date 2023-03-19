package io.littlehorse.common.model;

import com.google.protobuf.Message;

public abstract class Storeable<T extends Message> extends LHSerializable<T> {

    public abstract String getObjectId();
}
