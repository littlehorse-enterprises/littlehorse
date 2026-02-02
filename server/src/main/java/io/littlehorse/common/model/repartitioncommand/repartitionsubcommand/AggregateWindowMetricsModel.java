package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import org.apache.kafka.streams.processor.api.ProcessorContext;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateWindowMetrics;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetricWindowModel;
import lombok.Getter;

@Getter
public class AggregateWindowMetricsModel extends LHSerializable<AggregateWindowMetrics> implements RepartitionSubCommand {

    private WfSpecIdModel wfSpecId;
    private TenantIdModel tenantId;
    private MetricWindowModel metricWindow;

    public AggregateWindowMetricsModel() {}

    public AggregateWindowMetricsModel(
            WfSpecIdModel wfSpecId, 
            TenantIdModel tenantId,
            MetricWindowModel metricWindow) {
        this.wfSpecId = wfSpecId;
        this.tenantId = tenantId;
        this.metricWindow = metricWindow;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        AggregateWindowMetrics p = (AggregateWindowMetrics) proto;
        this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        this.metricWindow = LHSerializable.fromProto(p.getMetricWindow(), MetricWindowModel.class, context);
    }

    @Override
    public AggregateWindowMetrics.Builder toProto() {
        AggregateWindowMetrics.Builder out = AggregateWindowMetrics.newBuilder();
        out.setWfSpecId(wfSpecId.toProto());
        out.setTenantId(tenantId.toProto());
        out.setMetricWindow(metricWindow.toProto());
        return out;
    }

    @Override
    public Class<AggregateWindowMetrics> getProtoBaseClass() {
        return AggregateWindowMetrics.class;
    }

    @Override
    public String getPartitionKey() {
        return wfSpecId.toString();
    }

    @Override
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        String consolidatedKey = metricWindow.getStoreKey().replace("/partition/", "/");
        MetricWindowModel consolidatedMetric = repartitionedStore.get(consolidatedKey, MetricWindowModel.class);
        
        if (consolidatedMetric == null) {
            // First partition to report metrics for this window, use the incoming one
            consolidatedMetric = new MetricWindowModel(
                metricWindow.getWfSpecId(),
                metricWindow.getWindowStart()
            );
            consolidatedMetric.mergeFrom(metricWindow);
        } else {
            // Merge metrics from this partition with existing consolidated metrics
            consolidatedMetric.mergeFrom(metricWindow);
        }
        
        // Save the consolidated metric back to the store
        repartitionedStore.put(consolidatedMetric);
    }
}
