package io.littlehorse.common;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
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

    /**
     * See Proposal #9.
     */
    public static String getLegacyWfRunStoredInventoryModelKey(WfRunIdModel wfRunId) {
        return getSubstorePrefix(StoreableType.WFRUN_STORED_INVENTORY) + wfRunId.toString();
    }

    public static String getFullStoreKey(StoreableType type, String storeKey) {
        // See Proposal #9
        if (type == StoreableType.WFRUN_STORED_INVENTORY) {
            // We want the WfRunStoredInventory to live on the same block as the WfRun/NodeRun/TaskRun/etc
            // so it must share the same prefix, which means it needs to live under the StoredGetable
            // prefix.
            return getSubstorePrefix(StoreableType.STORED_GETABLE) + storeKey + "/wrsi/";
        } else {
            return getSubstorePrefix(type) + storeKey;
        }
    }

    public static String getFullStoreKey(Class<? extends Storeable<?>> cls, String storeKey) {
        return getFullStoreKey(getStoreableType(cls), storeKey);
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
            case "EventCorrelationMarkerModel":
                return StoreableType.CORRELATION_MARKER;
        }
        throw new IllegalArgumentException("Unrecognized Storeable class: " + cls);
    }
}
