package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.jlib.common.proto.DeleteWfRunPb;

public class DeleteWfRun extends SubCommand<DeleteWfRunPb> {

    public String wfRunId;

    public Class<DeleteWfRunPb> getProtoBaseClass() {
        return DeleteWfRunPb.class;
    }

    public DeleteWfRunPb.Builder toProto() {
        DeleteWfRunPb.Builder out = DeleteWfRunPb.newBuilder().setWfRunId(wfRunId);
        return out;
    }

    public void initFrom(Message proto) {
        DeleteWfRunPb p = (DeleteWfRunPb) proto;
        wfRunId = p.getWfRunId();
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        return dao.deleteWfRun(wfRunId);
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteWfRun fromProto(DeleteWfRunPb p) {
        DeleteWfRun out = new DeleteWfRun();
        out.initFrom(p);
        return out;
    }
}
