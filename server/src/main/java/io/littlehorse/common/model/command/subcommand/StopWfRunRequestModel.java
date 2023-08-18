package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.StopWfRunReply;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;

public class StopWfRunRequestModel extends SubCommand<StopWfRunRequest> {

    public String wfRunId;
    public int threadRunNumber;

    public Class<StopWfRunRequest> getProtoBaseClass() {
        return StopWfRunRequest.class;
    }

    public StopWfRunRequest.Builder toProto() {
        StopWfRunRequest.Builder out =
                StopWfRunRequest.newBuilder().setWfRunId(wfRunId).setThreadRunNumber(threadRunNumber);
        return out;
    }

    public void initFrom(Message proto) {
        StopWfRunRequest p = (StopWfRunRequest) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public StopWfRunReply process(LHDAO dao, LHConfig config) {
        StopWfRunReply out = new StopWfRunReply();
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel == null) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }

        WfSpecModel wfSpecModel = dao.getWfSpec(wfRunModel.wfSpecName, wfRunModel.wfSpecVersion);
        if (wfSpecModel == null) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message = "Somehow missing wfSpec for wfRun";
            return out;
        }

        wfRunModel.wfSpecModel = wfSpecModel;
        try {
            wfRunModel.processStopRequest(this);
            out.code = LHResponseCode.OK;
        } catch (LHValidationError exn) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message = exn.getMessage();
        }

        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public static StopWfRunRequestModel fromProto(StopWfRunRequest p) {
        StopWfRunRequestModel out = new StopWfRunRequestModel();
        out.initFrom(p);
        return out;
    }
}
