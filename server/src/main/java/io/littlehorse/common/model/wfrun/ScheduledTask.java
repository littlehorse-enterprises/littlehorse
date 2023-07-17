package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSource;
import io.littlehorse.common.model.wfrun.taskrun.UserTaskTriggerReference;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ScheduledTaskPb;
import io.littlehorse.sdk.common.proto.VarNameAndValPb;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledTask extends Storeable<ScheduledTaskPb> {

    private TaskRunId taskRunId;
    private TaskDefId taskDefId;
    private int attemptNumber;

    private List<VarNameAndVal> variables;
    private Date createdAt;

    private TaskRunSource source;

    public ScheduledTask() {
        variables = new ArrayList<>();
    }

    /*
     * Sets attempt number to zero.
     */
    public ScheduledTask(
        TaskDefId taskDefId,
        List<VarNameAndVal> variables,
        UserTaskRun userTaskRun
    ) {
        this.variables = variables;
        this.createdAt = new Date();
        this.source = new TaskRunSource(new UserTaskTriggerReference(userTaskRun));
        this.taskDefId = taskDefId;
        this.attemptNumber = 0;

        // This is just the wfRunId.
        this.taskRunId = new TaskRunId(userTaskRun.getNodeRun().getPartitionKey());
    }

    public String getPartitionKey() {
        return taskRunId.getPartitionKey();
    }

    public String getStoreKey() {
        // Note: only one ScheduledTask can be active at once for a
        // TaskRun, so we don't need to worry about the attemptNumber.
        return taskRunId.getStoreKey();
    }

    public Date getCreatedAt() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        return createdAt;
    }

    public ScheduledTaskPb.Builder toProto() {
        ScheduledTaskPb.Builder out = ScheduledTaskPb
            .newBuilder()
            .setTaskRunId(taskRunId.toProto())
            .setTaskDefId(taskDefId.toProto())
            .setAttemptNumber(attemptNumber)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
            .setSource(source.toProto());

        for (VarNameAndVal v : variables) {
            out.addVariables(v.toProto());
        }

        return out;
    }

    public Class<ScheduledTaskPb> getProtoBaseClass() {
        return ScheduledTaskPb.class;
    }

    public static ScheduledTask fromProto(ScheduledTaskPb p) {
        ScheduledTask out = new ScheduledTask();
        out.initFrom(p);
        return out;
    }

    public void initFrom(Message proto) {
        ScheduledTaskPb p = (ScheduledTaskPb) proto;
        taskRunId = LHSerializable.fromProto(p.getTaskRunId(), TaskRunId.class);
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefId.class);
        attemptNumber = p.getAttemptNumber();

        for (VarNameAndValPb v : p.getVariablesList()) {
            variables.add(LHSerializable.fromProto(v, VarNameAndVal.class));
        }

        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        if (this.createdAt.getTime() == 0) {
            this.createdAt = new Date();
        }

        this.source = LHSerializable.fromProto(p.getSource(), TaskRunSource.class);
    }
}
