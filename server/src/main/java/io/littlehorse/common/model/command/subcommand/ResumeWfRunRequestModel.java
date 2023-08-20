package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.ResumeWfRunReply;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.ResumeWfRunRequest;

public class ResumeWfRunRequestModel extends SubCommand<ResumeWfRunRequest> {

    public String wfRunId;
    public int threadRunNumber;

    public Class<ResumeWfRunRequest> getProtoBaseClass() {
        return ResumeWfRunRequest.class;
    }

    public ResumeWfRunRequest.Builder toProto() {
        ResumeWfRunRequest.Builder out = ResumeWfRunRequest.newBuilder();
        out.setWfRunId(wfRunId);
        out.setThreadRunNumber(threadRunNumber);
        return out;
    }

    public void initFrom(Message proto) {
        ResumeWfRunRequest p = (ResumeWfRunRequest) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public ResumeWfRunReply process(CoreProcessorDAO dao, LHConfig config) {
        ResumeWfRunReply out = new ResumeWfRunReply();
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
            wfRunModel.processResumeRequest(this);
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

    public static ResumeWfRunRequestModel fromProto(ResumeWfRunRequest p) {
        ResumeWfRunRequestModel out = new ResumeWfRunRequestModel();
        out.initFrom(p);
        return out;
    }
}
