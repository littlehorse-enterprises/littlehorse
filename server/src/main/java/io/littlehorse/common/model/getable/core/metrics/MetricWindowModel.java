package io.littlehorse.common.model.getable.core.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MetricWindowModel extends CoreGetable<MetricWindow> {

    private MetricWindowIdModel id;
    private Map<String, CountAndTimingModel> metrics;

    public MetricWindowModel(MetricWindowIdModel id, Map<String, CountAndTimingModel> metrics) {
        this.id = id;
        this.metrics = metrics;
    }

    public void mergeFrom(Map<String, CountAndTimingModel> otherMetrics) {
        for (Entry<String, CountAndTimingModel> entry : otherMetrics.entrySet()) {
            String key = entry.getKey();
            CountAndTimingModel incoming = entry.getValue();
            CountAndTimingModel existing = metrics.get(key);
            if (existing == null) {
                metrics.put(key, incoming);
            } else {
                existing.mergeFrom(incoming);
            }
        }
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

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
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
}
