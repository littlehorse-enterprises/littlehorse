package io.littlehorse.common;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
// import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;

public abstract class Storeable<T extends Message> extends LHSerializable<T> {

    public String getFullStoreKey() {
        return getFullStoreKey(getType(), getStoreKey());
    }

    public static String getSubstorePrefix(StoreableType storeableType) {
        return String.valueOf(storeableType.getNumber()) + "/";
    }

    public static String stripPrefix(String fullStoreKey) {
        return fullStoreKey.substring(fullStoreKey.indexOf('/') + 1);
    }

    public static String getSubstorePrefix(Class<? extends Storeable<?>> cls) {
        return getSubstorePrefix(getStoreableType(cls));
    }

    public static String getFullStoreKey(StoreableType type, String storeKey) {
        return getSubstorePrefix(type) + storeKey;
    }

    public static String getFullStoreKey(Class<? extends Storeable<?>> cls, String storeKey) {
        return getSubstorePrefix(getStoreableType(cls)) + storeKey;
    }

    public abstract String getStoreKey();

    public abstract StoreableType getType();

    public static Class<? extends Storeable<?>> getStoreableClass(StoreableType type) {
        switch (type) {
            case STORED_GETABLE:
                return (Class<? extends Storeable<?>>) StoredGetable.class;
            case SCHEDULED_TASK:
                return (Class<? extends Storeable<?>>) ScheduledTaskModel.class;
            case WF_METRIC_UPDATE:
                return (Class<? extends Storeable<?>>) WfMetricUpdate.class;
            case TASK_METRIC_UPDATE:
                return (Class<? extends Storeable<?>>) TaskMetricUpdate.class;
            case TAG:
                return (Class<? extends Storeable<?>>) Tag.class;
            case LH_TIMER: // apparently LHTimer isn't storeable
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("unexpected storeable type");
    }

    public static StoreableType getStoreableType(Class<? extends Storeable<?>> cls) {
        switch (cls.getSimpleName()) {
            case "StoredGetable":
                return StoreableType.STORED_GETABLE;
            case "LHTimer":
                return StoreableType.LH_TIMER;
            case "Tag":
                return StoreableType.TAG;
            case "ScheduledTaskModel":
                return StoreableType.SCHEDULED_TASK;
            case "TaskMetricUpdate":
                return StoreableType.TASK_METRIC_UPDATE;
            case "PartitionMetricsModel":
                return StoreableType.PARTITION_METRICS;
            case "MetricWindowModel":
                return StoreableType.METRIC_WINDOW;
        }
        throw new IllegalArgumentException("Unrecognized Storeable class: " + cls);
    }
}
