package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.metrics.TaskDefMetricsModel;
import io.littlehorse.common.model.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.proto.TaskMetricUpdatePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import java.util.Date;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class TaskMetricUpdate
    extends Storeable<TaskMetricUpdatePb>
    implements RepartitionSubCommand {

    public Date windowStart;
    public MetricsWindowLength type;
    public long numEntries;
    public long scheduleToStartMax;
    public long scheduleToStartTotal;
    public long startToCompleteMax;
    public long startToCompleteTotal;
    public long totalCompleted;
    public long totalErrored;
    public long totalStarted;
    public long totalScheduled;

    public String taskDefName;

    public TaskMetricUpdate() {}

    public TaskMetricUpdate(
        Date windowStart,
        MetricsWindowLength type,
        String taskDefName
    ) {
        this.windowStart = windowStart;
        this.type = type;
        this.taskDefName = taskDefName;
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
            .setNumEntries(numEntries)
            .setTotalScheduled(totalScheduled);

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
        totalScheduled = p.getTotalScheduled();
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
        totalScheduled += o.totalScheduled;
    }

    public TaskDefMetricsModel toResponse() {
        TaskDefMetricsModel out = new TaskDefMetricsModel();
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
        out.totalScheduled = totalScheduled;
        out.type = type;

        return out;
    }

    public String getClusterLevelWindow() {
        return new TaskDefMetricsIdModel(
            windowStart,
            type,
            LHConstants.CLUSTER_LEVEL_METRIC
        )
            .getStoreKey();
    }

    public void process(LHStoreWrapper store, ProcessorContext<Void, Void> ctx) {
        // Update TaskDef-Level Metrics
        TaskMetricUpdate previousUpdate = store.get(getStoreKey(), getClass());
        if (previousUpdate != null) {
            merge(previousUpdate);
        }
        store.put(this);
        store.put(toResponse());
    }

    public String getPartitionKey() {
        return taskDefName;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(
            LHUtil.toLhDbFormat(windowStart),
            type.toString(),
            taskDefName
        );
    }

    public static String getStoreKey(
        MetricsWindowLength type,
        Date windowStart,
        String taskDefName
    ) {
        return LHUtil.getCompositeId(
            LHUtil.toLhDbFormat(windowStart),
            type.toString(),
            taskDefName
        );
    }

    public Date getCreatedAt() {
        return windowStart;
    }
}
/*

partition 1:
// executes 2 tasks "greett"

partition 2:
// executes 1 task "greet"
// executes 1 task "foo"


Partition 1 sends:
{
    "windowStart": 5:00pm
    "windowLength": MINUTES_5
    taskDefName: greet
    numTasks: 2
}

Partition 2 sends:
{
    "windowStart": 5:00pm
    "windowLength": MINUTES_5
    taskDefName: greet
    numTasks: 1
}
{
    "windowStart": 5:00pm
    "windowLength": MINUTES_5
    taskDefName: foo
    numTasks: 1
}



repartition processor:
{
    "windowStart": 5:00pm
    "windowLength": MINUTES_5
    taskDefName: "ALL_TASKS"
    numTasks: 4
}

{
    "windowStart": 5:00pm
    "windowLength": MINUTES_5
    taskDefName: greet
    numTasks: 3
}
{
    "windowStart": 5:00pm
    "windowLength": MINUTES_5
    taskDefName: foo
    numTasks: 1
}


 */
