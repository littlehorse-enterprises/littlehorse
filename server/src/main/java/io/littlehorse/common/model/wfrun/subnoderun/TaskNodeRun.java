package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VarNameAndVal;
import io.littlehorse.common.model.wfrun.taskrun.TaskNodeReference;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSource;
import io.littlehorse.sdk.common.proto.TaskNodeRunPb;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeRun extends SubNodeRun<TaskNodeRunPb> {

    private TaskRunId taskRunId;

    public Class<TaskNodeRunPb> getProtoBaseClass() {
        return TaskNodeRunPb.class;
    }

    public void initFrom(Message proto) {
        TaskNodeRunPb p = (TaskNodeRunPb) proto;
        if (p.hasTaskRunId()) {
            taskRunId = LHSerializable.fromProto(p.getTaskRunId(), TaskRunId.class);
        }
    }

    public TaskNodeRunPb.Builder toProto() {
        TaskNodeRunPb.Builder out = TaskNodeRunPb.newBuilder();

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
                new Failure(
                    "Appears that TaskDef was deleted!",
                    LHConstants.TASK_ERROR
                ),
                time
            );
            return;
        }

        List<VarNameAndVal> inputVariables;

        try {
            inputVariables =
                node.getTaskNode().assignInputVars(nodeRunModel.getThreadRun());
        } catch (LHVarSubError exn) {
            nodeRunModel.fail(
                new Failure(
                    "Failed calculating TaskRun Input Vars: " + exn.getMessage(),
                    LHConstants.VAR_SUB_ERROR
                ),
                time
            );
            return;
        }

        // Create a TaskRun
        TaskNodeReference source = new TaskNodeReference(
            nodeRunModel.getObjectId(),
            nodeRunModel.getWfSpecId()
        );

        TaskRun task = new TaskRun(
            getDao(),
            inputVariables,
            new TaskRunSource(source),
            node.getTaskNode()
        );
        this.taskRunId = new TaskRunId(nodeRunModel.getPartitionKey());
        task.setId(taskRunId);

        // When creating a new Getable for the first time, we need to explicitly
        // save it.
        getDao().putTaskRun(task);

        // TODO: this should update metrics
        task.scheduleAttempt();
    }
}
