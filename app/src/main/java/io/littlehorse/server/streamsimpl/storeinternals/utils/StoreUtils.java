package io.littlehorse.server.streamsimpl.storeinternals.utils;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.GETableClassEnumPb;

public class StoreUtils {

    public static String getFullStoreKey(Storeable<?> thing) {
        return thing.getClass().getSimpleName() + "/" + thing.getStoreKey();
    }

    public static <T extends Message, U extends GETable<T>> String getFullStoreKey(
        ObjectId<?, T, U> objectId,
        Class<? extends Storeable<T>> cls
    ) {
        return cls.getSimpleName() + "/" + objectId.getStoreKey();
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
        return "TagCache-" + thing.getClass() + "/" + thing.getStoreKey();
    }
}
