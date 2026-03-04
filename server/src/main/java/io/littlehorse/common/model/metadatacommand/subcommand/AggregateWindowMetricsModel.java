package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.proto.AggregateWindowMetrics;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class AggregateWindowMetricsModel extends CoreSubCommand<AggregateWindowMetrics> {

    private PartitionMetricWindowModel metricWindow;

    public AggregateWindowMetricsModel() {}

    public AggregateWindowMetricsModel(PartitionMetricWindowModel metricWindow) {
        this.metricWindow = metricWindow;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        AggregateWindowMetrics p = (AggregateWindowMetrics) proto;
        this.metricWindow = LHSerializable.fromProto(p.getMetricWindow(), PartitionMetricWindowModel.class, context);
        // this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
    }

    @Override
    public AggregateWindowMetrics.Builder toProto() {
        AggregateWindowMetrics.Builder out = AggregateWindowMetrics.newBuilder();
        // out.setTenantId(tenantId.toProto());
        out.setMetricWindow(metricWindow.toProto());
        return out;
    }

    @Override
    public Class<AggregateWindowMetrics> getProtoBaseClass() {
        return AggregateWindowMetrics.class;
    }

    @Override
    public String getPartitionKey() {
        return this.metricWindow.getId().getMetricType().name() + "/"
                + this.metricWindow.getId().getWfSpecId();
    }

    @SuppressWarnings("unchecked")
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        MetricWindowIdModel id = metricWindow.getId();
        StoredGetable<MetricWindow, MetricWindowModel> storedMetric =
                executionContext.getCoreStore().get(id.getStoreableKey(), StoredGetable.class);
        MetricWindowModel consolidatedMetric;
        if (storedMetric == null) {
            consolidatedMetric = new MetricWindowModel(id, metricWindow.getMetrics());
        } else {
            consolidatedMetric = storedMetric.getStoredObject();
            consolidatedMetric.mergeFrom(metricWindow.getMetrics());
        }
        executionContext.getCoreStore().put(new StoredGetable<>(consolidatedMetric));
        return null;
    }
}
