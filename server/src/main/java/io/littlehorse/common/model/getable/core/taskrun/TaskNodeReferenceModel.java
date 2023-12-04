package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.TaskNodeReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeReferenceModel extends TaskRunSubSource<TaskNodeReference> {

    private NodeRunIdModel nodeRunId;
    private ExecutionContext context;
    private ProcessorExecutionContext processorContext;

    public TaskNodeReferenceModel() {}

    public TaskNodeReferenceModel(NodeRunIdModel nodeRunId, WfSpecIdModel wfSpecId) {
        this.nodeRunId = nodeRunId;
    }

    public Class<TaskNodeReference> getProtoBaseClass() {
        return TaskNodeReference.class;
    }

    public TaskNodeReference.Builder toProto() {
        TaskNodeReference.Builder out = TaskNodeReference.newBuilder().setNodeRunId(nodeRunId.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskNodeReference p = (TaskNodeReference) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
        this.context = context;
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    public void onCompleted(TaskAttemptModel successfulAttept) {
        NodeRunModel nodeRunModel = processorContext.getableManager().get(nodeRunId);
        nodeRunModel.complete(successfulAttept.getOutput(), successfulAttept.getEndTime());
    }

    @Override
    public void onFailed(TaskAttemptModel lastFailure) {
        NodeRunModel nodeRunModel = processorContext.getableManager().get(nodeRunId);

        String message = getMessageFor(lastFailure);
        String errorType = (!lastFailure.containsException() && lastFailure.getError() != null)
                ? lastFailure.getError().getType().name() // case for only technical errors where error type is known
                : getFailureCodeFor(lastFailure); // case for technical and business exception

        FailureModel failure = new FailureModel(message, errorType);

        nodeRunModel.fail(failure, lastFailure.getEndTime());
    }

    private String getFailureCodeFor(TaskAttemptModel lastFailure) {
        switch (lastFailure.getStatus()) {
            case TASK_EXCEPTION:
                return lastFailure.getException().getName();
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
        throw new IllegalArgumentException("Unexpected task status: " + lastFailure.getStatus());
    }

    private String getMessageFor(TaskAttemptModel lastFailure) {
        switch (lastFailure.getStatus()) {
            case TASK_EXCEPTION:
                return lastFailure.getException().getMessage();
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
        throw new IllegalArgumentException("Unexpected task status: " + lastFailure.getStatus());
    }
}
