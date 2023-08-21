package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.proto.TagStorageType;

public abstract class CoreGetable<T extends Message> extends AbstractGetable<T> {

    public TagStorageType getTagType() {
        return TagStorageType.LOCAL;
    }
}
