package io.littlehorse.server.streamsimpl.storeinternals.utils;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;

public class StoreUtils {

    @SuppressWarnings("unchecked")
    public static String getFullStoreKey(Storeable<?> thing) {
        return (
            getSubstorePrefix((Class<? extends Storeable<?>>) thing.getClass()) +
            thing.getStoreKey()
        );
    }

    public static <T extends Message, U extends Getable<T>> String getFullStoreKey(
        ObjectId<?, T, U> objectId,
        Class<? extends Storeable<T>> cls
    ) {
        return getSubstorePrefix(cls) + objectId.getStoreKey();
    }

    public static String getFullStoreKey(
        String objectId,
        Class<? extends Storeable<?>> cls
    ) {
        return getSubstorePrefix(cls) + objectId;
    }

    public static String getFullPrefixByName(
        String name,
        Class<? extends Storeable<?>> cls
    ) {
        return getSubstorePrefix(cls) + name + "/";
    }

    @SuppressWarnings("unchecked")
    public static String getSubstorePrefix(Class<? extends Storeable<?>> cls) {
        if (Getable.class.isAssignableFrom(cls)) {
            return (
                "" +
                Getable.getTypeEnum((Class<? extends Getable<?>>) cls).getNumber() +
                "/"
            );
        }

        if (cls.equals(Tag.class)) {
            return "TG/";
        } else if (cls.equals(ScheduledTask.class)) {
            return "ST/";
        } else if (cls.equals(TagsCache.class)) {
            return "TC/";
        } else if (cls.equals(TaskMetricUpdate.class)) {
            return "TM/";
        } else if (cls.equals(WfMetricUpdate.class)) {
            return "WM/";
        } else {
            throw new IllegalArgumentException(
                "Unrecognized Storeable Class: " + cls.getName()
            );
        }
    }

    public static String getFullStoreKey(String objectId, GetableClassEnumPb type) {
        return getFullStoreKey(objectId, Getable.getCls(type));
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

    @SuppressWarnings("unchecked")
    public static String getTagsCacheKey(Getable<?> thing) {
        Class<Getable<?>> cls = (Class<Getable<?>>) thing.getClass();
        return getTagsCacheKey(thing.getStoreKey(), cls);
    }

    public static String getTagsCacheKey(
        String getableId,
        Class<? extends Getable<?>> cls
    ) {
        return "TC/" + cls.getSimpleName() + "/" + getableId;
    }
}
