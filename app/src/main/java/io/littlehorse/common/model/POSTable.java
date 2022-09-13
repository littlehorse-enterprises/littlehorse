package io.littlehorse.common.model;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.Arrays;
import java.util.List;

public abstract class POSTable<T extends MessageOrBuilder> extends GETable<T> {

    public abstract void handlePost(
        POSTable<T> old,
        LHGlobalMetaStores client,
        LHConfig config
    ) throws LHValidationError;

    // TODO: Need to think about how to make wait's transactional.
    public abstract boolean handleDelete()
        throws LHValidationError, LHConnectionError;

    public static final List<Class<? extends POSTable<?>>> POSTables = Arrays.asList(
        WfSpec.class,
        TaskDef.class
    );

    public static String getRequestTopicName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_Requests";
    }

    public static String getEntityTopicName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_Entity";
    }

    public static String getTopoSourceName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_Source";
    }

    public static String getTopoProcessorName(
        Class<? extends POSTable<?>> cls
    ) {
        return cls.getSimpleName() + "_Processor";
    }

    public static String getIdxFanoutProcessorName(
        Class<? extends POSTable<?>> cls
    ) {
        return cls.getSimpleName() + "_IndexFanoutProcessor";
    }

    public static String getIdxSinkName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_IndexSink";
    }

    public static String getEntitySinkName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_EntitySink";
    }

    public static String getResponseStoreName(
        Class<? extends POSTable<?>> cls
    ) {
        return cls.getSimpleName() + "_ResponseStore";
    }

    public static String getGlobalStoreSourceName(
        Class<? extends POSTable<?>> cls
    ) {
        // TODO: this should throw exception if used with invalid type.
        return cls.getSimpleName() + "_GlobalStoreSource";
    }

    public static String getGlobalStoreName(Class<? extends POSTable<?>> cls) {
        // TODO: this should throw exception if used with invalid type.
        return cls.getSimpleName() + "_GlobalStore";
    }

    public static String getGlobalStoreProcessorName(
        Class<? extends POSTable<?>> cls
    ) {
        // TODO: this should throw exception if used with invalid type.
        return cls.getSimpleName() + "_GlobalStoreProcessor";
    }
}
