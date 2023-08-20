package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;

public class DeleteTaskDefRequestModel extends SubCommand<DeleteTaskDefRequest> {

    public String name;
    public int version;

    public Class<DeleteTaskDefRequest> getProtoBaseClass() {
        return DeleteTaskDefRequest.class;
    }

    public DeleteTaskDefRequest.Builder toProto() {
        DeleteTaskDefRequest.Builder out = DeleteTaskDefRequest.newBuilder().setName(name);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteTaskDefRequest p = (DeleteTaskDefRequest) proto;
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

    public static DeleteTaskDefRequestModel fromProto(DeleteTaskDefRequest p) {
        DeleteTaskDefRequestModel out = new DeleteTaskDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
