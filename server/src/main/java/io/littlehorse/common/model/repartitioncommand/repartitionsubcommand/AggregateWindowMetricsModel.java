package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateWindowMetrics;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.PartitionMetricWindowModel;
import lombok.Getter;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Getter
public class AggregateWindowMetricsModel extends LHSerializable<AggregateWindowMetrics>
        implements RepartitionSubCommand {

    private WfSpecIdModel wfSpecId;
    private TenantIdModel tenantId;
    private PartitionMetricWindowModel metricWindow;

    public AggregateWindowMetricsModel() {}

    public AggregateWindowMetricsModel(WfSpecIdModel wfSpecId, TenantIdModel tenantId, PartitionMetricWindowModel metricWindow) {
        this.wfSpecId = wfSpecId;
        this.tenantId = tenantId;
        this.metricWindow = metricWindow;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        AggregateWindowMetrics p = (AggregateWindowMetrics) proto;
        this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        this.metricWindow = LHSerializable.fromProto(p.getMetricWindow(), PartitionMetricWindowModel.class, context);
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
        MetricWindowIdModel id = new MetricWindowIdModel(wfSpecId, metricWindow.getWindowStart());
        
        StoredGetable<MetricWindow, MetricWindowModel> storedMetric = 
                repartitionedStore.get(id.getStoreableKey(), StoredGetable.class);
        
        MetricWindowModel consolidatedMetric;
        if (storedMetric == null) {
            // Create new consolidated metric window
            consolidatedMetric = new MetricWindowModel(wfSpecId, metricWindow.getWindowStart());
        } else {
            consolidatedMetric = storedMetric.getStoredObject();
        }
        
        // Merge the partition metrics into the consolidated window
        consolidatedMetric.mergeFrom(metricWindow);
        
        // Save the consolidated metric back to the store
        repartitionedStore.put(new StoredGetable<>(consolidatedMetric));
    }
}
