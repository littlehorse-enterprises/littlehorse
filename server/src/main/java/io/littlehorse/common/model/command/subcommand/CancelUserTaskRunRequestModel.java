package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.CancelUserTaskRunReply;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class CancelUserTaskRunRequestModel extends SubCommand<CancelUserTaskRunRequest> {

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
    public CancelUserTaskRunReply process(CoreProcessorDAO dao, LHConfig config) {
        UserTaskRunModel userTaskRun = dao.get(userTaskRunId);
        if (userTaskRun == null) {
            return new CancelUserTaskRunReply("Provided invalid wfRunId", LHResponseCode.BAD_REQUEST_ERROR);
        }
        userTaskRun.cancel();
        return new CancelUserTaskRunReply(userTaskRun.getId().getPartitionKey().get(), LHResponseCode.OK);
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getWfRunId();
    }
}
