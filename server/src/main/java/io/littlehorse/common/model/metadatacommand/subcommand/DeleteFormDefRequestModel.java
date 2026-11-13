package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.FormDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.DeleteFormDefRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public class DeleteFormDefRequestModel extends MetadataSubCommand<DeleteFormDefRequest> {

    private FormDefIdModel id;

    @Override
    public Message process(MetadataProcessorContext context) {
        context.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }

    @Override
    public DeleteFormDefRequest.Builder toProto() {
        return DeleteFormDefRequest.newBuilder().setId(id.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        id = LHSerializable.fromProto(((DeleteFormDefRequest) proto).getId(), FormDefIdModel.class, context);
    }

    @Override
    public Class<DeleteFormDefRequest> getProtoBaseClass() {
        return DeleteFormDefRequest.class;
    }
}
