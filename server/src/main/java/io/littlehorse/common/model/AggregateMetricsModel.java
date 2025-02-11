package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateMetrics;
import io.littlehorse.common.proto.RepartitionWindowedMetric;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class AggregateMetricsModel extends LHSerializable<AggregateMetrics> implements RepartitionSubCommand {

    @Getter
    private List<RepartitionWindowedMetricModel> windowedMetrics = new ArrayList<>();

    public AggregateMetricsModel() {}

    public AggregateMetricsModel(List<RepartitionWindowedMetricModel> windowedMetrics) {
        this.windowedMetrics = windowedMetrics;
    }

    @Override
    public AggregateMetrics.Builder toProto() {
        List<RepartitionWindowedMetric> windowedMetricsPb = windowedMetrics.stream()
                .map(RepartitionWindowedMetricModel::toProto)
                .map(RepartitionWindowedMetric.Builder::build)
                .toList();
        return AggregateMetrics.newBuilder().addAllWindowedMetrics(windowedMetricsPb);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        AggregateMetrics p = (AggregateMetrics) proto;
        this.windowedMetrics = p.getWindowedMetricsList().stream()
                .map(pb -> LHSerializable.fromProto(pb, RepartitionWindowedMetricModel.class, context))
                .toList();
    }

    @Override
    public Class<AggregateMetrics> getProtoBaseClass() {
        return AggregateMetrics.class;
    }

    @Override
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx) {}

    @Override
    public String getPartitionKey() {
        return "";
    }
}
