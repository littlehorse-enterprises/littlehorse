package io.littlehorse.common.model;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;

public abstract class POSTable<T extends MessageOrBuilder> extends GETable<T> {
    public abstract void handlePost(POSTable<T> old)
    throws LHValidationError, LHConnectionError;

    // TODO: Need to think about how to make wait's transactional.
    public abstract boolean handleDelete()
    throws LHValidationError, LHConnectionError;

    public static String getRequestTopicName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName();
    }

    public static String getEntitytTopicName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_Entity";
    }
}
