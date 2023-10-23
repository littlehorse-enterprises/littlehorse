package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;

public class DeleteWfRunRequestModel extends CoreSubCommand<DeleteWfRunRequest> {

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

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config, String tenantId) {
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
