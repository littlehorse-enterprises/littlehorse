package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.CancelUserTaskRunReply;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class CancelUserTaskRun extends SubCommand<CancelUserTaskRunPb> {

    private UserTaskRunIdModel userTaskRunId;

    @Override
    public CancelUserTaskRunPb.Builder toProto() {
        return CancelUserTaskRunPb
            .newBuilder()
            .setUserTaskRunId(userTaskRunId.toProto());
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        CancelUserTaskRunPb cancelUserTaskRunPb = (CancelUserTaskRunPb) proto;
        userTaskRunId =
            LHSerializable.fromProto(
                cancelUserTaskRunPb.getUserTaskRunId(),
                UserTaskRunIdModel.class
            );
    }

    @Override
    public Class<CancelUserTaskRunPb> getProtoBaseClass() {
        return CancelUserTaskRunPb.class;
    }

    @Override
    public CancelUserTaskRunReply process(LHDAO dao, LHConfig config) {
        UserTaskRunModel userTaskRun = dao.getUserTaskRun(userTaskRunId);
        if (userTaskRun == null) {
            return new CancelUserTaskRunReply(
                "Provided invalid wfRunId",
                LHResponseCode.BAD_REQUEST_ERROR
            );
        }
        userTaskRun.cancel();
        return new CancelUserTaskRunReply(
            userTaskRun.getId().getPartitionKey(),
            LHResponseCode.OK
        );
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
