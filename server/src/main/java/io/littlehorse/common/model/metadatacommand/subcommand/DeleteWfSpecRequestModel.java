package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ExecutionContext;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;

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
    public void initFrom(Message proto, io.littlehorse.server.streams.topology.core.ExecutionContext context) {
        DeleteWfSpecRequest p = (DeleteWfSpecRequest) proto;
        id = LHSerializable.fromProto(p.getId(), WfSpecIdModel.class, context);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public Empty process(ExecutionContext dao, LHServerConfig config) {
        dao.delete(id);
        return Empty.getDefaultInstance();
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteWfSpecRequestModel fromProto(DeleteWfSpecRequest p, io.littlehorse.server.streams.topology.core.ExecutionContext context) {
        DeleteWfSpecRequestModel out = new DeleteWfSpecRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
