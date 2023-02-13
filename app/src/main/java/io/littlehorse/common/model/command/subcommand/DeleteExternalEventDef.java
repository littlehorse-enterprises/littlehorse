package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.jlib.common.proto.DeleteExternalEventDefPb;
import io.littlehorse.jlib.common.proto.DeleteExternalEventDefPbOrBuilder;

public class DeleteExternalEventDef extends SubCommand<DeleteExternalEventDefPb> {

    public String name;
    public int version;

    public Class<DeleteExternalEventDefPb> getProtoBaseClass() {
        return DeleteExternalEventDefPb.class;
    }

    public DeleteExternalEventDefPb.Builder toProto() {
        DeleteExternalEventDefPb.Builder out = DeleteExternalEventDefPb
            .newBuilder()
            .setName(name)
            .setVersion(version);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        DeleteExternalEventDefPbOrBuilder p = (DeleteExternalEventDefPbOrBuilder) proto;
        name = p.getName();
        version = p.getVersion();
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        return dao.deleteExternalEventDef(name, version);
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteExternalEventDef fromProto(
        DeleteExternalEventDefPbOrBuilder p
    ) {
        DeleteExternalEventDef out = new DeleteExternalEventDef();
        out.initFrom(p);
        return out;
    }
}
