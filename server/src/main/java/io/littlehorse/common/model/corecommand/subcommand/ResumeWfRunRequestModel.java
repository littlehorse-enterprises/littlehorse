package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.ResumeWfRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;

public class ResumeWfRunRequestModel extends CoreSubCommand<ResumeWfRunRequest> {

    public WfRunIdModel wfRunId;
    public int threadRunNumber;

    public Class<ResumeWfRunRequest> getProtoBaseClass() {
        return ResumeWfRunRequest.class;
    }

    public ResumeWfRunRequest.Builder toProto() {
        ResumeWfRunRequest.Builder out = ResumeWfRunRequest.newBuilder();
        out.setWfRunId(wfRunId.toProto());
        out.setThreadRunNumber(threadRunNumber);
        return out;
    }

    public void initFrom(Message proto, ExecutionContext context) {
        ResumeWfRunRequest p = (ResumeWfRunRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        threadRunNumber = p.getThreadRunNumber();
    }

    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    @Override
    public Empty process(CoreProcessorContext executionContext, LHServerConfig config) {
        WfRunModel wfRunModel = executionContext.getableManager().get(new WfRunIdModel(wfRunId.getId()));
        if (wfRunModel == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find provided WfRun");
        }

        wfRunModel.processResumeRequest(this);
        return Empty.getDefaultInstance();
    }

    public static ResumeWfRunRequestModel fromProto(ResumeWfRunRequest p, ExecutionContext context) {
        ResumeWfRunRequestModel out = new ResumeWfRunRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
