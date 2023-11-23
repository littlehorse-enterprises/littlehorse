package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;

public class DeleteUserTaskDefRequestModel extends MetadataSubCommand<DeleteUserTaskDefRequest> {

    public UserTaskDefIdModel id;

    public Class<DeleteUserTaskDefRequest> getProtoBaseClass() {
        return DeleteUserTaskDefRequest.class;
    }

    public DeleteUserTaskDefRequest.Builder toProto() {
        DeleteUserTaskDefRequest.Builder out =
                DeleteUserTaskDefRequest.newBuilder().setId(id.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, io.littlehorse.server.streams.topology.core.ExecutionContext context) {
        DeleteUserTaskDefRequest p = (DeleteUserTaskDefRequest) proto;
        id = LHSerializable.fromProto(p.getId(), UserTaskDefIdModel.class, context);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public Empty process(MetadataCommandExecution context) {
        context.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteUserTaskDefRequestModel fromProto(
            DeleteUserTaskDefRequest p, io.littlehorse.server.streams.topology.core.ExecutionContext context) {
        DeleteUserTaskDefRequestModel out = new DeleteUserTaskDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
