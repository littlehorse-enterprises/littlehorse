package io.littlehorse.common;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
// import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;

public abstract class Storeable<T extends Message> extends LHSerializable<T> {

    public static final String GROUPED_WF_RUN_PREFIX = "wrg";

    public String getFullStoreKey() {
        if (getGroupingWfRunId().isPresent()) {
            if (!(this instanceof StoredGetable)) {
                // TODO: We'lll have to be fancy for the WfRunStoredInventory
                throw new NotImplementedException();
            }
            StoredGetable sg = (StoredGetable) this;
            return getGroupedFullStoreKey(getGroupingWfRunId().get(), getType(), sg.getObjectType(), getStoreKey());
        } else {
            return getFullStoreKey(getType(), getStoreKey());
        }
    }

    public static String getSubstorePrefix(StoreableType storeableType) {
        return String.valueOf(storeableType.getNumber()) + "/";
    }

    public static String stripPrefix(String fullStoreKey) {
        return fullStoreKey.substring(fullStoreKey.indexOf('/') + 1);
    }

    public static String getFullStoreKey(StoreableType type, String storeKey) {
        return getSubstorePrefix(type) + storeKey;
    }

    public static String getGroupedFullStoreKey(
            WfRunIdModel wfRunId, StoreableType type, GetableClassEnum getableType, String storeKey) {
        return GROUPED_WF_RUN_PREFIX + "/" + wfRunId + "/" + type.getNumber() + "/" + getableType.getNumber() + "/"
                + storeKey;
    }

    public static String getGroupedPrefixKey(String wfRunId, StoreableType type, GetableClassEnum getableType) {
        return GROUPED_WF_RUN_PREFIX + "/" + wfRunId +  type.getNumber() + "/" + getableType.getNumber() + "/";
    }

    public static String getFullStoreKey(Class<? extends Storeable<?>> cls, String storeKey) {
        return getSubstorePrefix(getStoreableType(cls)) + storeKey;
    }

    public abstract String getStoreKey();

    public abstract StoreableType getType();

    public Optional<WfRunIdModel> getGroupingWfRunId() {
        return Optional.empty();
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
