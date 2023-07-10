package io.littlehorse.common.model.wfrun.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.TaskAttempt;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.sdk.common.proto.TaskNodeReferencePb;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeReference extends TaskRunSubSource<TaskNodeReferencePb> {

    private NodeRunId nodeRunId;
    private WfSpecId wfSpecId;

    public TaskNodeReference() {}

    public TaskNodeReference(NodeRunId nodeRunId, WfSpecId wfSpecId) {
        this.nodeRunId = nodeRunId;
        this.wfSpecId = wfSpecId;
    }

    public Class<TaskNodeReferencePb> getProtoBaseClass() {
        return TaskNodeReferencePb.class;
    }

    public TaskNodeReferencePb.Builder toProto() {
        TaskNodeReferencePb.Builder out = TaskNodeReferencePb
            .newBuilder()
            .setWfSpecId(wfSpecId.toProto())
            .setNodeRunId(nodeRunId.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        TaskNodeReferencePb p = (TaskNodeReferencePb) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunId.class);
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecId.class);
    }

    public void onCompleted(TaskAttempt successfulAttept, LHDAO dao) {
        NodeRun nodeRun = dao.getNodeRun(nodeRunId);
        nodeRun.complete(successfulAttept.getOutput(), successfulAttept.getEndTime());
    }

    public void onFailed(TaskAttempt lastFailure, LHDAO dao) {
        NodeRun nodeRun = dao.getNodeRun(nodeRunId);

        String message = getMessageFor(lastFailure.getStatus());
        VariableValue stderr = lastFailure.getLogOutput();
        if (stderr != null && stderr.getVal() != null) {
            message += ": " + stderr.getVal().toString();
        }
        nodeRun.fail(
            new Failure(message, getFailureCodeFor(lastFailure.getStatus())),
            lastFailure.getEndTime()
        );
    }

    private String getMessageFor(TaskStatusPb status) {
        switch (status) {
            case TASK_FAILED:
                return "Task execution failed";
            case TASK_TIMEOUT:
                return "Task timed out";
            case TASK_OUTPUT_SERIALIZING_ERROR:
                return "Failed serializing Task Output";
            case TASK_INPUT_VAR_SUB_ERROR:
                return "Failed calculating Task Input Variables";
            case TASK_RUNNING:
            case TASK_SCHEDULED:
            case TASK_SUCCESS:
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("Unexpected task status: " + status);
    }

    private String getFailureCodeFor(TaskStatusPb status) {
        switch (status) {
            case TASK_FAILED:
                return LHConstants.TASK_FAILURE;
            case TASK_TIMEOUT:
                return LHConstants.TIMEOUT;
            case TASK_OUTPUT_SERIALIZING_ERROR:
                return LHConstants.VAR_MUTATION_ERROR;
            case TASK_INPUT_VAR_SUB_ERROR:
                return LHConstants.VAR_SUB_ERROR;
            case TASK_RUNNING:
            case TASK_SCHEDULED:
            case TASK_SUCCESS:
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("Unexpected task status: " + status);
    }
}
