package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.TaskMetricUpdatePb;
import io.littlehorse.jlib.common.proto.TaskMetricUpdatePbOrBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public List<Integer> seenPartitions;
    public String taskDefName;

    public TaskMetricUpdate() {
        seenPartitions = new ArrayList<>();
    }

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

        for (Integer seen : seenPartitions) {
            out.addSeenPartitions(seen);
        }

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

        for (int seenPartition : p.getSeenPartitionsList()) {
            seenPartitions.add(seenPartition);
        }
    }

    public void merge(TaskMetricUpdate o) {
        if (!o.windowStart.equals(windowStart)) {
            throw new RuntimeException("Merging non-matched windows!");
        }
        if (!o.type.equals(type)) {
            throw new RuntimeException("Merging non-matched windows!");
        }

        numEntries += o.numEntries;
        if (o.scheduleToStartMax > scheduleToStartMax) {
            scheduleToStartMax = o.scheduleToStartMax;
        }
        scheduleToStartTotal += o.scheduleToStartTotal;

        if (o.startToCompleteMax > startToCompleteMax) {
            startToCompleteMax = o.startToCompleteMax;
        }
        startToCompleteTotal += o.startToCompleteTotal;

        totalCompleted += o.totalCompleted;
        totalErrored += o.totalErrored;
        totalStarted += o.totalStarted;

        for (Integer seenPartition : o.seenPartitions) {
            seenPartitions.add(seenPartition);
        }
    }

    public TaskDefMetrics toResponse() {
        TaskDefMetrics out = new TaskDefMetrics();
        out.scheduleToStartAvg = scheduleToStartTotal / numEntries;
        out.scheduleToStartMax = scheduleToStartMax;
        out.startToComplete

        return out;
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
