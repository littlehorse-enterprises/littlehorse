package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.ResumeWfRunReply;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.ResumeWfRunPb;

public class ResumeWfRun extends SubCommand<ResumeWfRunPb> {

    public String wfRunId;
    public int threadRunNumber;

    public Class<ResumeWfRunPb> getProtoBaseClass() {
        return ResumeWfRunPb.class;
    }

    public ResumeWfRunPb.Builder toProto() {
        ResumeWfRunPb.Builder out = ResumeWfRunPb.newBuilder();
        out.setWfRunId(wfRunId);
        out.setThreadRunNumber(threadRunNumber);
        return out;
    }

    public void initFrom(Message proto) {
        ResumeWfRunPb p = (ResumeWfRunPb) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public ResumeWfRunReply process(LHDAO dao, LHConfig config) {
        ResumeWfRunReply out = new ResumeWfRunReply();
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }

        WfSpecModel wfSpecModel = dao.getWfSpec(
            wfRunModel.wfSpecName,
            wfRunModel.wfSpecVersion
        );
        if (wfSpecModel == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Somehow missing wfSpec for wfRun";
            return out;
        }

        wfRunModel.wfSpecModel = wfSpecModel;
        try {
            wfRunModel.processResumeRequest(this);
            out.code = LHResponseCodePb.OK;
        } catch (LHValidationError exn) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = exn.getMessage();
        }
        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public static ResumeWfRun fromProto(ResumeWfRunPb p) {
        ResumeWfRun out = new ResumeWfRun();
        out.initFrom(p);
        return out;
    }
}
