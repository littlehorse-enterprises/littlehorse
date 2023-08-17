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
import io.littlehorse.sdk.common.proto.StopWfRunPb;

public class StopWfRun extends SubCommand<StopWfRunPb> {

    public String wfRunId;
    public int threadRunNumber;

    public Class<StopWfRunPb> getProtoBaseClass() {
        return StopWfRunPb.class;
    }

    public StopWfRunPb.Builder toProto() {
        StopWfRunPb.Builder out = StopWfRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber);
        return out;
    }

    public void initFrom(Message proto) {
        StopWfRunPb p = (StopWfRunPb) proto;
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

        WfSpecModel wfSpecModel = dao.getWfSpec(
            wfRunModel.wfSpecName,
            wfRunModel.wfSpecVersion
        );
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

    public static StopWfRun fromProto(StopWfRunPb p) {
        StopWfRun out = new StopWfRun();
        out.initFrom(p);
        return out;
    }
}
