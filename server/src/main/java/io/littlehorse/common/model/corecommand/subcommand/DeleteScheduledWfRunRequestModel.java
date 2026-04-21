package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class DeleteScheduledWfRunRequestModel extends CoreSubCommand<DeleteScheduledWfRunRequest> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DeleteScheduledWfRunRequestModel.class);
    private ScheduledWfRunIdModel id;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
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
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        executionContext.getableManager().delete(id);
        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return id.getPartitionKey().get();
    }
}
