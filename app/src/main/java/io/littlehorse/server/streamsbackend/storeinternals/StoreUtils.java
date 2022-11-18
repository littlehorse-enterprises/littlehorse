package io.littlehorse.server.streamsbackend.storeinternals;

import io.littlehorse.common.model.GETable;

public class StoreUtils {

    public static String getStoreKey(GETable<?> thing) {
        return thing.getClass().getSimpleName() + "/" + thing.getObjectId();
    }

    public static String getStoreKey(
        String objectId,
        Class<? extends GETable<?>> cls
    ) {
        return cls.getSimpleName() + "/" + objectId;
    }
}
