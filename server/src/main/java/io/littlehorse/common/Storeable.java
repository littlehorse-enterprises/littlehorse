package io.littlehorse.common;

import com.google.protobuf.Message;
// import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.proto.StoreableType;

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
            case "TaskMetricUpdateModel":
                return StoreableType.TASK_METRIC_UPDATE;
            case "PartitionMetricsModel":
                return StoreableType.PARTITION_METRICS;
            case "MetricWindowModel":
                return StoreableType.METRIC_WINDOW;
            case "InitializationLogModel":
                return StoreableType.INITIALIZATION_LOG;
            case "WfRunStoredInventoryModel":
                return StoreableType.WFRUN_STORED_INVENTORY;
            case "PartitionMetricInventoryModel":
                return StoreableType.METRIC_PARTITION_INVENTORY;
            case "EventCorrelationMarkerModel":
                return StoreableType.CORRELATION_MARKER;
        }
        throw new IllegalArgumentException("Unrecognized Storeable class: " + cls);
    }
}
