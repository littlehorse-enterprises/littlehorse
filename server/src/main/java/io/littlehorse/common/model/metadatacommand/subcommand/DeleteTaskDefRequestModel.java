package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public class DeleteTaskDefRequestModel extends MetadataSubCommand<DeleteTaskDefRequest> {

    public TaskDefIdModel id;

    public Class<DeleteTaskDefRequest> getProtoBaseClass() {
        return DeleteTaskDefRequest.class;
    }

    public DeleteTaskDefRequest.Builder toProto() {
        DeleteTaskDefRequest.Builder out = DeleteTaskDefRequest.newBuilder().setId(id.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        DeleteTaskDefRequest p = (DeleteTaskDefRequest) proto;
        id = LHSerializable.fromProto(p.getId(), TaskDefIdModel.class, context);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public Empty process(MetadataProcessorContext context) {
        context.metadataManager().delete(id);
        context.forward(new DeleteTaskWorkerGroupRequestModel(id));
        return Empty.getDefaultInstance();
    }

    public static DeleteTaskDefRequestModel fromProto(DeleteTaskDefRequest p, ExecutionContext context) {
        DeleteTaskDefRequestModel out = new DeleteTaskDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
