package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.AggregateWindowMetrics;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.PartitionMetricWindowModel;
import lombok.Getter;

@Getter
public class AggregateWindowMetricsModel extends CoreSubCommand<AggregateWindowMetrics> {

    private WfSpecIdModel wfSpecId;
    private TenantIdModel tenantId;
    private PartitionMetricWindowModel metricWindow;

    public AggregateWindowMetricsModel() {}

    public AggregateWindowMetricsModel(
            WfSpecIdModel wfSpecId, TenantIdModel tenantId, PartitionMetricWindowModel metricWindow) {
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

    public String getPartitionKey() {
        return wfSpecId.toString() + "/" + metricWindow.getWindowStart().getTime();
    }

    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        MetricWindowIdModel id = new MetricWindowIdModel(wfSpecId, metricWindow.getWindowStart());

        StoredGetable<MetricWindow, MetricWindowModel> storedMetric =
                executionContext.getCoreStore().get(id.getStoreableKey(), StoredGetable.class);

        MetricWindowModel consolidatedMetric;
        if (storedMetric == null) {
            consolidatedMetric = new MetricWindowModel(id, metricWindow.getMetrics());
        } else {
            consolidatedMetric = storedMetric.getStoredObject();
            consolidatedMetric.mergeFrom(metricWindow);
        }
        executionContext.getCoreStore().put(new StoredGetable<>(consolidatedMetric));
        System.out.println("Consolidated metrics: " + consolidatedMetric);
        return null;
    }
}
