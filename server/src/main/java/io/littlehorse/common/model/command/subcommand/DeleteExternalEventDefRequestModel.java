package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest;

public class DeleteExternalEventDefRequestModel extends SubCommand<DeleteExternalEventDefRequest> {

    public String name;

    public Class<DeleteExternalEventDefRequest> getProtoBaseClass() {
        return DeleteExternalEventDefRequest.class;
    }

    public DeleteExternalEventDefRequest.Builder toProto() {
        DeleteExternalEventDefRequest.Builder out =
                DeleteExternalEventDefRequest.newBuilder().setName(name);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteExternalEventDefRequest p = (DeleteExternalEventDefRequest) proto;
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

    public static DeleteExternalEventDefRequestModel fromProto(DeleteExternalEventDefRequest p) {
        DeleteExternalEventDefRequestModel out = new DeleteExternalEventDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
