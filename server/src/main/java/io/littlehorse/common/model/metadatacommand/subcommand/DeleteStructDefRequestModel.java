package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.DeleteStructDefRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public class DeleteStructDefRequestModel extends MetadataSubCommand<DeleteStructDefRequest> {

    private StructDefIdModel id;

    @Override
    public Message process(MetadataProcessorContext context) {
        context.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }

    @Override
    public DeleteStructDefRequest.Builder toProto() {
        DeleteStructDefRequest.Builder out = DeleteStructDefRequest.newBuilder().setId(id.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        DeleteStructDefRequest p = (DeleteStructDefRequest) proto;

        id = LHSerializable.fromProto(p.getId(), StructDefIdModel.class, context);
    }

    @Override
    public Class<DeleteStructDefRequest> getProtoBaseClass() {
        return DeleteStructDefRequest.class;
    }
}
