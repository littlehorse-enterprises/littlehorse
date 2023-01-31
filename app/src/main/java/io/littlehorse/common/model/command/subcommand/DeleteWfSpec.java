package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.proto.DeleteWfSpecPb;
import io.littlehorse.common.proto.DeleteWfSpecPbOrBuilder;

public class DeleteWfSpec extends SubCommand<DeleteWfSpecPb> {

    public String name;
    public int version;

    public Class<DeleteWfSpecPb> getProtoBaseClass() {
        return DeleteWfSpecPb.class;
    }

    public DeleteWfSpecPb.Builder toProto() {
        DeleteWfSpecPb.Builder out = DeleteWfSpecPb
            .newBuilder()
            .setName(name)
            .setVersion(version);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        DeleteWfSpecPbOrBuilder p = (DeleteWfSpecPbOrBuilder) proto;
        name = p.getName();
        version = p.getVersion();
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        return dao.deleteWfSpec(name, version);
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteWfSpec fromProto(DeleteWfSpecPbOrBuilder p) {
        DeleteWfSpec out = new DeleteWfSpec();
        out.initFrom(p);
        return out;
    }
}
