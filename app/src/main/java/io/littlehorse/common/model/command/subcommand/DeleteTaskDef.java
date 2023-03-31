package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.jlib.common.proto.DeleteTaskDefPb;

public class DeleteTaskDef extends SubCommand<DeleteTaskDefPb> {

    public String name;
    public int version;

    public Class<DeleteTaskDefPb> getProtoBaseClass() {
        return DeleteTaskDefPb.class;
    }

    public DeleteTaskDefPb.Builder toProto() {
        DeleteTaskDefPb.Builder out = DeleteTaskDefPb.newBuilder().setName(name);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteTaskDefPb p = (DeleteTaskDefPb) proto;
        name = p.getName();
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        return dao.deleteTaskDef(name);
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteTaskDef fromProto(DeleteTaskDefPb p) {
        DeleteTaskDef out = new DeleteTaskDef();
        out.initFrom(p);
        return out;
    }
}
