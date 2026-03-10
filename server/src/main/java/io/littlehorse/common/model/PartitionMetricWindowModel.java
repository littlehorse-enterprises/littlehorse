package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.core.metrics.CountAndTimingModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.common.proto.PartitionMetricWindow;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionMetricsMemoryStore;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartitionMetricWindowModel extends Storeable<PartitionMetricWindow> {

    private MetricWindowIdModel id;
    private Map<String, CountAndTimingModel> metrics;

    public PartitionMetricWindowModel() {}

    public PartitionMetricWindowModel(MetricWindowIdModel id) {
        this.id = id;
        this.metrics = new HashMap<>();
    }

    public void incrementCountAndLatency(String metricKey, long latencyMs) {
        CountAndTimingModel timing = metrics.computeIfAbsent(metricKey, k -> new CountAndTimingModel());
        timing.incrementCountAndLatency(latencyMs);
    }

    public void incrementCount(String metricKey) {
        CountAndTimingModel timing = metrics.computeIfAbsent(metricKey, k -> new CountAndTimingModel());
        timing.incrementCount();
    }

    public void mergeFrom(PartitionMetricWindowModel other) {
        if (other == null) {
            return;
        }
        for (Map.Entry<String, CountAndTimingModel> entry : other.getMetrics().entrySet()) {
            String metricKey = entry.getKey();
            CountAndTimingModel otherTiming = entry.getValue();
            CountAndTimingModel thisTiming = metrics.computeIfAbsent(metricKey, k -> new CountAndTimingModel());
            thisTiming.mergeFrom(otherTiming);
        }
    }

    public void incrementWfCount(LHStatus previousStatus, LHStatus newStatus, Date startTime, Date endTime) {
        if (previousStatus == null) {
            incrementCount("started");
        } else {
            if (endTime == null) {
                endTime = new Date();
            }
            long latencyMs = endTime.getTime() - startTime.getTime();
            String metricKey = previousStatus.name().toLowerCase() + "_to_"
                    + newStatus.name().toLowerCase();
            incrementCountAndLatency(metricKey, latencyMs);
        }
    }

    public void incrementTaskAttemptCount(TaskStatus previousStatus, TaskStatus newStatus, Date phaseStart, Date phaseEnd) {
        if (phaseStart == null) return;
        if (phaseEnd == null) phaseEnd = new Date();
        long latencyMs = phaseEnd.getTime() - phaseStart.getTime();
        String prev = previousStatus.name().replace("TASK_", "").toLowerCase();
        String next = newStatus.name().replace("TASK_", "").toLowerCase();
        String metricKey = "taskattempt_" + prev + "_to_" + next;
        incrementCountAndLatency(metricKey, latencyMs);
    }


    public void incrementTaskRunCount(TaskStatus terminalStatus, Date createdAt, Date endTime) {
        if (endTime == null) endTime = new Date();
        long latencyMs = endTime.getTime() - createdAt.getTime();
        String metricKey = switch (terminalStatus) {
            case TASK_SUCCESS -> "taskrun_created_to_completed";
            case TASK_EXCEPTION -> "taskrun_created_to_exception";
            default -> "taskrun_created_to_error";
        };
        incrementCountAndLatency(metricKey, latencyMs);
    }

    public static void trackTaskAttempt(
            CoreProcessorContext processorContext,
            TaskDefIdModel taskDefId,
            TaskStatus previousStatus,
            TaskStatus newStatus,
            Date phaseStart,
            Date phaseEnd) {
        Date windowStart = LHUtil.getCurrentWindowDate();
        TenantIdModel tenantId = processorContext.authorization().tenantId();
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(processorContext.nativeCoreStore(), processorContext);
        PartitionMetricsMemoryStore memoryStore = processorContext.getPartitionMetricsMemoryStore();
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, taskDefId, windowStart);
        PartitionMetricWindowModel metricWindow = memoryStore.get(id.getPartitionMetricStoreKey());
        if (metricWindow == null) {
            metricWindow = clusterScopedStore.get(id.getPartitionMetricStoreKey(), PartitionMetricWindowModel.class);
        }
        if (metricWindow == null) {
            metricWindow = new PartitionMetricWindowModel(id);
        }
        metricWindow.incrementTaskAttemptCount(previousStatus, newStatus, phaseStart, phaseEnd);
        clusterScopedStore.put(metricWindow);
        memoryStore.put(metricWindow);
    }

    public static void trackTaskRun(
            CoreProcessorContext processorContext,
            TaskDefIdModel taskDefId,
            TaskStatus terminalStatus,
            Date createdAt,
            Date endTime) {
        Date windowStart = LHUtil.getCurrentWindowDate();
        TenantIdModel tenantId = processorContext.authorization().tenantId();
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(processorContext.nativeCoreStore(), processorContext);
        PartitionMetricsMemoryStore memoryStore = processorContext.getPartitionMetricsMemoryStore();
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, taskDefId, windowStart);
        PartitionMetricWindowModel metricWindow = memoryStore.get(id.getPartitionMetricStoreKey());
        if (metricWindow == null) {
            metricWindow = clusterScopedStore.get(id.getPartitionMetricStoreKey(), PartitionMetricWindowModel.class);
        }
        if (metricWindow == null) {
            metricWindow = new PartitionMetricWindowModel(id);
        }
        metricWindow.incrementTaskRunCount(terminalStatus, createdAt, endTime);
        clusterScopedStore.put(metricWindow);
        memoryStore.put(metricWindow);
    }

    public static void trackWorkflow(
            CoreProcessorContext processorContext,
            WfSpecIdModel wfSpecId,
            LHStatus previousStatus,
            LHStatus newStatus,
            Date startTime,
            Date endTime) {
        Date windowStart = LHUtil.getCurrentWindowDate();
        TenantIdModel tenantId = processorContext.authorization().tenantId();
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(processorContext.nativeCoreStore(), processorContext);
        PartitionMetricsMemoryStore memoryStore = processorContext.getPartitionMetricsMemoryStore();
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, wfSpecId, windowStart);
        PartitionMetricWindowModel metricWindow = memoryStore.get(id.getPartitionMetricStoreKey());
        if (metricWindow == null) {
            metricWindow = clusterScopedStore.get(id.getPartitionMetricStoreKey(), PartitionMetricWindowModel.class);
        }
        if (metricWindow == null) {
            metricWindow = new PartitionMetricWindowModel(id);
            ;
        }
        metricWindow.incrementWfCount(previousStatus, newStatus, startTime, endTime);
        clusterScopedStore.put(metricWindow);
        memoryStore.put(metricWindow);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PartitionMetricWindow p = (PartitionMetricWindow) proto;
        id = LHSerializable.fromProto(p.getId(), MetricWindowIdModel.class, context);
        metrics = new HashMap<>();
        for (Map.Entry<String, CountAndTiming> entry : p.getMetricsMap().entrySet()) {
            metrics.put(entry.getKey(), LHSerializable.fromProto(entry.getValue(), CountAndTimingModel.class, context));
        }
    }

    @Override
    public PartitionMetricWindow.Builder toProto() {
        PartitionMetricWindow.Builder out = PartitionMetricWindow.newBuilder().setId(id.toProto());

        for (Map.Entry<String, CountAndTimingModel> entry : metrics.entrySet()) {
            out.putMetrics(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public Class<PartitionMetricWindow> getProtoBaseClass() {
        return PartitionMetricWindow.class;
    }

    @Override
    public String getStoreKey() {
        return id.getPartitionMetricStoreKey();
    }

    @Override
    public StoreableType getType() {
        return StoreableType.PARTITION_METRICS;
    }
    
}
