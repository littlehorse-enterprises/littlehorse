package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.TaskMetricUpdate;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Getter
public class TaskMetricUpdateModel extends Storeable<TaskMetricUpdate> implements RepartitionSubCommand {

    private TaskDefIdModel taskDefId;
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

    @Override
    public StoreableType getType() {
        return StoreableType.TASK_METRIC_UPDATE;
    }

    public TaskMetricUpdateModel() {}

    public TaskMetricUpdateModel(Date windowStart, MetricsWindowLength type, TaskDefIdModel taskDefId) {
        this.windowStart = windowStart;
        this.type = type;
        this.taskDefId = taskDefId;
    }

    public Class<TaskMetricUpdate> getProtoBaseClass() {
        return TaskMetricUpdate.class;
    }

    public TaskMetricUpdate.Builder toProto() {
        TaskMetricUpdate.Builder out = TaskMetricUpdate.newBuilder()
                .setWindowStart(LHLibUtil.fromDate(windowStart))
                .setType(type)
                .setTaskDefId(taskDefId.toProto())
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

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskMetricUpdate p = (TaskMetricUpdate) proto;
        windowStart = LHLibUtil.fromProtoTs(p.getWindowStart());
        type = p.getType();
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
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

    public void merge(TaskMetricUpdateModel o) {
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
        out.scheduleToStartAvg = totalStarted > 0 ? scheduleToStartTotal / totalStarted : 0;
        out.scheduleToStartMax = scheduleToStartMax;
        out.startToCompleteAvg = totalCompleted > 0 ? startToCompleteTotal / totalCompleted : 0;
        out.startToCompleteMax = startToCompleteMax;
        out.taskDefId = taskDefId;
        out.totalCompleted = totalCompleted;
        out.totalStarted = totalStarted;
        out.totalErrored = totalErrored;
        out.windowStart = windowStart;
        out.totalScheduled = totalScheduled;
        out.type = type;

        return out;
    }

    public String getClusterLevelWindow() {
        return new TaskDefMetricsIdModel(windowStart, type, new TaskDefIdModel(LHConstants.CLUSTER_LEVEL_METRIC))
                .getStoreableKey();
    }

    @Override
    public void process(ModelStore store, ProcessorContext<Void, Void> ctx) {
        throw new NotImplementedException("Need to re-implement metrics");
        /*
         * // Update TaskDef-Level Metrics
         * TaskMetricUpdate previousUpdate = store.get(getStoreKey(), getClass());
         * if (previousUpdate != null) {
         * merge(previousUpdate);
         * }
         * store.put(this);
         * store.put(toResponse());
         */
    }

    @Override
    public String getPartitionKey() {
        return taskDefId.getName();
    }

    @Override
    public String getStoreKey() {
        return LHUtil.getCompositeId(LHUtil.toLhDbFormat(windowStart), type.toString(), taskDefId.toString());
    }

    public static String getStoreKey(MetricsWindowLength type, Date windowStart, String taskDefName) {
        return LHUtil.getCompositeId(LHUtil.toLhDbFormat(windowStart), type.toString(), taskDefName);
    }

    public Date getCreatedAt() {
        return windowStart;
    }
}
/*
 *
 * partition 1:
 * // executes 2 tasks "greett"
 *
 * partition 2:
 * // executes 1 task "greet"
 * // executes 1 task "foo"
 *
 *
 * Partition 1 sends:
 * {
 * "windowStart": 5:00pm
 * "windowLength": MINUTES_5
 * taskDefName: greet
 * numTasks: 2
 * }
 *
 * Partition 2 sends:
 * {
 * "windowStart": 5:00pm
 * "windowLength": MINUTES_5
 * taskDefName: greet
 * numTasks: 1
 * }
 * {
 * "windowStart": 5:00pm
 * "windowLength": MINUTES_5
 * taskDefName: foo
 * numTasks: 1
 * }
 *
 *
 *
 * repartition processor:
 * {
 * "windowStart": 5:00pm
 * "windowLength": MINUTES_5
 * taskDefName: "ALL_TASKS"
 * numTasks: 4
 * }
 *
 * {
 * "windowStart": 5:00pm
 * "windowLength": MINUTES_5
 * taskDefName: greet
 * numTasks: 3
 * }
 * {
 * "windowStart": 5:00pm
 * "windowLength": MINUTES_5
 * taskDefName: foo
 * numTasks: 1
 * }
 *
 *
 */
