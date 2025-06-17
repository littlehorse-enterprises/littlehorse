package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskWorkerGroupIdModel;
import io.littlehorse.common.proto.DeleteTaskWorkerGroupRequest;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;

public class DeleteTaskWorkerGroupRequestModel extends CoreSubCommand<DeleteTaskWorkerGroupRequest> {

    private TaskDefIdModel taskDefId;

    public DeleteTaskWorkerGroupRequestModel() {}

    public DeleteTaskWorkerGroupRequestModel(TaskDefIdModel taskDefId) {
        this.taskDefId = taskDefId;
    }

    @Override
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        executionContext.getableManager().delete(new TaskWorkerGroupIdModel(taskDefId));
        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return taskDefId.toString();
    }

    @Override
    public DeleteTaskWorkerGroupRequest.Builder toProto() {
        DeleteTaskWorkerGroupRequest.Builder builder = DeleteTaskWorkerGroupRequest.newBuilder();
        builder.setId(taskDefId.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        DeleteTaskWorkerGroupRequest p = (DeleteTaskWorkerGroupRequest) proto;
        this.taskDefId = LHSerializable.fromProto(p.getId(), TaskDefIdModel.class, context);
    }

    @Override
    public Class<DeleteTaskWorkerGroupRequest> getProtoBaseClass() {
        return DeleteTaskWorkerGroupRequest.class;
    }
}
