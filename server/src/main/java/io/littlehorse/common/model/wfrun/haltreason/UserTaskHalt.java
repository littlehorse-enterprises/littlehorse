package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskHaltPb;
import lombok.Getter;

@Getter
public class UserTaskHalt
    extends LHSerializable<UserTaskHaltPb>
    implements SubHaltReason {

    private UserTaskRunId userTaskRunId;

    public UserTaskHalt() {}

    public UserTaskHalt(UserTaskRunId userTaskRunId) {
        this.userTaskRunId = userTaskRunId;
    }

    @Override
    public boolean isResolved(WfRun wfRun) {
        return false;
    }

    @Override
    public UserTaskHaltPb.Builder toProto() {
        return UserTaskHaltPb.newBuilder().setUserTaskRunId(userTaskRunId.toProto());
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserTaskHaltPb userTaskHaltPb = (UserTaskHaltPb) proto;
        userTaskRunId =
            LHSerializable.fromProto(
                userTaskHaltPb.getUserTaskRunId(),
                UserTaskRunId.class
            );
    }

    @Override
    public Class<UserTaskHaltPb> getProtoBaseClass() {
        return UserTaskHaltPb.class;
    }
}
