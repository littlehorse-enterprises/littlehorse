package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.TaskMetricUpdatePb;
import io.littlehorse.jlib.common.proto.TaskMetricUpdatePbOrBuilder;
import java.util.Date;

public class TaskMetricUpdate extends Storeable<TaskMetricUpdatePb> {

    public Date windowStart;
    public MetricsWindowLengthPb type;
    public long numEntries;
    public long scheduleToStartMax;
    public long scheduleToStartTotal;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;

    public int partition;
    public String taskDefName;

    public Class<TaskMetricUpdatePb> getProtoBaseClass() {
        return TaskMetricUpdatePb.class;
    }

    public TaskMetricUpdatePb.Builder toProto() {
        TaskMetricUpdatePb.Builder out = TaskMetricUpdatePb
            .newBuilder()
            .setWindowStart(LHLibUtil.fromDate(windowStart))
            .setType(type)
            .setTaskDefName(taskDefName)
            .setTotalCompleted(totalCompleted)
            .setTotalErrored(totalErrored)
            .setTotalStarted(totalStarted)
            .setScheduleToStartTotal(scheduleToStartTotal)
            .setScheduleToStartMax(scheduleToStartMax)
            .setStartToCompleteTotal(startToCompleteTotal)
            .setStartToCompleteMax(startToCompleteMax);

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskMetricUpdatePbOrBuilder p = (TaskMetricUpdatePbOrBuilder) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        taskDefName = p.getTaskDefName();
        totalCompleted = p.getTotalCompleted();
        totalErrored = p.getTotalErrored();
        totalStarted = p.getTotalStarted();
        scheduleToStartTotal = p.getScheduleToStartTotal();
        scheduleToStartMax = p.getScheduleToStartMax();
        startToCompleteTotal = p.getStartToCompleteTotal();
        startToCompleteMax = p.getStartToCompleteMax();
    }

    public static String getObjectId(
        MetricsWindowLengthPb type,
        Date windowStart,
        String taskDefName
    ) {
        return type + "/" + LHUtil.toLhDbFormat(windowStart) + "/" + taskDefName;
    }

    public String getObjectId() {
        return getObjectId(type, windowStart, taskDefName);
    }
}
