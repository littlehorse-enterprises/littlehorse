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
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Getter
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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
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
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        for (WfMetricUpdateModel metricUpdate : metricUpdates) {

            // TODO: We should NOT do this. The RepartitionContext should do this for us. We also shouldn't
            // be passing in a TenantScopedStore; rather, we should pass in the context.
            StoredGetable<WfSpecMetrics, WfSpecMetricsModel> storedMetrics = repartitionedStore.get(
                    WfSpecMetricsIdModel.getObjectId(
                                    metricUpdate.getWindowStart(),
                                    metricUpdate.getWindowType(),
                                    metricUpdate.getWfSpecId())
                            .getStoreableKey(),
                    StoredGetable.class);
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
        wfSpecMetric.startToCompleteMax = Math.max(metricUpdate.startToCompleteMax, wfSpecMetric.startToCompleteMax);
        BigDecimal calculatedAvg = BigDecimal.valueOf(metricUpdate.startToCompleteTotal)
                .divide(BigDecimal.valueOf(Math.max(wfSpecMetric.totalCompleted, 1L)), 4, RoundingMode.HALF_UP);
        wfSpecMetric.startToCompleteAvg = calculatedAvg.longValue();
    }
}
