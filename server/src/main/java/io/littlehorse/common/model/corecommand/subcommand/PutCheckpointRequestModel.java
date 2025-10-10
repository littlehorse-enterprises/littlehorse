package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.taskrun.CheckpointModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskAttemptModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.CheckpointIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.sdk.common.proto.PutCheckpointRequest;
import io.littlehorse.sdk.common.proto.PutCheckpointResponse;
import io.littlehorse.sdk.common.proto.PutCheckpointResponse.FlowControlContinue;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class PutCheckpointRequestModel extends CoreSubCommand<PutCheckpointRequest> {

    private TaskRunIdModel taskRunId;
    private int taskAttempt;
    private VariableValueModel value;
    private String logs;

    @Override
    public Class<PutCheckpointRequest> getProtoBaseClass() {
        return PutCheckpointRequest.class;
    }

    @Override
    public PutCheckpointRequest.Builder toProto() {
        PutCheckpointRequest.Builder out = PutCheckpointRequest.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTaskAttempt(taskAttempt)
                .setValue(value.toProto());

        if (logs != null) out.setLogs(logs);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        PutCheckpointRequest p = (PutCheckpointRequest) proto;
        if (p.hasLogs()) logs = p.getLogs();
        taskAttempt = p.getTaskAttempt();
        value = VariableValueModel.fromProto(p.getValue(), ignored);
        taskRunId = LHSerializable.fromProto(p.getTaskRunId(), TaskRunIdModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return taskRunId.getPartitionKey().get();
    }

    @Override
    public PutCheckpointResponse process(CoreProcessorContext context, LHServerConfig config) {
        GetableManager manager = context.getableManager();
        TaskRunModel taskRun = manager.get(taskRunId);
        if (taskRun == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Could not find specified TaskRun");
        }

        int currentAttemptNumber = taskRun.getAttempts().size() - 1;
        if (currentAttemptNumber != taskAttempt) {
            // We need to fence the Task Worker.
            return PutCheckpointResponse.newBuilder()
                    .setFlowControlContinueType(FlowControlContinue.STOP_TASK)
                    .build();
        }

        TaskAttemptModel currentAttempt = taskRun.getLatestAttempt();
        if (currentAttempt.getStatus() != TaskStatus.TASK_RUNNING) {
            // We need to fence the Task Worker.
            return PutCheckpointResponse.newBuilder()
                    .setFlowControlContinueType(FlowControlContinue.STOP_TASK)
                    .build();
        }

        // Now we create the checkpoint.
        CheckpointIdModel checkpointId = new CheckpointIdModel(taskRunId, taskRun.getTotalCheckpoints());

        CheckpointModel checkpoint = new CheckpointModel();
        checkpoint.setCreatedAt(new Date());
        checkpoint.setId(checkpointId);
        checkpoint.setLogs(logs);
        checkpoint.setValue(value);

        manager.put(checkpoint);

        // Lastly, we need to reset the Timeout on the TaskRun.
        taskRun.observeNewCheckpointAndUpdateTimeouts(context);

        // Tell Task Worker to carry on.
        return PutCheckpointResponse.newBuilder()
                .setFlowControlContinueType(FlowControlContinue.CONTINUE_TASK)
                .setCreatedCheckpoint(checkpoint.toProto())
                .build();
    }
}
