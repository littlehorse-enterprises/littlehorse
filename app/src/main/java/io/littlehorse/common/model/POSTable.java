package io.littlehorse.common.model;

import com.google.protobuf.MessageOrBuilder;

public abstract class POSTable<T extends MessageOrBuilder> extends GETable<T> {

    public long updatedOffset;
}
