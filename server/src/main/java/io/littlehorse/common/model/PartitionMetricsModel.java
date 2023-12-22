package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateWfMetricsModel;
import io.littlehorse.common.proto.MetricsByTenant;
import io.littlehorse.common.proto.PartitionMetrics;
import io.littlehorse.common.proto.StatusChanges;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.Getter;

@Getter
public class PartitionMetricsModel extends Storeable<PartitionMetrics> {

    private final Map<WfMetricId, StatusChangesModel> wfMetrics = new HashMap<>();
    private final Map<TaskMetricId, StatusChangesModel> taskMetrics = new HashMap<>();

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PartitionMetrics p = (PartitionMetrics) proto;
        for (MetricsByTenant metricsByTenant : p.getMetricsByTenantList()) {
            String tenantId = metricsByTenant.getTenantId();
            for (Map.Entry<String, StatusChanges> wfMetricEntry :
                    metricsByTenant.getLhStatusChangesMap().entrySet()) {
                WfSpecIdModel wfSpecId =
                        (WfSpecIdModel) ObjectIdModel.fromString(wfMetricEntry.getKey(), WfSpecIdModel.class);
                StatusChangesModel statusChanges =
                        LHSerializable.fromProto(wfMetricEntry.getValue(), StatusChangesModel.class, context);
                wfMetrics.put(new WfMetricId(wfSpecId, tenantId), statusChanges);
            }
            for (Map.Entry<String, StatusChanges> taskMetricEntry :
                    metricsByTenant.getTaskStatusChangesMap().entrySet()) {
                TaskDefIdModel taskDefId =
                        (TaskDefIdModel) ObjectIdModel.fromString(taskMetricEntry.getKey(), TaskDefIdModel.class);
                StatusChangesModel statusChanges =
                        LHSerializable.fromProto(taskMetricEntry.getValue(), StatusChangesModel.class, context);
                taskMetrics.put(new TaskMetricId(taskDefId, tenantId), statusChanges);
            }
        }
    }

    @Override
    public PartitionMetrics.Builder toProto() {
        PartitionMetrics.Builder out = PartitionMetrics.newBuilder();
        Map<String, MetricsByTenant.Builder> metricsByTenant = new HashMap<>();
        for (Map.Entry<WfMetricId, StatusChangesModel> wfMetric : wfMetrics.entrySet()) {
            WfMetricId metricId = wfMetric.getKey();
            StatusChangesModel changes = wfMetric.getValue();
            MetricsByTenant.Builder metricsByTenantProto =
                    metricsByTenant.getOrDefault(metricId.tenantId(), MetricsByTenant.newBuilder());
            metricsByTenantProto.setTenantId(metricId.tenantId());
            metricsByTenantProto.putLhStatusChanges(
                    metricId.wfSpecId().toString(), changes.toProto().build());
            metricsByTenant.putIfAbsent(metricId.tenantId(), metricsByTenantProto);
        }
        for (Map.Entry<TaskMetricId, StatusChangesModel> metrics : taskMetrics.entrySet()) {
            TaskMetricId taskMetric = metrics.getKey();
            StatusChangesModel changes = metrics.getValue();
            MetricsByTenant.Builder metricsByTenantProto =
                    metricsByTenant.getOrDefault(taskMetric.tenantId(), MetricsByTenant.newBuilder());
            metricsByTenantProto.setTenantId(taskMetric.tenantId());
            metricsByTenantProto.putTaskStatusChanges(
                    taskMetric.taskDefId().toString(), changes.toProto().build());
            metricsByTenant.putIfAbsent(taskMetric.tenantId(), metricsByTenantProto);
        }
        List<MetricsByTenant> metrics = metricsByTenant.values().stream()
                .map(MetricsByTenant.Builder::build)
                .toList();
        out.addAllMetricsByTenant(metrics);
        return out;
    }

    public void addMetric(WfSpecIdModel wfSpecId, String tenantId, LHStatusChangedModel lhStatus, Date time) {
        WfMetricId metricId = new WfMetricId(wfSpecId, tenantId);
        StatusChangesModel statusChanges = wfMetrics.getOrDefault(metricId, new StatusChangesModel());
        statusChanges.statusChanges.add(new StatusChangedModel(time, lhStatus));
        wfMetrics.putIfAbsent(metricId, statusChanges);
    }

    public void addMetric(TaskDefIdModel taskDefId, String tenantId, TaskStatusChangedModel taskStatus, Date time) {
        TaskMetricId metricId = new TaskMetricId(taskDefId, tenantId);
        StatusChangesModel statusChanges = taskMetrics.getOrDefault(metricId, new StatusChangesModel());
        statusChanges.statusChanges.add(new StatusChangedModel(time, taskStatus));
        taskMetrics.putIfAbsent(metricId, statusChanges);
    }

    @Override
    public Class<PartitionMetrics> getProtoBaseClass() {
        return PartitionMetrics.class;
    }

    @Override
    public String getStoreKey() {
        return LHConstants.PARTITION_METRICS_KEY;
    }

    @Override
    public StoreableType getType() {
        return StoreableType.PARTITION_METRICS;
    }

    public List<AggregateWfMetricsModel> buildWfRepartitionCommands() {
        Function<Map.Entry<WfMetricId, StatusChangesModel>, AggregateWfMetricsModel> transformMetricEntryToCommand =
                metricEntry -> new AggregateWfMetricsModel(
                        metricEntry.getKey().wfSpecId(),
                        metricEntry.getValue().statusChanges,
                        metricEntry.getKey().tenantId());
        return wfMetrics.entrySet().stream().map(transformMetricEntryToCommand).toList();
    }

    private record WfMetricId(WfSpecIdModel wfSpecId, String tenantId) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WfMetricId that)) return false;
            return Objects.equals(wfSpecId, that.wfSpecId) && Objects.equals(tenantId, that.tenantId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wfSpecId, tenantId);
        }
    }

    private record TaskMetricId(TaskDefIdModel taskDefId, String tenantId) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TaskMetricId that)) return false;
            return Objects.equals(taskDefId, that.taskDefId) && Objects.equals(tenantId, that.tenantId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(taskDefId, tenantId);
        }
    }
}
