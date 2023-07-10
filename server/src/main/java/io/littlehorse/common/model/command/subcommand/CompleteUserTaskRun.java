package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.CompleteUserTaskRunReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.UserTaskResultPb;
import java.util.Date;

public class CompleteUserTaskRun extends SubCommand<CompleteUserTaskRunPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;
    public String userId;
    public UserTaskResultPb result;
    public Date time;

    public Class<CompleteUserTaskRunPb> getProtoBaseClass() {
        return CompleteUserTaskRunPb.class;
    }

    public CompleteUserTaskRunPb.Builder toProto() {
        CompleteUserTaskRunPb.Builder out = CompleteUserTaskRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setNodeRunPosition(nodeRunPosition)
            .setUserId(userId)
            .setResult(result);
        return out;
    }

    public void initFrom(Message proto) {
        CompleteUserTaskRunPb p = (CompleteUserTaskRunPb) proto;
        wfRunId = p.getWfRunId();
        userId = p.getUserId();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
        result = p.getResult();
    }

    public CompleteUserTaskRunReply process(LHDAO dao, LHConfig config) {
        WfRun wfRun = dao.getWfRun(wfRunId);
        CompleteUserTaskRunReply out = new CompleteUserTaskRunReply();

        if (wfRun == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            wfRun.failDueToWfSpecDeletion();
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Apparently WfSpec was deleted!";
            return out;
        }

        wfRun.wfSpec = wfSpec;
        wfRun.processCompleteUserTaskRun(this);

        // TODO: We don't really check to see if the incoming was valid.
        // For example, we should probably check to make sure that the specified
        // node was actually a user task node...especially if customers are writing
        // their own clients (whereas with Task Workers it is our own code).

        out.code = LHResponseCodePb.OK;
        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public String getPartitionKey() {
        return wfRunId;
    }
}
