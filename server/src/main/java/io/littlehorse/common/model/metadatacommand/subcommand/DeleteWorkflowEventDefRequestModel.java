package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;

public class DeleteWorkflowEventDefRequestModel extends MetadataSubCommand<DeleteWorkflowEventDefRequest> {

    public WorkflowEventDefIdModel id;

    public Class<DeleteWorkflowEventDefRequest> getProtoBaseClass() {
        return DeleteWorkflowEventDefRequest.class;
    }

    public DeleteWorkflowEventDefRequest.Builder toProto() {
        DeleteWorkflowEventDefRequest.Builder out =
                DeleteWorkflowEventDefRequest.newBuilder().setId(id.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        DeleteWorkflowEventDefRequest p = (DeleteWorkflowEventDefRequest) proto;
        id = LHSerializable.fromProto(p.getId(), WorkflowEventDefIdModel.class, context);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public Empty process(MetadataCommandExecution executionContext) {
        executionContext.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }

    public static DeleteWorkflowEventDefRequestModel fromProto(
            DeleteWorkflowEventDefRequest p, ExecutionContext context) {
        DeleteWorkflowEventDefRequestModel out = new DeleteWorkflowEventDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
