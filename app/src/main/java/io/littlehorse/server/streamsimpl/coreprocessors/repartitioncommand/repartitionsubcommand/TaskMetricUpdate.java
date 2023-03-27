package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.common.model.objectId.TaskDefMetricsId;
import io.littlehorse.common.proto.TaskMetricUpdatePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class TaskMetricUpdate
    extends Storeable<TaskMetricUpdatePb>
    implements RepartitionSubCommand {

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
            .setStartToCompleteMax(startToCompleteMax)
            .setNumEntries(numEntries);

        for (Integer seen : seenPartitions) {
            out.addSeenPartitions(seen);
        }

        return out;
    }

    public void initFrom(Message proto) {
        TaskMetricUpdatePb p = (TaskMetricUpdatePb) proto;
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
        numEntries = p.getNumEntries();

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
        out.scheduleToStartAvg =
            totalStarted > 0 ? scheduleToStartTotal / totalStarted : 0;
        out.scheduleToStartMax = scheduleToStartMax;
        out.startToCompleteAvg =
            totalCompleted > 0 ? startToCompleteTotal / totalCompleted : 0;
        out.startToCompleteMax = startToCompleteMax;
        out.taskDefName = taskDefName;
        out.totalCompleted = totalCompleted;
        out.totalStarted = totalStarted;
        out.totalErrored = totalErrored;
        out.windowStart = windowStart;
        out.type = type;

        return out;
    }

    public static String getPrefix(MetricsWindowLengthPb type, Date windowStart) {
        return type + "/" + LHUtil.toLhDbFormat(windowStart) + "/";
    }

    public void process(LHStoreWrapper store, ProcessorContext<Void, Void> ctx) {
        TaskMetricUpdate previous = store.get(getStoreKey(), getClass());
        if (previous != null) {
            merge(previous);
        }
        store.put(this);
        store.put(toResponse());
    }

    public String getPartitionKey() {
        return taskDefName;
    }

    public String getStoreKey() {
        return new TaskDefMetricsId(windowStart, type, taskDefName).getStoreKey();
    }

    public static String getStoreKey(
        MetricsWindowLengthPb type,
        Date windowStart,
        String taskDefName
    ) {
        return new TaskDefMetricsId(windowStart, type, taskDefName).getStoreKey();
    }

    public Date getCreatedAt() {
        return windowStart;
    }
}
