package io.littlehorse.common.model.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TaskDefMetricsModel extends Getable<TaskDefMetrics> {

    public Date windowStart;
    public MetricsWindowLength type;
    public String taskDefName;
    public long scheduleToStartMax;
    public long scheduleToStartAvg;
    public long startToCompleteMax;
    public long startToCompleteAvg;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;
    public long totalScheduled;

    public Class<TaskDefMetrics> getProtoBaseClass() {
        return TaskDefMetrics.class;
    }

    public TaskDefMetrics.Builder toProto() {
        TaskDefMetrics.Builder out = TaskDefMetrics
            .newBuilder()
            .setWindowStart(LHLibUtil.fromDate(windowStart))
            .setType(type)
            .setTaskDefName(taskDefName)
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

    public void initFrom(Message proto) {
        TaskDefMetrics p = (TaskDefMetrics) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        taskDefName = p.getTaskDefName();
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
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public TaskDefMetricsIdModel getObjectId() {
        TaskDefMetricsIdModel out = new TaskDefMetricsIdModel();
        out.windowStart = windowStart;
        out.windowType = type;
        out.taskDefName = taskDefName;
        return out;
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        return List.of();
    }

    public static String getObjectId(
        MetricsWindowLength windowType,
        Date time,
        String taskDefName
    ) {
        return new TaskDefMetricsIdModel(time, windowType, taskDefName).getStoreKey();
    }

    public static String getObjectId(TaskDefMetricsQueryPb request) {
        return new TaskDefMetricsIdModel(
            LHUtil.getWindowStart(
                LHLibUtil.fromProtoTs(request.getWindowStart()),
                request.getWindowType()
            ),
            request.getWindowType(),
            request.getTaskDefName()
        )
            .getStoreKey();
    }
}
