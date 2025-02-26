package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.metrics.MetricModel;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateMetrics;
import io.littlehorse.common.proto.RepartitionWindowedMetric;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Slf4j
public class AggregateMetricsModel extends LHSerializable<AggregateMetrics> implements RepartitionSubCommand {

    private TenantIdModel tenantId;
    private MetricSpecIdModel metricId;
    private List<RepartitionWindowedMetricModel> windowedMetrics = new ArrayList<>();
    private Integer partitionId;

    public AggregateMetricsModel() {}

    public AggregateMetricsModel(
            TenantIdModel tenantId,
            MetricSpecIdModel metricId,
            List<RepartitionWindowedMetricModel> windowedMetrics,
            Integer partitionId) {
        this.windowedMetrics = windowedMetrics;
        this.tenantId = tenantId;
        this.metricId = metricId;
        this.partitionId = partitionId;
    }

    @Override
    public AggregateMetrics.Builder toProto() {
        List<RepartitionWindowedMetric> windowedMetricsPb = windowedMetrics.stream()
                .map(RepartitionWindowedMetricModel::toProto)
                .map(RepartitionWindowedMetric.Builder::build)
                .toList();
        return AggregateMetrics.newBuilder()
                .setMetricSpecId(metricId.toProto())
                .addAllWindowedMetrics(windowedMetricsPb)
                .setPartitionId(partitionId)
                .setTenantId(tenantId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        AggregateMetrics p = (AggregateMetrics) proto;
        this.windowedMetrics = p.getWindowedMetricsList().stream()
                .map(pb -> LHSerializable.fromProto(pb, RepartitionWindowedMetricModel.class, context))
                .toList();
        this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        this.metricId = LHSerializable.fromProto(p.getMetricSpecId(), MetricSpecIdModel.class, context);
        this.partitionId = p.getPartitionId();
    }

    public List<RepartitionWindowedMetricModel> getWindowedMetrics() {
        return Collections.unmodifiableList(windowedMetrics);
    }

    public void addWindowedMetric(List<RepartitionWindowedMetricModel> windowedMetrics) {
        this.windowedMetrics.addAll(windowedMetrics);
    }

    @Override
    public Class<AggregateMetrics> getProtoBaseClass() {
        return AggregateMetrics.class;
    }

    @Override
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        try {
            for (RepartitionWindowedMetricModel windowedMetric : windowedMetrics) {
                StoredGetable<Metric, MetricModel> storedGetable =
                        (StoredGetable<Metric, MetricModel>) repartitionedStore.get(
                                new MetricIdModel(
                                                metricId,
                                                windowedMetric.getWindowStart(),
                                                windowedMetric.getWindowLength())
                                        .getStoreableKey(),
                                StoredGetable.class);
                if (storedGetable == null) {
                    log.info("creating new metric on partition {}, partition KEY {}", ctx.taskId(), getPartitionKey());
                    storedGetable = new StoredGetable<>(new MetricModel(new MetricIdModel(
                            metricId, windowedMetric.getWindowStart(), windowedMetric.getWindowLength())));
                }
                MetricModel metricRun = storedGetable.getStoredObject();
                metricRun.mergePartitionMetric(windowedMetric, partitionId);
                repartitionedStore.put(storedGetable);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getPartitionKey() {
        return LHUtil.getCompositeId(tenantId.toString(), metricId.toString());
    }
}
