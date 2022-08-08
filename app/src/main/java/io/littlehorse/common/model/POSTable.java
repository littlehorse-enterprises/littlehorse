package io.littlehorse.common.model;

import java.util.Arrays;
import java.util.List;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;

public abstract class POSTable<T extends MessageOrBuilder> extends GETable<T> {
    public abstract void handlePost(POSTable<T> old, LHDatabaseClient client)
    throws LHValidationError, LHConnectionError;

    // TODO: Need to think about how to make wait's transactional.
    public abstract boolean handleDelete()
    throws LHValidationError, LHConnectionError;

    public static final List<Class<? extends POSTable<?>>> POSTables = Arrays.asList(
        WfSpec.class, TaskDef.class
    );

    public static String getRequestTopicName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName();
    }

    public static String getEntitytTopicName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_Entity";
    }

    public static String getTopoSourceName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_Source";
    }

    public static String getTopoProcessorName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_Processor";
    }

    public static String getIdxFanoutProcessorName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_IndexFanoutProcessor";
    }

    public static String getIdxSinkName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_IndexSink";
    }

    public static String getEntitySinkName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_EntitySink";
    }

    public static String getResponseStoreName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_ResponseStore";
    }
}
