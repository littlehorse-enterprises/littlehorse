package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.PartitionMetricWindow;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MetricWindowId;
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

    public PartitionMetricWindowModel(
            WfSpecIdModel wfSpecId, Date windowStart, TenantIdModel tenantId) {
        this.metricType = MetricWindowId.IdCase.WORKFLOW;
        this.wfSpecId = wfSpecId;
        this.windowStart = alignToMinute(windowStart);
        this.metrics = new HashMap<>();
        this.tenantId = tenantId;
    }

    private Date alignToMinute(Date date) {
        long timestamp = date.getTime();
        long minuteInMs = 60 * 1000;
        long aligned = (timestamp / minuteInMs) * minuteInMs;
        return new Date(aligned);
    }

    public void addMetric(String metricKey, int count, long latencyMs) {
        CountAndTimingModel timing = metrics.computeIfAbsent(metricKey, k -> new CountAndTimingModel());
        timing.add(count, latencyMs);
    }

    public void mergeFrom(PartitionMetricWindowModel other) {
        if (other == null) {
            return;
        }

        for (Map.Entry<String, CountAndTimingModel> entry : other.getMetrics().entrySet()) {
            String metricKey = entry.getKey();
            CountAndTimingModel otherTiming = entry.getValue();

            CountAndTimingModel thisTiming = metrics.computeIfAbsent(metricKey, k -> new CountAndTimingModel());
            thisTiming.add(otherTiming.getCount(), 0); // Add count

            if (otherTiming.getMinLatencyMs() < thisTiming.getMinLatencyMs()) {
                thisTiming.setMinLatencyMs(otherTiming.getMinLatencyMs());
            }
            if (otherTiming.getMaxLatencyMs() > thisTiming.getMaxLatencyMs()) {
                thisTiming.setMaxLatencyMs(otherTiming.getMaxLatencyMs());
            }
            thisTiming.setTotalLatencyMs(thisTiming.getTotalLatencyMs() + otherTiming.getTotalLatencyMs());
        }
    }

    public void trackWfRun(LHStatus status, Date startTime, Date endTime) {
        long latencyMs = 0;
        if (startTime != null && endTime != null) {
            latencyMs = endTime.getTime() - startTime.getTime();
        }

        switch (status) {
            case RUNNING -> addMetric("started", 1, latencyMs);
            case COMPLETED -> addMetric("running_to_completed", 1, latencyMs);
            case HALTED -> addMetric("running_to_halted", 1, latencyMs);
            case EXCEPTION -> addMetric("running_to_exception", 1, latencyMs);
            case ERROR -> addMetric("running_to_error", 1, latencyMs);
            default -> {}
        }
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
            builder.putMetrics(entry.getKey(), entry.getValue().toProto());
        }
        return builder;
    }

    @Override
    public Class<PartitionMetricWindow> getProtoBaseClass() {
        return PartitionMetricWindow.class;
    }

    @Override
    public String getStoreKey() {
        // Format: metrics/window/{windowStart}/{tenantId}/{type}/{specId}
        String typeStr = getMetricTypeString();
        String tenantStr = tenantId != null ? tenantId.toString() : "null";
        String idStr = wfSpecId != null ? wfSpecId.toString() : "null";
        String windowStr = windowStart != null ? LHUtil.toLhDbFormat(windowStart) : "0";
        return String.format("metrics/%s/%s/%s/%s", windowStr, tenantStr, typeStr, idStr);
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

    @Getter
    public static class CountAndTimingModel {
        private int count = 0;
        private long minLatencyMs = Long.MAX_VALUE;
        private long maxLatencyMs = 0;
        private long totalLatencyMs = 0;

        public void add(int countToAdd, long latencyMs) {
            this.count += countToAdd;
            if (latencyMs > 0) {
                this.minLatencyMs = Math.min(this.minLatencyMs, latencyMs);
                this.maxLatencyMs = Math.max(this.maxLatencyMs, latencyMs);
                this.totalLatencyMs += latencyMs;
            }
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setMinLatencyMs(long minLatencyMs) {
            this.minLatencyMs = minLatencyMs;
        }

        public void setMaxLatencyMs(long maxLatencyMs) {
            this.maxLatencyMs = maxLatencyMs;
        }

        public void setTotalLatencyMs(long totalLatencyMs) {
            this.totalLatencyMs = totalLatencyMs;
        }

        public CountAndTiming toProto() {
            return CountAndTiming.newBuilder()
                    .setCount(count)
                    .setMinLatencyMs(minLatencyMs == Long.MAX_VALUE ? 0 : minLatencyMs)
                    .setMaxLatencyMs(maxLatencyMs)
                    .setTotalLatencyMs(totalLatencyMs)
                    .build();
        }
    }
}
