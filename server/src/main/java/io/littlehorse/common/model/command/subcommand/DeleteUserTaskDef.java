package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb;

public class DeleteUserTaskDef extends SubCommand<DeleteUserTaskDefPb> {

    public String name;
    public int version;

    public Class<DeleteUserTaskDefPb> getProtoBaseClass() {
        return DeleteUserTaskDefPb.class;
    }

    public DeleteUserTaskDefPb.Builder toProto() {
        DeleteUserTaskDefPb.Builder out = DeleteUserTaskDefPb
            .newBuilder()
            .setName(name)
            .setVersion(version);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteUserTaskDefPb p = (DeleteUserTaskDefPb) proto;
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

    public static DeleteUserTaskDef fromProto(DeleteUserTaskDefPb p) {
        DeleteUserTaskDef out = new DeleteUserTaskDef();
        out.initFrom(p);
        return out;
    }
}
