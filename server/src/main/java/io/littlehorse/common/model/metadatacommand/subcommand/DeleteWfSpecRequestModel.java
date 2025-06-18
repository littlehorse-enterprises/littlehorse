package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public class DeleteWfSpecRequestModel extends MetadataSubCommand<DeleteWfSpecRequest> {

    public WfSpecIdModel id;

    public Class<DeleteWfSpecRequest> getProtoBaseClass() {
        return DeleteWfSpecRequest.class;
    }

    public DeleteWfSpecRequest.Builder toProto() {
        DeleteWfSpecRequest.Builder out = DeleteWfSpecRequest.newBuilder().setId(id.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        DeleteWfSpecRequest p = (DeleteWfSpecRequest) proto;
        id = LHSerializable.fromProto(p.getId(), WfSpecIdModel.class, context);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public Empty process(MetadataProcessorContext context) {
        context.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }

    public static DeleteWfSpecRequestModel fromProto(DeleteWfSpecRequest p, ExecutionContext context) {
        DeleteWfSpecRequestModel out = new DeleteWfSpecRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
