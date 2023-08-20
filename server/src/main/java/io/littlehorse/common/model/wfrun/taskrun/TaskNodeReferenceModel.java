package io.littlehorse.common.model.wfrun.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.common.model.objectId.WfSpecIdModel;
import io.littlehorse.common.model.wfrun.FailureModel;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.TaskAttemptModel;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.sdk.common.proto.TaskNodeReference;
import io.littlehorse.sdk.common.proto.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeReferenceModel extends TaskRunSubSource<TaskNodeReference> {

    private NodeRunIdModel nodeRunId;
    private WfSpecIdModel wfSpecId;

    public TaskNodeReferenceModel() {}

    public TaskNodeReferenceModel(NodeRunIdModel nodeRunId, WfSpecIdModel wfSpecId) {
        this.nodeRunId = nodeRunId;
        this.wfSpecId = wfSpecId;
    }

    public Class<TaskNodeReference> getProtoBaseClass() {
        return TaskNodeReference.class;
    }

    public TaskNodeReference.Builder toProto() {
        TaskNodeReference.Builder out =
                TaskNodeReference.newBuilder().setWfSpecId(wfSpecId.toProto()).setNodeRunId(nodeRunId.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        TaskNodeReference p = (TaskNodeReference) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class);
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class);
    }

    public void onCompleted(TaskAttemptModel successfulAttept, LHDAO dao) {
        NodeRunModel nodeRunModel = dao.getNodeRun(nodeRunId);
        nodeRunModel.complete(successfulAttept.getOutput(), successfulAttept.getEndTime());
    }

    public void onFailed(TaskAttemptModel lastFailure, LHDAO dao) {
        NodeRunModel nodeRunModel = dao.getNodeRun(nodeRunId);

        String message = getMessageFor(lastFailure.getStatus());
        VariableValueModel stderr = lastFailure.getLogOutput();
        if (stderr != null && stderr.getVal() != null) {
            message += ": " + stderr.getVal().toString();
        }
        nodeRunModel.fail(
                new FailureModel(message, getFailureCodeFor(lastFailure.getStatus())), lastFailure.getEndTime());
    }

    private String getMessageFor(TaskStatus status) {
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

    private String getFailureCodeFor(TaskStatus status) {
        switch (status) {
            case TASK_FAILED:
                return LHConstants.TASK_FAILURE;
            case TASK_TIMEOUT:
                return LHConstants.TIMEOUT;
            case TASK_OUTPUT_SERIALIZING_ERROR:
                return LHConstants.VAR_MUTATION_ERROR;
            case TASK_INPUT_VAR_SUB_ERROR:
                return LHConstants.VAR_SUB_ERROR;
            case TASK_CANCELLED:
                return LHConstants.USER_TASK_CANCELLED;
            case TASK_RUNNING:
            case TASK_SCHEDULED:
            case TASK_SUCCESS:
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("Unexpected task status: " + status);
    }
}
