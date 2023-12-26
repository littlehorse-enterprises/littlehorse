package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.WfMetricUpdateModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateWfMetrics;
import io.littlehorse.common.proto.WfMetricUpdate;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Getter
@Slf4j
public class AggregateWfMetricsModel extends LHSerializable<AggregateWfMetrics> implements RepartitionSubCommand {

    private WfSpecIdModel wfSpecId;
    private TenantIdModel tenantId;
    private final Collection<WfMetricUpdateModel> metricUpdates;

    public AggregateWfMetricsModel() {
        this.metricUpdates = new ArrayList<>();
    }

    public AggregateWfMetricsModel(
            WfSpecIdModel wfSpecId, Collection<WfMetricUpdateModel> metricUpdates, TenantIdModel tenantId) {
        this.wfSpecId = wfSpecId;
        this.metricUpdates = metricUpdates;
        this.tenantId = tenantId;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        AggregateWfMetrics p = (AggregateWfMetrics) proto;
        this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        metricUpdates.clear();
        for (WfMetricUpdate metricUpdate : p.getMetricUpdatesList()) {
            metricUpdates.add(LHSerializable.fromProto(metricUpdate, WfMetricUpdateModel.class, context));
        }
    }

    @Override
    public AggregateWfMetrics.Builder toProto() {
        AggregateWfMetrics.Builder out = AggregateWfMetrics.newBuilder();
        out.setWfSpecId(wfSpecId.toProto());
        out.setTenantId(tenantId.toProto());
        List<WfMetricUpdate> metricUpdatesProto = metricUpdates.stream()
                .map(WfMetricUpdateModel::toProto)
                .map(WfMetricUpdate.Builder::build)
                .toList();
        out.addAllMetricUpdates(metricUpdatesProto);
        return out;
    }

    @Override
    public Class<AggregateWfMetrics> getProtoBaseClass() {
        return AggregateWfMetrics.class;
    }

    @Override
    public String getPartitionKey() {
        return wfSpecId.toString();
    }

    @Override
    public void process(ModelStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        for (WfMetricUpdateModel metricUpdate : metricUpdates) {
            StoredGetable<WfSpecMetrics, WfSpecMetricsModel> storedMetrics =
                    repartitionedStore.get(WfSpecMetricsIdModel.getObjectId(
                            metricUpdate.getWindowStart(), metricUpdate.getWindowType(), metricUpdate.getWfSpecId()));
            WfSpecMetricsModel metricToUpdate;
            if (storedMetrics == null) {
                metricToUpdate = new WfSpecMetricsModel(
                        metricUpdate.getWindowStart(), metricUpdate.getWindowType(), metricUpdate.getWfSpecId());
            } else {
                metricToUpdate = storedMetrics.getStoredObject();
            }
            mergeMetrics(metricToUpdate, metricUpdate);
            repartitionedStore.put(new StoredGetable<>(metricToUpdate));
        }
    }

    private void mergeMetrics(WfSpecMetricsModel wfSpecMetric, WfMetricUpdateModel metricUpdate) {
        wfSpecMetric.totalCompleted += metricUpdate.totalCompleted;
        wfSpecMetric.totalErrored += metricUpdate.totalErrored;
        wfSpecMetric.totalStarted += metricUpdate.totalStarted;
    }
}
