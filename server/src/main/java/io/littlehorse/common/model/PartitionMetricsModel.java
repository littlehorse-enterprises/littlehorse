package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateTaskMetricsModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateWfMetricsModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdateModel;
import io.littlehorse.common.proto.MetricsByTenant;
import io.littlehorse.common.proto.PartitionMetrics;
import io.littlehorse.common.proto.StatusChanges;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Collection;
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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PartitionMetrics p = (PartitionMetrics) proto;
        for (MetricsByTenant metricsByTenant : p.getMetricsByTenantList()) {
            TenantIdModel tenantId =
                    LHSerializable.fromProto(metricsByTenant.getTenantId(), TenantIdModel.class, context);
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
        Map<TenantIdModel, MetricsByTenant.Builder> metricsByTenant = new HashMap<>();
        for (Map.Entry<WfMetricId, StatusChangesModel> wfMetric : wfMetrics.entrySet()) {
            WfMetricId metricId = wfMetric.getKey();
            StatusChangesModel changes = wfMetric.getValue();
            MetricsByTenant.Builder metricsByTenantProto =
                    metricsByTenant.getOrDefault(metricId.tenantId(), MetricsByTenant.newBuilder());
            metricsByTenantProto.setTenantId(metricId.tenantId().toProto());
            metricsByTenantProto.putLhStatusChanges(
                    metricId.wfSpecId().toString(), changes.toProto().build());
            metricsByTenant.putIfAbsent(metricId.tenantId(), metricsByTenantProto);
        }
        for (Map.Entry<TaskMetricId, StatusChangesModel> metrics : taskMetrics.entrySet()) {
            TaskMetricId taskMetric = metrics.getKey();
            StatusChangesModel changes = metrics.getValue();
            MetricsByTenant.Builder metricsByTenantProto =
                    metricsByTenant.getOrDefault(taskMetric.tenantId(), MetricsByTenant.newBuilder());
            metricsByTenantProto.setTenantId(taskMetric.tenantId().toProto());
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

    public void addMetric(
            WfSpecIdModel wfSpecId,
            TenantIdModel tenantId,
            LHStatusChangedModel lhStatus,
            Date time,
            long firstEventToLastDelay) {
        WfMetricId clusterLevelId = new WfMetricId(new WfSpecIdModel(LHConstants.CLUSTER_LEVEL_METRIC, 0, 0), tenantId);
        StatusChangesModel clusterLevelChanges = wfMetrics.getOrDefault(clusterLevelId, new StatusChangesModel());
        WfMetricId metricId = new WfMetricId(wfSpecId, tenantId);
        StatusChangesModel statusChanges = wfMetrics.getOrDefault(metricId, new StatusChangesModel());
        StatusChangedModel statusChanged = new StatusChangedModel(time, lhStatus, firstEventToLastDelay);
        statusChanges.statusChanges.add(statusChanged);
        clusterLevelChanges.statusChanges.add(statusChanged);
        wfMetrics.putIfAbsent(metricId, statusChanges);
        wfMetrics.putIfAbsent(clusterLevelId, statusChanges);
    }

    public void addMetric(
            TaskDefIdModel taskDefId,
            TenantIdModel tenantId,
            TaskStatusChangedModel taskStatus,
            Date time,
            long firstEventToLastDelay) {
        TaskMetricId clusterId = new TaskMetricId(new TaskDefIdModel(LHConstants.CLUSTER_LEVEL_METRIC), tenantId);
        TaskMetricId metricId = new TaskMetricId(taskDefId, tenantId);
        StatusChangesModel statusChanges = taskMetrics.getOrDefault(metricId, new StatusChangesModel());
        StatusChangesModel globalChanges = taskMetrics.getOrDefault(clusterId, new StatusChangesModel());
        StatusChangedModel statusChanged = new StatusChangedModel(time, taskStatus, firstEventToLastDelay);
        statusChanges.statusChanges.add(statusChanged);
        globalChanges.statusChanges.add(statusChanged);
        taskMetrics.putIfAbsent(metricId, statusChanges);
        taskMetrics.putIfAbsent(clusterId, globalChanges);
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
                        calculateMetrics(metricEntry.getKey(), metricEntry.getValue()),
                        metricEntry.getKey().tenantId());
        return wfMetrics.entrySet().stream().map(transformMetricEntryToCommand).toList();
    }

    private Collection<WfMetricUpdateModel> calculateMetrics(WfMetricId metricId, StatusChangesModel changes) {
        Map<WfMetricUpdateModel, List<StatusChangedModel>> windowMap = new HashMap<>();
        for (StatusChangedModel statusChange : changes.statusChanges) {
            for (MetricsWindowLength validWindowLength : MetricsWindowLength.values()) {
                if (validWindowLength.equals(MetricsWindowLength.UNRECOGNIZED)) {
                    continue;
                }
                WfMetricUpdateModel metricUpdate = new WfMetricUpdateModel(
                        LHUtil.getWindowStart(statusChange.getTime(), validWindowLength),
                        validWindowLength,
                        metricId.wfSpecId());
                List<StatusChangedModel> changesByWindow = windowMap.getOrDefault(metricUpdate, new ArrayList<>());
                changesByWindow.add(statusChange);
                windowMap.putIfAbsent(metricUpdate, changesByWindow);
            }
        }
        windowMap.forEach((wfMetricUpdateModel, statusChangedModels) -> {
            for (StatusChangedModel statusChangedModel : statusChangedModels) {
                incrementMetric(wfMetricUpdateModel, statusChangedModel);
            }
        });
        return windowMap.keySet();
    }

    public List<AggregateTaskMetricsModel> buildTaskMetricRepartitionCommand() {
        return taskMetrics.entrySet().stream()
                .map(taskMetric -> new AggregateTaskMetricsModel(
                        taskMetric.getKey().taskDefId(),
                        taskMetric.getKey().tenantId(),
                        calculateMetrics(taskMetric.getKey(), taskMetric.getValue())))
                .toList();
    }

    private Collection<TaskMetricUpdateModel> calculateMetrics(TaskMetricId taskMetricId, StatusChangesModel changes) {
        Map<TaskMetricUpdateModel, List<StatusChangedModel>> windowMap = new HashMap<>();
        for (StatusChangedModel statusChange : changes.statusChanges) {
            for (MetricsWindowLength validWindowLength : MetricsWindowLength.values()) {
                if (validWindowLength.equals(MetricsWindowLength.UNRECOGNIZED)) {
                    continue;
                }
                TaskMetricUpdateModel metricUpdate = new TaskMetricUpdateModel(
                        LHUtil.getWindowStart(statusChange.getTime(), validWindowLength),
                        validWindowLength,
                        taskMetricId.taskDefId());
                List<StatusChangedModel> changesByWindow = windowMap.getOrDefault(metricUpdate, new ArrayList<>());
                changesByWindow.add(statusChange);
                windowMap.putIfAbsent(metricUpdate, changesByWindow);
            }
        }
        windowMap.forEach((taskMetric, statusChanges) -> {
            for (StatusChangedModel statusChange : statusChanges) {
                incrementMetric(taskMetric, statusChange);
            }
        });
        return windowMap.keySet();
    }

    private void incrementMetric(WfMetricUpdateModel metric, StatusChangedModel statusChanged) {
        if (statusChanged.getLhStatusChanged().isCompleted()) {
            metric.totalCompleted += 1;
        } else if (statusChanged.getLhStatusChanged().isStarted()) {
            metric.totalStarted += 1;
        } else if (statusChanged.getLhStatusChanged().isErrored()) {
            metric.totalErrored += 1;
        }
        metric.startToCompleteTotal += statusChanged.getFirstEventToLastDelay();
        metric.numEntries += 1;
        metric.startToCompleteMax = Math.max(statusChanged.getFirstEventToLastDelay(), metric.startToCompleteMax);
    }

    private void incrementMetric(TaskMetricUpdateModel metric, StatusChangedModel statusChanged) {
        if (statusChanged.getTaskStatusChanged().isCompleted()) {
            metric.totalCompleted += 1;
        } else if (statusChanged.getTaskStatusChanged().isStarted()) {
            metric.totalStarted += 1;
        } else if (statusChanged.getTaskStatusChanged().isErrored()) {
            metric.totalErrored += 1;
        } else if (statusChanged.getTaskStatusChanged().isScheduled()) {
            metric.totalScheduled += 1;
        }
        metric.startToCompleteTotal += statusChanged.getFirstEventToLastDelay();
        metric.numEntries += 1;
        metric.startToCompleteMax = Math.max(statusChanged.getFirstEventToLastDelay(), metric.startToCompleteMax);
    }

    private record WfMetricId(WfSpecIdModel wfSpecId, TenantIdModel tenantId) {
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

    private record TaskMetricId(TaskDefIdModel taskDefId, TenantIdModel tenantId) {
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
