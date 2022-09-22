package io.littlehorse.common.model;

import com.google.protobuf.MessageOrBuilder;

public abstract class GlobalPOSTable<T extends MessageOrBuilder> extends POSTable<T> {

    public abstract String getName();

    public static String getGlobalStoreSourceName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_GlobalStoreSource";
    }

    public static String getGlobalStoreName(Class<? extends POSTable<?>> cls) {
        return cls.getSimpleName() + "_GlobalStore";
    }

    public static String getGlobalStoreProcessorName(
        Class<? extends POSTable<?>> cls
    ) {
        return cls.getSimpleName() + "_GlobalStoreProcessor";
    }
}
