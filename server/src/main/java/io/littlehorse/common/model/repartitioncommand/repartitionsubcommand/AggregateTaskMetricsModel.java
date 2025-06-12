package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateTaskMetrics;
import io.littlehorse.common.proto.TaskMetricUpdate;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class AggregateTaskMetricsModel extends LHSerializable<AggregateTaskMetrics> implements RepartitionSubCommand {

    private TaskDefIdModel taskDefId;
    private TenantIdModel tenantId;
    private Collection<TaskMetricUpdateModel> taskMetrics;

    public AggregateTaskMetricsModel() {
        this.taskMetrics = new ArrayList<>();
    }

    public AggregateTaskMetricsModel(
            TaskDefIdModel taskDefId, TenantIdModel tenantId, Collection<TaskMetricUpdateModel> taskMetrics) {
        this.taskDefId = taskDefId;
        this.tenantId = tenantId;
        this.taskMetrics = taskMetrics;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        AggregateTaskMetrics p = (AggregateTaskMetrics) proto;
        this.taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        this.taskMetrics.clear();
        for (TaskMetricUpdate tmu : p.getMetricUpdatesList()) {
            taskMetrics.add(LHSerializable.fromProto(tmu, TaskMetricUpdateModel.class, context));
        }
    }

    @Override
    public AggregateTaskMetrics.Builder toProto() {
        AggregateTaskMetrics.Builder builder = AggregateTaskMetrics.newBuilder();
        builder.setTenantId(tenantId.toProto());
        builder.setTaskDefId(taskDefId.toProto());
        List<TaskMetricUpdate> metricUpdates = taskMetrics.stream()
                .map(taskMetric -> taskMetric.toProto().build())
                .toList();
        builder.addAllMetricUpdates(metricUpdates);
        return builder;
    }

    @Override
    public Class<AggregateTaskMetrics> getProtoBaseClass() {
        return AggregateTaskMetrics.class;
    }

    @Override
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        for (TaskMetricUpdateModel metricUpdate : taskMetrics) {
            StoredGetable<TaskDefMetrics, TaskDefMetricsModel> storedMetrics = repartitionedStore.get(
                    TaskDefMetricsIdModel.getObjectId(
                                    metricUpdate.getWindowStart(),
                                    metricUpdate.getWindowType(),
                                    metricUpdate.getTaskDefId())
                            .getStoreableKey(),
                    StoredGetable.class);
            TaskDefMetricsModel metricToUpdate;
            if (storedMetrics == null) {
                metricToUpdate = new TaskDefMetricsModel(
                        metricUpdate.getWindowStart(), metricUpdate.getWindowType(), metricUpdate.getTaskDefId());
            } else {
                metricToUpdate = storedMetrics.getStoredObject();
            }
            mergeMetrics(metricToUpdate, metricUpdate);
            repartitionedStore.put(new StoredGetable<>(metricToUpdate));
        }
    }

    private void mergeMetrics(TaskDefMetricsModel taskDefMetric, TaskMetricUpdateModel metricUpdate) {
        taskDefMetric.totalCompleted += metricUpdate.totalCompleted;
        taskDefMetric.totalErrored += metricUpdate.totalErrored;
        taskDefMetric.totalStarted += metricUpdate.totalStarted;
        taskDefMetric.totalScheduled += metricUpdate.totalScheduled;
        taskDefMetric.startToCompleteMax = Math.max(metricUpdate.startToCompleteMax, taskDefMetric.startToCompleteMax);
        BigDecimal calculatedAvg = BigDecimal.valueOf(metricUpdate.startToCompleteTotal)
                .divide(BigDecimal.valueOf(Math.max(taskDefMetric.totalCompleted, 1L)), 4, RoundingMode.HALF_UP);
        taskDefMetric.startToCompleteAvg = calculatedAvg.longValue();
    }

    @Override
    public String getPartitionKey() {
        return taskDefId.toString();
    }
}
