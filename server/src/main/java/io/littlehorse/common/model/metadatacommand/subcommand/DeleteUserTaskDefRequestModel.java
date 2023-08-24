package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;

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

    public void initFrom(Message proto) {
        DeleteUserTaskDefRequest p = (DeleteUserTaskDefRequest) proto;
        id = LHSerializable.fromProto(p.getId(), UserTaskDefIdModel.class);
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

    public static DeleteUserTaskDefRequestModel fromProto(DeleteUserTaskDefRequest p) {
        DeleteUserTaskDefRequestModel out = new DeleteUserTaskDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
