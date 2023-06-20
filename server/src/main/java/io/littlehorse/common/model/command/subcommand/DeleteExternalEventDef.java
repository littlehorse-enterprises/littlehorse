package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.jlib.common.proto.DeleteExternalEventDefPb;

public class DeleteExternalEventDef extends SubCommand<DeleteExternalEventDefPb> {

    public String name;

    public Class<DeleteExternalEventDefPb> getProtoBaseClass() {
        return DeleteExternalEventDefPb.class;
    }

    public DeleteExternalEventDefPb.Builder toProto() {
        DeleteExternalEventDefPb.Builder out = DeleteExternalEventDefPb
            .newBuilder()
            .setName(name);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteExternalEventDefPb p = (DeleteExternalEventDefPb) proto;
        name = p.getName();
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        return dao.deleteExternalEventDef(name);
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteExternalEventDef fromProto(DeleteExternalEventDefPb p) {
        DeleteExternalEventDef out = new DeleteExternalEventDef();
        out.initFrom(p);
        return out;
    }
}
