package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.corecommand.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;

public class DeleteWfSpecRequestModel extends MetadataSubCommand<DeleteWfSpecRequest> {

    public String name;
    public int version;

    public Class<DeleteWfSpecRequest> getProtoBaseClass() {
        return DeleteWfSpecRequest.class;
    }

    public DeleteWfSpecRequest.Builder toProto() {
        DeleteWfSpecRequest.Builder out =
                DeleteWfSpecRequest.newBuilder().setName(name).setVersion(version);
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

    public DeleteObjectReply process(MetadataProcessorDAO dao, LHConfig config) {
        return dao.delete(new WfSpecIdModel(name, version));
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
