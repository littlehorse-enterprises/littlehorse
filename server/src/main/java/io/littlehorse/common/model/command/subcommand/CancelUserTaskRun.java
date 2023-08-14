package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.CancelUserTaskRunReply;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.ThreadHaltReason;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.haltreason.UserTaskHalt;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.ThreadHaltReasonPb;

public class CancelUserTaskRun extends SubCommand<CancelUserTaskRunPb> {

    private UserTaskRunId userTaskRunId;

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
                UserTaskRunId.class
            );
    }

    @Override
    public Class<CancelUserTaskRunPb> getProtoBaseClass() {
        return CancelUserTaskRunPb.class;
    }

    @Override
    public CancelUserTaskRunReply process(LHDAO dao, LHConfig config) {
        UserTaskRun userTaskRun = dao.getUserTaskRun(userTaskRunId);
        if (userTaskRun == null) {
            return new CancelUserTaskRunReply(
                "Provided invalid wfRunId",
                LHResponseCodePb.BAD_REQUEST_ERROR
            );
        }
        userTaskRun.cancel();
        ThreadRun threadRun = userTaskRun.getNodeRun().getThreadRun();
        ThreadHaltReason haltReason = new ThreadHaltReason();
        haltReason.type = ThreadHaltReasonPb.ReasonCase.USER_TASK_CANCELLED;
        haltReason.setUserTaskHalt(new UserTaskHalt(userTaskRun.getId()));
        threadRun.getWfRun().stop(threadRun, haltReason);
        return new CancelUserTaskRunReply(
            userTaskRun.getId().getPartitionKey(),
            LHResponseCodePb.OK
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
