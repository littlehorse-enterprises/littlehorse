package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteScheduledWfRunRequestModel extends MetadataSubCommand<DeleteScheduledWfRunRequest> {

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
    public Message process(MetadataCommandExecution executionContext) {
        executionContext.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }
}
