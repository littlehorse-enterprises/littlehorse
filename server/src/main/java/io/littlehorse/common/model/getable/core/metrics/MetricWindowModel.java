package io.littlehorse.common.model.getable.core.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.PartitionMetricWindowModel;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricWindowModel extends CoreGetable<MetricWindow> {

    private MetricWindowIdModel id;
    private Map<String, CountAndTimingModel> metrics;

    public MetricWindowModel() {
        this.metrics = new HashMap<>();
    }

    public MetricWindowModel(WfSpecIdModel wfSpecId, Date windowStart) {
        this.id = new MetricWindowIdModel(wfSpecId, windowStart);
        this.metrics = new HashMap<>();
    }

    @Override
    public Class<MetricWindow> getProtoBaseClass() {
        return MetricWindow.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MetricWindow p = (MetricWindow) proto;
        id = LHSerializable.fromProto(p.getId(), MetricWindowIdModel.class, context);
        metrics = new HashMap<>();
        for (Map.Entry<String, CountAndTiming> entry : p.getMetricsMap().entrySet()) {
            metrics.put(entry.getKey(), LHSerializable.fromProto(entry.getValue(), CountAndTimingModel.class, context));
        }
    }

    @Override
    public MetricWindow.Builder toProto() {
        MetricWindow.Builder out = MetricWindow.newBuilder().setId(id.toProto());
        
        for (Map.Entry<String, CountAndTimingModel> entry : metrics.entrySet()) {
            out.putMetrics(entry.getKey(), entry.getValue().toProto().build());
        }
        
        return out;
    }

    /**
     * Merge metrics from a PartitionMetricWindowModel into this consolidated MetricWindow
     */
    public void mergeFrom(PartitionMetricWindowModel partitionMetric) {
        for (Map.Entry<String, PartitionMetricWindowModel.CountAndTimingModel> entry : 
                partitionMetric.getMetrics().entrySet()) {
            
            String key = entry.getKey();
            PartitionMetricWindowModel.CountAndTimingModel incoming = entry.getValue();
            
            CountAndTimingModel existing = metrics.get(key);
            if (existing == null) {
                // First time seeing this status, create new entry
                existing = new CountAndTimingModel();
                existing.setCount(0);
                existing.setMinLatencyMs(Long.MAX_VALUE);
                existing.setMaxLatencyMs(0L);
                existing.setTotalLatencyMs(0L);
                metrics.put(key, existing);
            }
            
            // Aggregate the metrics
            existing.setCount(existing.getCount() + incoming.getCount());
            existing.setMinLatencyMs(Math.min(existing.getMinLatencyMs(), incoming.getMinLatencyMs()));
            existing.setMaxLatencyMs(Math.max(existing.getMaxLatencyMs(), incoming.getMaxLatencyMs()));
            existing.setTotalLatencyMs(existing.getTotalLatencyMs() + incoming.getTotalLatencyMs());
        }
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        // No need to search metric windows by index
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public Date getCreatedAt() {
        return id.getWindowStart();
    }

    @Override
    public MetricWindowIdModel getObjectId() {
        return id;
    }

    @Getter
    @Setter
    public static class CountAndTimingModel extends LHSerializable<CountAndTiming> {
        private int count;
        private long minLatencyMs;
        private long maxLatencyMs;
        private long totalLatencyMs;

        @Override
        public Class<CountAndTiming> getProtoBaseClass() {
            return CountAndTiming.class;
        }

        @Override
        public void initFrom(Message proto, ExecutionContext context) {
            CountAndTiming p = (CountAndTiming) proto;
            count = p.getCount();
            minLatencyMs = p.getMinLatencyMs();
            maxLatencyMs = p.getMaxLatencyMs();
            totalLatencyMs = p.getTotalLatencyMs();
        }

        @Override
        public CountAndTiming.Builder toProto() {
            return CountAndTiming.newBuilder()
                    .setCount(count)
                    .setMinLatencyMs(minLatencyMs)
                    .setMaxLatencyMs(maxLatencyMs)
                    .setTotalLatencyMs(totalLatencyMs);
        }
    }
}
