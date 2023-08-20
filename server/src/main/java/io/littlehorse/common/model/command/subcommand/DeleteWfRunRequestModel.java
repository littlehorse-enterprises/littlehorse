package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;

public class DeleteWfRunRequestModel extends SubCommand<DeleteWfRunRequest> {

    public String wfRunId;

    public Class<DeleteWfRunRequest> getProtoBaseClass() {
        return DeleteWfRunRequest.class;
    }

    public DeleteWfRunRequest.Builder toProto() {
        DeleteWfRunRequest.Builder out = DeleteWfRunRequest.newBuilder().setWfRunId(wfRunId);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteWfRunRequest p = (DeleteWfRunRequest) proto;
        wfRunId = p.getWfRunId();
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public DeleteObjectReply process(CoreProcessorDAO dao, LHConfig config) {
        return dao.deleteWfRun(wfRunId);
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
