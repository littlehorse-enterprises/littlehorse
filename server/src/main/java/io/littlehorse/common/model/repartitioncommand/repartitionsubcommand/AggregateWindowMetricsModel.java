package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateWindowMetrics;
import io.littlehorse.sdk.common.exception.LHSerdeException;
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
        // out.setMetricWindow(metricWindow.toProto());
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
        // String consolidatedKey = metricWindow.getStoreKey().replace("/partition/", "/");
        // PartitionMetricWindowModel existingMetric = repartitionedStore.get(consolidatedKey, PartitionMetricWindowModel.class);

        // System.out.println(
        //         "AggregateWindowMetricsModel.process() - incoming metric key: " + metricWindow.getStoreKey());
        // System.out.println("AggregateWindowMetricsModel.process() - incoming isLocalPartition: "
        //         + metricWindow.isLocalPartition());
        // System.out.println("AggregateWindowMetricsModel.process() - consolidatedKey: " + consolidatedKey);

        // // Always create a new consolidated metric with isLocalPartition=false
        // // This ensures the key is generated correctly when saving
        // PartitionMetricWindowModel consolidatedMetric = new PartitionMetricWindowModel(
        //         metricWindow.getWfSpecId(),
        //         metricWindow.getWindowStart(),
        //         false // isLocalPartition=false for consolidated metrics
        //         );

        // // Merge existing data if any
        // if (existingMetric != null) {
        //     consolidatedMetric.mergeFrom(existingMetric);
        // }

        // // Merge the incoming partition data
        // consolidatedMetric.mergeFrom(metricWindow);

        // System.out.println(
        //         "AggregateWindowMetricsModel.process() - consolidated metric key: " + consolidatedMetric.getStoreKey());
        // System.out.println("AggregateWindowMetricsModel.process() - consolidated getFullStoreKey: "
        //         + consolidatedMetric.getFullStoreKey());
        // System.out.println("AggregateWindowMetricsModel.process() - consolidated isLocalPartition: "
        //         + consolidatedMetric.isLocalPartition());
        // System.out.println("AggregateWindowMetricsModel.process() - saving with "
        //         + consolidatedMetric.getMetrics().size() + " metric entries");
        // // Save the consolidated metric back to the store
        // repartitionedStore.put(consolidatedMetric);
        // System.out.println("AggregateWindowMetricsModel.process() - SAVED - now key would be: "
        //         + consolidatedMetric.getStoreKey());
    }
}
