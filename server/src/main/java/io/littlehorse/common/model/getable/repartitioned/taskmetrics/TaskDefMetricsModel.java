package io.littlehorse.common.model.getable.repartitioned.taskmetrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.RepartitionedGetable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Getter
public class TaskDefMetricsModel extends RepartitionedGetable<TaskDefMetrics> {

    public Date windowStart;
    public MetricsWindowLength type;
    public TaskDefIdModel taskDefId;
    public long scheduleToStartMax;
    public long scheduleToStartAvg;
    public long startToCompleteMax;
    public long startToCompleteAvg;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;
    public long totalScheduled;

    public TaskDefMetricsModel() {}

    public TaskDefMetricsModel(Date windowStart, MetricsWindowLength type, TaskDefIdModel taskDefId) {
        this.windowStart = windowStart;
        this.type = type;
        this.taskDefId = taskDefId;
    }

    public Class<TaskDefMetrics> getProtoBaseClass() {
        return TaskDefMetrics.class;
    }

    public TaskDefMetrics.Builder toProto() {
        TaskDefMetrics.Builder out = TaskDefMetrics.newBuilder()
                .setWindowStart(LHLibUtil.fromDate(windowStart))
                .setType(type)
                .setTaskDefId(taskDefId.toProto())
                .setTotalCompleted(totalCompleted)
                .setTotalErrored(totalErrored)
                .setTotalStarted(totalStarted)
                .setScheduleToStartAvg(scheduleToStartAvg)
                .setScheduleToStartMax(scheduleToStartMax)
                .setStartToCompleteAvg(startToCompleteAvg)
                .setStartToCompleteMax(startToCompleteMax)
                .setTotalScheduled(totalScheduled);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskDefMetrics p = (TaskDefMetrics) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        totalCompleted = p.getTotalCompleted();
        totalErrored = p.getTotalErrored();
        totalStarted = p.getTotalStarted();
        scheduleToStartAvg = p.getScheduleToStartAvg();
        scheduleToStartMax = p.getScheduleToStartMax();
        startToCompleteAvg = p.getStartToCompleteAvg();
        startToCompleteMax = p.getStartToCompleteMax();
        totalScheduled = p.getTotalScheduled();
    }

    public Date getCreatedAt() {
        return windowStart;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public TaskDefMetricsIdModel getObjectId() {
        return new TaskDefMetricsIdModel(windowStart, type, taskDefId);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    public static String getObjectId(MetricsWindowLength windowType, Date time, String taskDefName) {
        return new TaskDefMetricsIdModel(time, windowType, new TaskDefIdModel(taskDefName)).toString();
    }
}
