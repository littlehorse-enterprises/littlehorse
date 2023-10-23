package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;

public class StopWfRunRequestModel extends CoreSubCommand<StopWfRunRequest> {

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

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config, String tenantId) {
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfRun");
        }

        wfRunModel.processStopRequest(this);
        return Empty.getDefaultInstance();
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
