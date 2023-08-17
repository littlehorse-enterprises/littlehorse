package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;

public class DeleteWfSpecRequestModel extends SubCommand<DeleteWfSpecRequest> {

    public String name;
    public int version;

    public Class<DeleteWfSpecRequest> getProtoBaseClass() {
        return DeleteWfSpecRequest.class;
    }

    public DeleteWfSpecRequest.Builder toProto() {
        DeleteWfSpecRequest.Builder out = DeleteWfSpecRequest
            .newBuilder()
            .setName(name)
            .setVersion(version);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteWfSpecRequest p = (DeleteWfSpecRequest) proto;
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

    public static DeleteWfSpecRequestModel fromProto(DeleteWfSpecRequest p) {
        DeleteWfSpecRequestModel out = new DeleteWfSpecRequestModel();
        out.initFrom(p);
        return out;
    }
}
