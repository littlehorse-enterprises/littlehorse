package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;

public class CancelUserTaskRunRequestModel extends CoreSubCommand<CancelUserTaskRunRequest> {

    private UserTaskRunIdModel userTaskRunId;

    @Override
    public CancelUserTaskRunRequest.Builder toProto() {
        return CancelUserTaskRunRequest.newBuilder().setUserTaskRunId(userTaskRunId.toProto());
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        CancelUserTaskRunRequest cancelUserTaskRunPb = (CancelUserTaskRunRequest) proto;
        userTaskRunId = LHSerializable.fromProto(cancelUserTaskRunPb.getUserTaskRunId(), UserTaskRunIdModel.class);
    }

    @Override
    public Class<CancelUserTaskRunRequest> getProtoBaseClass() {
        return CancelUserTaskRunRequest.class;
    }

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config) {
        UserTaskRunModel userTaskRun = dao.get(userTaskRunId);
        if (userTaskRun == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified UserTaskRun");
        }
        userTaskRun.cancel();
        return Empty.getDefaultInstance();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }
}
