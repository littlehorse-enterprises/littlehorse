package io.littlehorse.common.model.metrics;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.TaskDefMetricsPb;
import io.littlehorse.jlib.common.proto.TaskDefMetricsPbOrBuilder;
import io.littlehorse.jlib.common.proto.TaskDefMetricsQueryPb;
import java.util.Date;

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
            .setStartToCompleteMax(startToCompleteMax);

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskDefMetricsPbOrBuilder p = (TaskDefMetricsPbOrBuilder) proto;
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
    }

    public Date getCreatedAt() {
        return windowStart;
    }

    public String getPartitionKey() {
        return taskDefName;
    }

    public String getObjectId() {
        return getObjectId(type, windowStart, taskDefName);
    }

    public static String getObjectId(
        MetricsWindowLengthPb windowType,
        Date time,
        String taskDefName
    ) {
        return (
            windowType.toString() +
            "/" +
            LHUtil.toLhDbFormat(time) +
            "/" +
            taskDefName
        );
    }

    public static String getObjectId(TaskDefMetricsQueryPb request) {
        return getObjectId(
            request.getWindowType(),
            LHLibUtil.fromProtoTs(request.getWindowStart()),
            request.getTaskDefName()
        );
    }
}
