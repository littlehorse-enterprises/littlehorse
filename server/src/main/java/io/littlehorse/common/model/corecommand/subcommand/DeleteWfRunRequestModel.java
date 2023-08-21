package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;

public class DeleteWfRunRequestModel extends SubCommand<DeleteWfRunRequest> {

    public WfRunIdModel wfRunId;

    public Class<DeleteWfRunRequest> getProtoBaseClass() {
        return DeleteWfRunRequest.class;
    }

    public DeleteWfRunRequest.Builder toProto() {
        DeleteWfRunRequest.Builder out = DeleteWfRunRequest.newBuilder().setId(wfRunId.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        DeleteWfRunRequest p = (DeleteWfRunRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getId(), WfRunIdModel.class);
    }

    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    public Empty process(CoreProcessorDAO dao, LHConfig config) {
        dao.delete(wfRunId);
        return Empty.getDefaultInstance();
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteWfRunRequestModel fromProto(DeleteWfRunRequest p) {
        DeleteWfRunRequestModel out = new DeleteWfRunRequestModel();
        out.initFrom(p);
        return out;
    }
}
