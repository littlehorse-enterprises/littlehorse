package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.core.metrics.CountAndTimingModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.PartitionMetricWindow;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartitionMetricWindowModel extends Storeable<PartitionMetricWindow> {

    private MetricWindowId.IdCase metricType;

    private WfSpecIdModel wfSpecId;

    private Date windowStart;

    private Map<String, CountAndTimingModel> metrics;

    private TenantIdModel tenantId;

    public PartitionMetricWindowModel() {}

    public PartitionMetricWindowModel(WfSpecIdModel wfSpecId, TenantIdModel tenantId, Date windowStart) {
        this.wfSpecId = wfSpecId;
        this.tenantId = tenantId;
        this.windowStart = windowStart;
        this.metricType = MetricWindowId.IdCase.WORKFLOW;
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
        if (previousStatus != null) {
            if (endTime == null) {
                endTime = new Date();
            }
            long latencyMs = endTime.getTime() - startTime.getTime();
            String metricKey = previousStatus.name().toLowerCase() + "_to_"
                    + newStatus.name().toLowerCase();
            incrementCountAndLatency(metricKey, latencyMs);
        } else {
            incrementCount("started");
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

        PartitionMetricWindowModel metricWindow = clusterScopedStore.get(
                buildStoreKey(windowStart, tenantId, wfSpecId), PartitionMetricWindowModel.class);
        if (metricWindow == null) {
            metricWindow = new PartitionMetricWindowModel(wfSpecId, tenantId);
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

        if (p.hasWfSpecId()) {
            this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        }

        this.metricType = MetricWindowId.IdCase.WORKFLOW;
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
        }

        for (Map.Entry<String, CountAndTimingModel> entry : metrics.entrySet()) {
            builder.putMetrics(entry.getKey(), entry.getValue().toProto().build());
        }
        return builder;
    }

    public MetricWindow.Builder toMetricWindowProto() {
        MetricWindow.Builder builder = MetricWindow.newBuilder();
        WfSpecIdModel wfSpecId = this.wfSpecId;
        MetricWindowIdModel id = new MetricWindowIdModel(wfSpecId, this.windowStart);
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
        return buildStoreKey(windowStart, tenantId, wfSpecId);
    }

    public static String buildStoreKey(Date windowStart, TenantIdModel tenantId, WfSpecIdModel wfSpecId) {
        String typeStr = "wf";
        String tenantStr = tenantId != null ? tenantId.toString() : "null";
        String idStr = wfSpecId != null ? wfSpecId.toString() : "null";
        String windowStr = windowStart != null ? LHUtil.toLhDbFormat(windowStart) : "0";
        return String.format("metrics/partition/%s/%s/%s/%s", windowStr, tenantStr, typeStr, idStr);
    }

    private String getMetricTypeString() {
        if (metricType == null) {
            return "wf"; // default to workflow
        }
        switch (metricType) {
            case WORKFLOW:
                return "wf";
            case TASK:
                return "task";
            case NODE:
                return "node";
            default:
                return "unknown";
        }
    }

    @Override
    public StoreableType getType() {
        return StoreableType.PARTITION_METRICS;
    }

    @Override
    public String toString() {
        return " {" + "\nwindowStart=" + windowStart + ",\nmetrics=" + metrics + "\n}";
    }
}
