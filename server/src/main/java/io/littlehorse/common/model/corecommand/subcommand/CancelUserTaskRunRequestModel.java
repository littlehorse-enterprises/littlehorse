package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class CancelUserTaskRunRequestModel extends CoreSubCommand<CancelUserTaskRunRequest> {

    private UserTaskRunIdModel userTaskRunId;

    public CancelUserTaskRunRequestModel() {}

    public CancelUserTaskRunRequestModel(UserTaskRunIdModel userTaskRunId) {
        this.userTaskRunId = userTaskRunId;
    }

    @Override
    public CancelUserTaskRunRequest.Builder toProto() {
        return CancelUserTaskRunRequest.newBuilder().setUserTaskRunId(userTaskRunId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        CancelUserTaskRunRequest cancelUserTaskRunPb = (CancelUserTaskRunRequest) proto;
        userTaskRunId =
                LHSerializable.fromProto(cancelUserTaskRunPb.getUserTaskRunId(), UserTaskRunIdModel.class, context);
    }

    @Override
    public Class<CancelUserTaskRunRequest> getProtoBaseClass() {
        return CancelUserTaskRunRequest.class;
    }

    @Override
    public Empty process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        UserTaskRunModel userTaskRun = executionContext.getableManager().get(userTaskRunId);
        if (userTaskRun == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified UserTaskRun");
        }
        userTaskRun.cancel();
        executionContext
                .getableManager()
                .get(userTaskRunId.getWfRunId())
                .advance(executionContext.currentCommand().getTime());
        return Empty.getDefaultInstance();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }
}
