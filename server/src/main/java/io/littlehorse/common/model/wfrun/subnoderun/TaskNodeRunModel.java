package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.objectId.TaskRunIdModel;
import io.littlehorse.common.model.wfrun.FailureModel;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VarNameAndValModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskNodeReferenceModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSourceModel;
import io.littlehorse.sdk.common.proto.TaskNodeRun;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeRunModel extends SubNodeRun<TaskNodeRun> {

    private TaskRunIdModel taskRunId;

    public Class<TaskNodeRun> getProtoBaseClass() {
        return TaskNodeRun.class;
    }

    public void initFrom(Message proto) {
        TaskNodeRun p = (TaskNodeRun) proto;
        if (p.hasTaskRunId()) {
            taskRunId = LHSerializable.fromProto(p.getTaskRunId(), TaskRunIdModel.class);
        }
    }

    public TaskNodeRun.Builder toProto() {
        TaskNodeRun.Builder out = TaskNodeRun.newBuilder();

        if (taskRunId != null) out.setTaskRunId(taskRunId.toProto());

        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // Task Node does not care about other ThreadRuns or ExternalEvents;
        // therefore we only advance the node when the TaskRun completes.
        return false;
    }

    public void arrive(Date time) {
        // The TaskNode arrive() function should create a TaskRun. Note that
        // creating a TaskRun also causes the first TaskAttempt to be scheduled.

        NodeModel node = nodeRunModel.getNode();

        TaskDefModel td = node.getTaskNode().getTaskDef();
        if (td == null) {
            // that means the TaskDef was deleted between now and the time that the
            // WfSpec was first created. Yikers!
            nodeRunModel.fail(
                    new FailureModel("Appears that TaskDef was deleted!", LHConstants.TASK_ERROR),
                    time);
            return;
        }

        List<VarNameAndValModel> inputVariables;

        try {
            inputVariables = node.getTaskNode().assignInputVars(nodeRunModel.getThreadRun());
        } catch (LHVarSubError exn) {
            nodeRunModel.fail(
                    new FailureModel(
                            "Failed calculating TaskRun Input Vars: " + exn.getMessage(),
                            LHConstants.VAR_SUB_ERROR),
                    time);
            return;
        }

        // Create a TaskRun
        TaskNodeReferenceModel source =
                new TaskNodeReferenceModel(nodeRunModel.getObjectId(), nodeRunModel.getWfSpecId());

        TaskRunModel task =
                new TaskRunModel(
                        getDao(),
                        inputVariables,
                        new TaskRunSourceModel(source),
                        node.getTaskNode());
        this.taskRunId = new TaskRunIdModel(nodeRunModel.getPartitionKey());
        task.setId(taskRunId);

        // When creating a new Getable for the first time, we need to explicitly
        // save it.
        getDao().putTaskRun(task);

        // TODO: this should update metrics
        task.scheduleAttempt();
    }
}
