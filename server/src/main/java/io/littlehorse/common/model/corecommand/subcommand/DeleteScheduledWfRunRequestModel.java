package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.ScheduledWfRunModel;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.extern.slf4j.Slf4j;

// Delete an existing ScheduledWfRun, returns INVALID_ARGUMENT if object does not exist
@Slf4j
public class DeleteScheduledWfRunRequestModel extends CoreSubCommand<DeleteScheduledWfRunRequest> {

    private ScheduledWfRunIdModel id;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        DeleteScheduledWfRunRequest p = (DeleteScheduledWfRunRequest) proto;
        id = LHSerializable.fromProto(p.getId(), ScheduledWfRunIdModel.class, context);
    }

    @Override
    public DeleteScheduledWfRunRequest.Builder toProto() {
        return DeleteScheduledWfRunRequest.newBuilder().setId(id.toProto());
    }

    @Override
    public Class<DeleteScheduledWfRunRequest> getProtoBaseClass() {
        return DeleteScheduledWfRunRequest.class;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        ScheduledWfRunModel deletedWfRun = executionContext.getableManager().delete(id);
        if (deletedWfRun == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Id not found");
        }
        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        // TODO: determine partition key
        return "";
    }
}
