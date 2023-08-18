package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.objectId.TaskDefIdModel;
import io.littlehorse.common.model.objectId.TaskRunIdModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSourceModel;
import io.littlehorse.common.model.wfrun.taskrun.UserTaskTriggerReferenceModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
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

    private List<VarNameAndValModel> variables;
    private Date createdAt;

    private TaskRunSourceModel source;
    private UserTaskTriggerContextModel context;

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
            UserTaskTriggerContextModel context) {
        this.variables = variables;
        this.createdAt = new Date();
        this.source = new TaskRunSourceModel(new UserTaskTriggerReferenceModel(userTaskRun));
        this.taskDefId = taskDefId;
        this.attemptNumber = 0;
        this.context = context;

        // This is just the wfRunId.
        this.taskRunId = new TaskRunIdModel(userTaskRun.getNodeRun().getPartitionKey());
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

    public ScheduledTask.Builder toProto() {
        ScheduledTask.Builder out = ScheduledTask.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTaskDefId(taskDefId.toProto())
                .setAttemptNumber(attemptNumber)
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
                .setSource(source.toProto());
        for (VarNameAndValModel v : variables) {
            out.addVariables(v.toProto());
        }

        return out;
    }

    public Class<ScheduledTask> getProtoBaseClass() {
        return ScheduledTask.class;
    }

    public static ScheduledTaskModel fromProto(ScheduledTask p) {
        ScheduledTaskModel out = new ScheduledTaskModel();
        out.initFrom(p);
        return out;
    }

    public void initFrom(Message proto) {
        ScheduledTask p = (ScheduledTask) proto;
        taskRunId = LHSerializable.fromProto(p.getTaskRunId(), TaskRunIdModel.class);
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class);
        attemptNumber = p.getAttemptNumber();

        for (VarNameAndVal v : p.getVariablesList()) {
            variables.add(LHSerializable.fromProto(v, VarNameAndValModel.class));
        }

        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        if (this.createdAt.getTime() == 0) {
            this.createdAt = new Date();
        }
        this.source = LHSerializable.fromProto(p.getSource(), TaskRunSourceModel.class);
    }
}
