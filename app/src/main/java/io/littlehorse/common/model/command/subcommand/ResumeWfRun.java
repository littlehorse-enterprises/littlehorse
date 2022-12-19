package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.ResumeWfRunReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.ResumeWfRunPb;
import io.littlehorse.common.proto.ResumeWfRunPbOrBuilder;

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

    public void initFrom(MessageOrBuilder proto) {
        ResumeWfRunPbOrBuilder p = (ResumeWfRunPbOrBuilder) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public ResumeWfRunReply process(LHDAO dao, LHConfig config) {
        ResumeWfRunReply out = new ResumeWfRunReply();
        WfRun wfRun = dao.getWfRun(wfRunId);
        if (wfRun == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Somehow missing wfSpec for wfRun";
            return out;
        }

        wfRun.wfSpec = wfSpec;
        wfRun.cmdDao = dao;
        try {
            wfRun.processResumeRequest(this);
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

    public static ResumeWfRun fromProto(ResumeWfRunPbOrBuilder p) {
        ResumeWfRun out = new ResumeWfRun();
        out.initFrom(p);
        return out;
    }
}
