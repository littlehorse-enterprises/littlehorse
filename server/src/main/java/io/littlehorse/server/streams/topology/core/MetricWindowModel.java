package io.littlehorse.server.streams.topology.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.sdk.common.proto.WorkflowMetricId;
import lombok.Getter;

public class MetricWindowModel extends Storeable<MetricWindow> {
    
    private MetricWindowId.IdCase metricType;
    private WfSpecIdModel wfSpecId;
    private Date windowStart;
    private Map<String, CountAndTimingModel> metrics = new HashMap<>();
    
    public MetricWindowModel() {}
    
    public MetricWindowModel(WfSpecIdModel wfSpecId, Date windowStart) {
        this.metricType = MetricWindowId.IdCase.WORKFLOW;
        this.wfSpecId = wfSpecId;
        this.windowStart = alignToMinute(windowStart);
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
    

    public void trackWfRun(io.littlehorse.sdk.common.proto.LHStatus status, Date startTime, Date endTime) {
        long latencyMs = 0;
        if (startTime != null && endTime != null) {
            latencyMs = endTime.getTime() - startTime.getTime();
        }
        
        switch (status) {
            case RUNNING:
                addMetric("started", 1, latencyMs);
                break;
            case COMPLETED:
                addMetric("running_to_completed", 1, latencyMs);
                break;
            case HALTED:
                addMetric("running_to_halted", 1, latencyMs);
                break;
            case EXCEPTION:
                addMetric("running_to_exception", 1, latencyMs);
                break;
            case ERROR:
                addMetric("running_to_error", 1, latencyMs);
                break;
            default:
                // No metric for this status
                break;
        }
    }
    
    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        MetricWindow p = (MetricWindow) proto;
        
        if (p.hasId()) {
            MetricWindowId id = p.getId();
            this.metricType = id.getIdCase();
            if (id.hasWorkflow()) {
                WorkflowMetricId workflowId = id.getWorkflow();
                if (workflowId.hasWfSpec()) {
                    this.wfSpecId = LHSerializable.fromProto(workflowId.getWfSpec(), WfSpecIdModel.class, context);
                }
            }
            if (id.hasWindowStart()) {
                this.windowStart = LHUtil.fromProtoTs(id.getWindowStart());
            }
        }
        
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
    public MetricWindow.Builder toProto() {
        MetricWindow.Builder builder = MetricWindow.newBuilder();
        
        WorkflowMetricId.Builder workflowIdBuilder = WorkflowMetricId.newBuilder();
        if (wfSpecId != null) {
            workflowIdBuilder.setWfSpec(wfSpecId.toProto());
        }
        
        MetricWindowId.Builder idBuilder = MetricWindowId.newBuilder()
                .setWorkflow(workflowIdBuilder.build());
        
        if (windowStart != null) {
            idBuilder.setWindowStart(LHUtil.fromDate(windowStart));
        }
        
        builder.setId(idBuilder.build());
        
        for (Map.Entry<String, CountAndTimingModel> entry : metrics.entrySet()) {
            builder.putMetrics(entry.getKey(), entry.getValue().toProto());
        }
        return builder;
    }
    
    @Override
    public Class<MetricWindow> getProtoBaseClass() {
        return MetricWindow.class;
    }
    
    @Override
    public String getStoreKey() {
        // Format: metrics/{type}/partition/{id}/{windowStart}
        // type: wf, task, or node
        // partition: literal string for future repartitioning/consolidation
        String typeStr = getMetricTypeString();
        String idStr = wfSpecId != null ? wfSpecId.toString() : "null";
        String windowStr = windowStart != null ? LHUtil.toLhDbFormat(windowStart) : "0";
        return String.format("metrics/%s/partition/%s/%s", typeStr, idStr, windowStr);
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
