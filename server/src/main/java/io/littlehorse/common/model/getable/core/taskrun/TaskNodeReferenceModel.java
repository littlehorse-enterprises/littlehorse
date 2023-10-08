package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
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

    public void onCompleted(TaskAttemptModel successfulAttept, CoreProcessorDAO dao) {
        NodeRunModel nodeRunModel = dao.get(nodeRunId);
        nodeRunModel.complete(successfulAttept.getOutput(), successfulAttept.getEndTime());
    }

    public void onFailed(TaskAttemptModel lastFailure, CoreProcessorDAO dao) {
        NodeRunModel nodeRunModel = dao.get(nodeRunId);
        FailureModel failure;
        if (!lastFailure.containsException()) {
            String message = getMessageFor(lastFailure.getStatus());
            VariableValueModel stderr = lastFailure.getLogOutput();
            if (stderr != null && stderr.getVal() != null) {
                message += ": " + stderr.getVal().toString();
            }
            if (lastFailure.getError() == null) { // check for compatibility
                failure = new FailureModel(message, getFailureCodeFor(lastFailure.getStatus()));
            } else {
                failure = new FailureModel(
                        message, lastFailure.getError().getType().name());
            }
        } else {
            failure = new FailureModel(
                    lastFailure.getException().getMessage(),
                    lastFailure.getException().getName());
        }
        nodeRunModel.fail(failure, lastFailure.getEndTime());
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
            case TASK_CANCELLED: // TODO: WTF? This is supposed to be for user task.
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("Unexpected task status: " + status);
    }
}
