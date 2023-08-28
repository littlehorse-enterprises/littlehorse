package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
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

    public Empty process(CoreProcessorDAO dao, LHServerConfig config) {
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find provided WfRun");
        }

        wfRunModel.processResumeRequest(this);
        return Empty.getDefaultInstance();
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
