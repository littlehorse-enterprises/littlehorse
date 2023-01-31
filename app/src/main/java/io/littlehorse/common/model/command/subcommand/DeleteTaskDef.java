package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.proto.DeleteTaskDefPb;
import io.littlehorse.common.proto.DeleteTaskDefPbOrBuilder;

public class DeleteTaskDef extends SubCommand<DeleteTaskDefPb> {

    public String name;
    public int version;

    public Class<DeleteTaskDefPb> getProtoBaseClass() {
        return DeleteTaskDefPb.class;
    }

    public DeleteTaskDefPb.Builder toProto() {
        DeleteTaskDefPb.Builder out = DeleteTaskDefPb
            .newBuilder()
            .setName(name)
            .setVersion(version);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        DeleteTaskDefPbOrBuilder p = (DeleteTaskDefPbOrBuilder) proto;
        name = p.getName();
        version = p.getVersion();
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        return dao.deleteTaskDef(name, version);
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteTaskDef fromProto(DeleteTaskDefPbOrBuilder p) {
        DeleteTaskDef out = new DeleteTaskDef();
        out.initFrom(p);
        return out;
    }
}
