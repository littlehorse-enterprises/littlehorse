package io.littlehorse.common;

import com.google.protobuf.Message;

import io.littlehorse.common.model.PartitionMetricsModel;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.WfMetricUpdateModel;
import io.littlehorse.common.model.getable.core.init.InitializationLogModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdateModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.EventCorrelationMarkerModel;
import io.littlehorse.server.streams.storeinternals.WfRunStoredInventoryModel;
import io.littlehorse.server.streams.storeinternals.index.Tag;

public abstract class Storeable<T extends Message> extends LHSerializable<T> {

    public String getFullStoreKey() {
        if (WfRunGroupedObject.class.isAssignableFrom(this.getClass())) {
            WfRunGroupedObject wgo = (WfRunGroupedObject) this;
            WfRunIdModel groupingId = wgo.getGroupingWfRunId();
            String restOfKey = wgo.getKeySuffix();
            return getGroupedStoreKey(groupingId, getType(), restOfKey);
        } else {
            return getUngroupedStoreKey(getType(), getStoreKey());
        }
    }

    public String legacyGetFulllStoreKey() {
        return getUngroupedStoreKey(getType(), getStoreKey());
    }

    private static String getUngroupedSubstorePrefix(StoreableType storeableType) {
        return String.valueOf(storeableType.getNumber()) + "/";
    }

    public static String getUngroupedStoreKey(StoreableType type, String storeKey) {
        return getUngroupedSubstorePrefix(type) + storeKey;
    }

    public static String getGroupedStoreKey(WfRunIdModel wfRunId, StoreableType type, String restOfKey) {
        return "wrg_/" + wfRunId.toString() + "/" + type.getNumber() + "/" + restOfKey;
    }

    // public static String getFullStoreKey(Class<? extends Storeable<?>> cls, String storeKey) {
    //     return getUngroupedSubstorePrefix(getStoreableType(cls)) + storeKey;
    // }

    public abstract String getStoreKey();

    public abstract StoreableType getType();

    public static StoreableType getStoreableType(Class<? extends Storeable<?>> cls) {
        switch (cls.getSimpleName()) {
            case "StoredGetable":
                return StoreableType.STORED_GETABLE;
            case "Tag":
                return StoreableType.TAG;
            case "ScheduledTaskModel":
                return StoreableType.SCHEDULED_TASK;
            case "TaskMetricUpdateModel":
                return StoreableType.TASK_METRIC_UPDATE;
            case "PartitionMetricsModel":
                return StoreableType.PARTITION_METRICS;
            case "InitializationLogModel":
                return StoreableType.INITIALIZATION_LOG;
            case "WfRunStoredInventoryModel":
                return StoreableType.WFRUN_STORED_INVENTORY;
            case "EventCorrelationMarkerModel":
                return StoreableType.CORRELATION_MARKER;
        }
        throw new IllegalArgumentException("Unrecognized Storeable class: " + cls);
    }

    public static Class<? extends Storeable<?>> getStoreableCls(StoreableType storeableType) {
        switch (storeableType) {
            case STORED_GETABLE:
                return (Class<? extends Storeable<?>>) StoredGetable.class;
            case TAG:
                return Tag.class;
            case SCHEDULED_TASK:
                return ScheduledTaskModel.class;
            case TASK_METRIC_UPDATE:
                return TaskMetricUpdateModel.class;
            case WF_METRIC_UPDATE:
                return WfMetricUpdateModel.class;
            case PARTITION_METRICS:
                return PartitionMetricsModel.class; 
            case INITIALIZATION_LOG:
                return InitializationLogModel.class;
            case WFRUN_STORED_INVENTORY:
                return WfRunStoredInventoryModel.class;
            case CORRELATION_MARKER:
                return EventCorrelationMarkerModel.class;
            case UNRECOGNIZED:
        }
        throw new IllegalStateException("Unrecognized storeable type");
    }
}
