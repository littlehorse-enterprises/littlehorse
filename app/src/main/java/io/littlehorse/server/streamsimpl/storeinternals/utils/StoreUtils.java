package io.littlehorse.server.streamsimpl.storeinternals.utils;

import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.GETableClassEnumPb;

public class StoreUtils {

    public static String getFullStoreKey(Storeable<?> thing) {
        return thing.getClass().getSimpleName() + "/" + thing.getObjectId();
    }

    public static String getFullStoreKey(
        String objectId,
        Class<? extends Storeable<?>> cls
    ) {
        return cls.getSimpleName() + "/" + objectId;
    }

    public static String getFullStoreKey(String objectId, GETableClassEnumPb type) {
        return getFullStoreKey(objectId, GETable.getCls(type));
    }

    /*
     * Strips the type prefix and returns the objectId or subStoreKey
     */
    public static String stripPrefix(String compositeKey) {
        return compositeKey.substring(compositeKey.indexOf("/") + 1);
    }

    public static String getResponseKey(String commandId) {
        return "Response/" + commandId;
    }

    public static String getTagsCacheKey(GETable<?> thing) {
        return "TagCache-" + thing.getClass() + "/" + thing.getObjectId();
    }
}
