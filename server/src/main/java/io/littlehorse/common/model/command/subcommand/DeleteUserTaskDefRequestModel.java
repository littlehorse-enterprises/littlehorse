package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;

public class DeleteUserTaskDefRequestModel extends SubCommand<DeleteUserTaskDefRequest> {

    public String name;
    public int version;

    public Class<DeleteUserTaskDefRequest> getProtoBaseClass() {
        return DeleteUserTaskDefRequest.class;
    }

    public DeleteUserTaskDefRequest.Builder toProto() {
        DeleteUserTaskDefRequest.Builder out =
                DeleteUserTaskDefRequest.newBuilder().setName(name).setVersion(version);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteUserTaskDefRequest p = (DeleteUserTaskDefRequest) proto;
        name = p.getName();
        version = p.getVersion();
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        return dao.deleteUserTaskDef(name, version);
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
