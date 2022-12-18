package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.CommandProcessorDao;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteWfRunReply;
import io.littlehorse.common.proto.DeleteWfRunPb;
import io.littlehorse.common.proto.DeleteWfRunPbOrBuilder;

public class DeleteWfRun extends SubCommand<DeleteWfRunPb> {

    public String wfRunId;

    public Class<DeleteWfRunPb> getProtoBaseClass() {
        return DeleteWfRunPb.class;
    }

    public DeleteWfRunPb.Builder toProto() {
        DeleteWfRunPb.Builder out = DeleteWfRunPb.newBuilder().setWfRunId(wfRunId);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        DeleteWfRunPbOrBuilder p = (DeleteWfRunPbOrBuilder) proto;
        wfRunId = p.getWfRunId();
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public DeleteWfRunReply process(CommandProcessorDao dao, LHConfig config) {
        return dao.deleteWfRun(wfRunId);
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteWfRun fromProto(DeleteWfRunPbOrBuilder p) {
        DeleteWfRun out = new DeleteWfRun();
        out.initFrom(p);
        return out;
    }
}
