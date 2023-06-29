package io.littlehorse.common.model.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.objectId.TaskDefMetricsId;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.TaskDefMetricsPb;
import io.littlehorse.jlib.common.proto.TaskDefMetricsQueryPb;
import io.littlehorse.server.streamsimpl.storeinternals.GETableIndex;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDefMetrics extends GETable<TaskDefMetricsPb> {

    public Date windowStart;
    public MetricsWindowLengthPb type;
    public String taskDefName;
    public long scheduleToStartMax;
    public long scheduleToStartAvg;
    public long startToCompleteMax;
    public long startToCompleteAvg;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;
    public long totalScheduled;

    public Class<TaskDefMetricsPb> getProtoBaseClass() {
        return TaskDefMetricsPb.class;
    }

    public TaskDefMetricsPb.Builder toProto() {
        TaskDefMetricsPb.Builder out = TaskDefMetricsPb
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
        TaskDefMetricsPb p = (TaskDefMetricsPb) proto;
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
    public List<GETableIndex> getIndexes() {
        return new ArrayList<>();
    }

    public TaskDefMetricsId getObjectId() {
        TaskDefMetricsId out = new TaskDefMetricsId();
        out.windowStart = windowStart;
        out.windowType = type;
        out.taskDefName = taskDefName;
        return out;
    }

    public static String getObjectId(
        MetricsWindowLengthPb windowType,
        Date time,
        String taskDefName
    ) {
        return new TaskDefMetricsId(time, windowType, taskDefName).getStoreKey();
    }

    public static String getObjectId(TaskDefMetricsQueryPb request) {
        return new TaskDefMetricsId(
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
