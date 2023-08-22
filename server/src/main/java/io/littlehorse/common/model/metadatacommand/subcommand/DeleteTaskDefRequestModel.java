package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;

public class DeleteTaskDefRequestModel extends MetadataSubCommand<DeleteTaskDefRequest> {

    public TaskDefIdModel id;

    public Class<DeleteTaskDefRequest> getProtoBaseClass() {
        return DeleteTaskDefRequest.class;
    }

    public DeleteTaskDefRequest.Builder toProto() {
        DeleteTaskDefRequest.Builder out = DeleteTaskDefRequest.newBuilder().setId(id.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        DeleteTaskDefRequest p = (DeleteTaskDefRequest) proto;
        id = LHSerializable.fromProto(p.getId(), TaskDefIdModel.class);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public Empty process(MetadataProcessorDAO dao, LHConfig config) {
        dao.delete(id);
        return Empty.getDefaultInstance();
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteTaskDefRequestModel fromProto(DeleteTaskDefRequest p) {
        DeleteTaskDefRequestModel out = new DeleteTaskDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
