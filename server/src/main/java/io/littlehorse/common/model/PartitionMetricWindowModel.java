package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.core.metrics.CountAndTimingModel;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.PartitionMetricWindow;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
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
            incrementCount(MetricWindowModel.STARTED);
            return;
        }
        endTime = endTime == null ? new Date() : endTime;
        long latencyMs = endTime.getTime() - startTime.getTime();
        String metricKey;
        if (previousStatus == LHStatus.RUNNING && newStatus == LHStatus.COMPLETED) {
            metricKey = MetricWindowModel.RUNNING_TO_COMPLETED;
        } else if (previousStatus == LHStatus.RUNNING && newStatus == LHStatus.ERROR) {
            metricKey = MetricWindowModel.RUNNING_TO_ERROR;
        } else if (previousStatus == LHStatus.RUNNING && newStatus == LHStatus.EXCEPTION) {
            metricKey = MetricWindowModel.RUNNING_TO_EXCEPTION;
        } else if (previousStatus == LHStatus.RUNNING && newStatus == LHStatus.HALTING) {
            metricKey = MetricWindowModel.RUNNING_TO_HALTING;
        } else if (previousStatus == LHStatus.HALTING && newStatus == LHStatus.HALTED) {
            metricKey = MetricWindowModel.HALTING_TO_HALTED;
        } else {
            metricKey = previousStatus.name().toLowerCase() + "_to_"
                    + newStatus.name().toLowerCase();
        }

        incrementCountAndLatency(metricKey, latencyMs);
    }

    public void incrementTaskAttemptCount(
            TaskStatus previousStatus, TaskStatus newStatus, Date startTime, Date endTime) {
        if (startTime == null) return;
        endTime = endTime == null ? new Date() : endTime;
        long latencyMs = endTime.getTime() - startTime.getTime();

        String metricKey;
        if (previousStatus == TaskStatus.TASK_PENDING && newStatus == TaskStatus.TASK_SCHEDULED) {
            metricKey = MetricWindowModel.TASKATTEMPT_PENDING_TO_SCHEDULED;
        } else if (previousStatus == TaskStatus.TASK_SCHEDULED && newStatus == TaskStatus.TASK_RUNNING) {
            metricKey = MetricWindowModel.TASKATTEMPT_SCHEDULED_TO_RUNNING;
        } else if (previousStatus == TaskStatus.TASK_RUNNING && newStatus == TaskStatus.TASK_SUCCESS) {
            metricKey = MetricWindowModel.TASKATTEMPT_RUNNING_TO_SUCCESS;
        } else if (previousStatus == TaskStatus.TASK_RUNNING && newStatus == TaskStatus.TASK_EXCEPTION) {
            metricKey = MetricWindowModel.TASKATTEMPT_RUNNING_TO_EXCEPTION;
        } else {
            String prev = previousStatus.name().replace("TASK_", "").toLowerCase();
            String next = newStatus.name().replace("TASK_", "").toLowerCase();
            metricKey = "taskattempt_" + prev + "_to_" + next;
        }

        incrementCountAndLatency(metricKey, latencyMs);
    }

    public void incrementTaskRunCount(TaskStatus endStatus, Date startTime, Date endTime) {
        endTime = endTime == null ? new Date() : endTime;
        long latencyMs = endTime.getTime() - startTime.getTime();
        String metricKey =
                switch (endStatus) {
                    case TASK_SUCCESS -> MetricWindowModel.TASKRUN_CREATED_TO_COMPLETED;
                    case TASK_EXCEPTION -> MetricWindowModel.TASKRUN_CREATED_TO_EXCEPTION;
                    default -> MetricWindowModel.TASKRUN_CREATED_TO_ERROR;
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
        TenantIdModel tenantId = processorContext.authorization().tenantId();
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, taskDefId, LHUtil.getCurrentWindowDate());
        PartitionMetricWindowModel metricWindow = getOrcreateMetricWindow(processorContext, id);
        metricWindow.incrementTaskAttemptCount(previousStatus, newStatus, phaseStart, phaseEnd);
        storeMetricWindow(processorContext, metricWindow);
    }

    public static void trackTaskRun(
            CoreProcessorContext processorContext,
            TaskDefIdModel taskDefId,
            TaskStatus terminalStatus,
            Date createdAt,
            Date endTime) {
        TenantIdModel tenantId = processorContext.authorization().tenantId();
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, taskDefId, LHUtil.getCurrentWindowDate());
        PartitionMetricWindowModel metricWindow = getOrcreateMetricWindow(processorContext, id);
        metricWindow.incrementTaskRunCount(terminalStatus, createdAt, endTime);
        storeMetricWindow(processorContext, metricWindow);
    }

    public static void trackWorkflow(
            CoreProcessorContext processorContext,
            WfSpecIdModel wfSpecId,
            LHStatus previousStatus,
            LHStatus newStatus,
            Date startTime,
            Date endTime) {
        TenantIdModel tenantId = processorContext.authorization().tenantId();
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, wfSpecId, LHUtil.getCurrentWindowDate());
        PartitionMetricWindowModel metricWindow = getOrcreateMetricWindow(processorContext, id);
        metricWindow.incrementWfCount(previousStatus, newStatus, startTime, endTime);
        storeMetricWindow(processorContext, metricWindow);
    }

    private static void storeMetricWindow(
            CoreProcessorContext processorContext, PartitionMetricWindowModel metricWindow) {
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(processorContext.nativeCoreStore(), processorContext);
        PartitionMetricsMemoryStore memoryStore = processorContext.getPartitionMetricsMemoryStore();
        clusterScopedStore.put(metricWindow);
        memoryStore.put(metricWindow);
    }

    private static PartitionMetricWindowModel getOrcreateMetricWindow(
            CoreProcessorContext processorContext, MetricWindowIdModel id) {
        PartitionMetricsMemoryStore memoryStore = processorContext.getPartitionMetricsMemoryStore();
        PartitionMetricWindowModel metricWindow = memoryStore.get(id.getPartitionMetricStoreKey());
        if (metricWindow == null) {
            ClusterScopedStore clusterScopedStore =
                    ClusterScopedStore.newInstance(processorContext.nativeCoreStore(), processorContext);
            metricWindow = clusterScopedStore.get(id.getPartitionMetricStoreKey(), PartitionMetricWindowModel.class);
        }
        if (metricWindow == null) {
            metricWindow = new PartitionMetricWindowModel(id);
        }
        return metricWindow;
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
