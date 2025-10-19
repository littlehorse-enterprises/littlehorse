package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunSourceModel;
import io.littlehorse.common.model.getable.core.taskrun.UserTaskTriggerReferenceModel;
import io.littlehorse.common.model.getable.core.taskrun.VarNameAndValModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledTaskModel extends Storeable<ScheduledTask> {

    private TaskRunIdModel taskRunId;
    private TaskDefIdModel taskDefId;
    private int attemptNumber;
    private int totalCheckpoints;

    private List<VarNameAndValModel> variables;
    private Date createdAt;

    private TaskRunSourceModel source;

    public ScheduledTaskModel() {
        variables = new ArrayList<>();
    }

    /*
     * Sets attempt number to zero.
     */
    public ScheduledTaskModel(
            TaskDefIdModel taskDefId,
            List<VarNameAndValModel> variables,
            UserTaskRunModel userTaskRun,
            CoreProcessorContext processorContext) {
        this.variables = variables;
        this.createdAt = new Date();
        this.source = new TaskRunSourceModel(
                new UserTaskTriggerReferenceModel(userTaskRun, processorContext), processorContext);
        this.taskDefId = taskDefId;
        this.attemptNumber = 0;

        // This is just the wfRunId.
        this.taskRunId = new TaskRunIdModel(userTaskRun);
    }

    @Override
    public ScheduledTask.Builder toProto() {
        ScheduledTask.Builder out = ScheduledTask.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTaskDefId(taskDefId.toProto())
                .setAttemptNumber(attemptNumber)
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
                .setSource(source.toProto())
                .setTotalObservedCheckpoints(totalCheckpoints);
        for (VarNameAndValModel v : variables) {
            out.addVariables(v.toProto());
        }

        return out;
    }

    @Override
    public Class<ScheduledTask> getProtoBaseClass() {
        return ScheduledTask.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ScheduledTask p = (ScheduledTask) proto;
        taskRunId = LHSerializable.fromProto(p.getTaskRunId(), TaskRunIdModel.class, context);
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        attemptNumber = p.getAttemptNumber();
        this.totalCheckpoints = p.getTotalObservedCheckpoints();

        for (VarNameAndVal v : p.getVariablesList()) {
            variables.add(LHSerializable.fromProto(v, VarNameAndValModel.class, context));
        }

        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        if (this.createdAt.getTime() == 0) {
            this.createdAt = new Date();
        }
        this.source = LHSerializable.fromProto(p.getSource(), TaskRunSourceModel.class, context);
    }

    @Override
    public StoreableType getType() {
        return StoreableType.SCHEDULED_TASK;
    }

    @Override
    public String getStoreKey() {
        return ScheduledTaskModel.getScheduledTaskKey(taskRunId, createdAt);
    }

    public static ScheduledTaskModel fromProto(ScheduledTask p, ExecutionContext context) {
        ScheduledTaskModel out = new ScheduledTaskModel();
        out.initFrom(p, context);
        return out;
    }

    public static String getLegacyKey(TaskRunModel taskRun) {
        return taskRun.getId().toString();
    }

    public static String getScheduledTaskKey(TaskRunModel taskRun) {
        return getScheduledTaskKey(taskRun.getId(), taskRun.getLatestAttempt().getScheduleTime());
    }

    public static String getScheduledTaskKey(TaskRunIdModel taskRunId, Date taskAttemptCreatedAt) {
        // Note: only one ScheduledTask can be active at once for a
        // TaskRun, so we don't need to worry about the attemptNumber.
        //
        // For compatibility we use "a" as a prefix since it is the first ASCII
        // character. This guarantees that the start of the iteration doesn't ignore
        // any previous keys.
        return "a/" + LHUtil.toLhDbFormat(taskAttemptCreatedAt) + "/" + taskRunId.toString();
    }
}
