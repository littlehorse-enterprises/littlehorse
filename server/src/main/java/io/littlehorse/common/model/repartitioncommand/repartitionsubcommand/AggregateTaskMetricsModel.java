package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateTaskMetrics;
import io.littlehorse.common.proto.TaskMetricUpdate;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        AggregateTaskMetrics p = (AggregateTaskMetrics) proto;
        this.taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        this.taskMetrics.clear();
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
    public void process(ModelStore repartitionedStore, ProcessorContext<Void, Void> ctx) {}

    @Override
    public String getPartitionKey() {
        return taskDefId.toString();
    }
}