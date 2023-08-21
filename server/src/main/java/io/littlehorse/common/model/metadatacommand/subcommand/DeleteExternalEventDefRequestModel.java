package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.corecommand.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest;

public class DeleteExternalEventDefRequestModel extends MetadataSubCommand<DeleteExternalEventDefRequest> {

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

    @Override
    public DeleteObjectReply process(MetadataProcessorDAO dao, LHConfig config) {
        return dao.delete(new ExternalEventDefIdModel(name));
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
