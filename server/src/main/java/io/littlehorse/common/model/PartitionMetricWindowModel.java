package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.core.metrics.CountAndTimingModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.PartitionMetricWindow;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricWindowType;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
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

    private WfSpecIdModel wfSpecId;
    private TaskDefIdModel taskDefId;
    private UserTaskDefIdModel userTaskDefId;

    private MetricWindowType metricType;
    private Date windowStart;
    private Map<String, CountAndTimingModel> metrics;
    private TenantIdModel tenantId;

    public PartitionMetricWindowModel() {}

    public PartitionMetricWindowModel(WfSpecIdModel wfSpecId, TenantIdModel tenantId, Date windowStart) {
        this.wfSpecId = wfSpecId;
        this.tenantId = tenantId;
        this.windowStart = windowStart;
        this.metricType = MetricWindowType.WORKFLOW_METRIC;
        this.metrics = new HashMap<>();
    }

    public PartitionMetricWindowModel(WfSpecIdModel wfSpecId, TenantIdModel tenantId) {
        this(wfSpecId, tenantId, LHUtil.getCurrentWindowTime());
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

    public static void trackWorkflow(
            CoreProcessorContext processorContext,
            WfSpecIdModel wfSpecId,
            LHStatus previousStatus,
            LHStatus newStatus,
            Date startTime,
            Date endTime) {
        Date windowStart = LHUtil.getCurrentWindowTime();
        TenantIdModel tenantId = processorContext.authorization().tenantId();
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(processorContext.nativeCoreStore(), processorContext);
        PartitionMetricWindowModel metricWindow = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        PartitionMetricWindowModel existingWindow =
                clusterScopedStore.get(metricWindow.getStoreKey(), PartitionMetricWindowModel.class);
        if (existingWindow != null) {
            metricWindow = existingWindow;
        }
        metricWindow.incrementWfCount(previousStatus, newStatus, startTime, endTime);
        clusterScopedStore.put(metricWindow);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PartitionMetricWindow p = (PartitionMetricWindow) proto;

        if (p.hasWindowStart()) {
            this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        }

        if (p.hasTenantId()) {
            this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        }

        // Handle oneof id
        switch (p.getIdCase()) {
            case WF_SPEC_ID:
                this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
                break;
            case TASK_DEF_ID:
                this.taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
                break;
            case USER_TASK_DEF_ID:
                this.userTaskDefId = LHSerializable.fromProto(p.getUserTaskDefId(), UserTaskDefIdModel.class, context);
                break;
            case ID_NOT_SET:
                break;
        }

        this.metricType = p.getMetricType();

        this.metrics = new HashMap<>();
        for (Map.Entry<String, CountAndTiming> entry : p.getMetricsMap().entrySet()) {
            CountAndTimingModel model = new CountAndTimingModel();
            model.setCount(entry.getValue().getCount());
            model.setMinLatencyMs(entry.getValue().getMinLatencyMs());
            model.setMaxLatencyMs(entry.getValue().getMaxLatencyMs());
            model.setTotalLatencyMs(entry.getValue().getTotalLatencyMs());
            this.metrics.put(entry.getKey(), model);
        }
    }

    @Override
    public PartitionMetricWindow.Builder toProto() {
        PartitionMetricWindow.Builder builder = PartitionMetricWindow.newBuilder();
        if (windowStart != null) {
            builder.setWindowStart(LHUtil.fromDate(windowStart));
        }
        if (tenantId != null) {
            builder.setTenantId(tenantId.toProto());
        }
        if (wfSpecId != null) {
            builder.setWfSpecId(wfSpecId.toProto());
        } else if (taskDefId != null) {
            builder.setTaskDefId(taskDefId.toProto());
        } else if (userTaskDefId != null) {
            builder.setUserTaskDefId(userTaskDefId.toProto());
        }
        if (metricType != null) {
            builder.setMetricType(metricType);
        }
        for (Map.Entry<String, CountAndTimingModel> entry : metrics.entrySet()) {
            builder.putMetrics(entry.getKey(), entry.getValue().toProto().build());
        }
        return builder;
    }

    public MetricWindow.Builder toMetricWindowProto() {
        MetricWindow.Builder builder = MetricWindow.newBuilder();
        MetricWindowIdModel id = new MetricWindowIdModel();
        id.setWfSpecId(this.wfSpecId);
        id.setTaskDefId(this.taskDefId);
        id.setUserTaskDefId(this.userTaskDefId);
        id.setWindowStart(this.windowStart);
        id.setMetricType(this.metricType);
        builder.setId(id.toProto());
        this.metrics.forEach((key, val) -> {
            builder.putMetrics(key, val.toProto().build());
        });
        return builder;
    }

    @Override
    public Class<PartitionMetricWindow> getProtoBaseClass() {
        return PartitionMetricWindow.class;
    }

    @Override
    public String getStoreKey() {
        String idPart = "";
        if (wfSpecId != null) {
            idPart = wfSpecId.toString();
        } else if (taskDefId != null) {
            idPart = taskDefId.toString();
        } else if (userTaskDefId != null) {
            idPart = userTaskDefId.toString();
        }
        return String.format(
                "%s/%s/%s/%s/%s",
                LHConstants.PARTITION_METRICS_KEY,
                LHUtil.toLhDbFormat(windowStart),
                getMetricType().name(),
                tenantId,
                idPart);
    }

    @Override
    public StoreableType getType() {
        return StoreableType.PARTITION_METRICS;
    }
}
