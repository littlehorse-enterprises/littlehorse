package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskNodePb.UserAssignmentPb;
import lombok.Getter;

@Getter
public class UserAssignment extends LHSerializable<UserAssignmentPb> {

    private VariableAssignmentModel userId;
    private VariableAssignmentModel userGroup;

    @Override
    public UserAssignmentPb.Builder toProto() {
        UserAssignmentPb.Builder userAssignmentPbBuilder = UserAssignmentPb.newBuilder();
        userAssignmentPbBuilder.setUserId(userId.toProto());
        if (userGroup != null) {
            userAssignmentPbBuilder.setUserGroup(userGroup.toProto());
        }
        return userAssignmentPbBuilder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserAssignmentPb userAssignmentPb = (UserAssignmentPb) proto;
        userId = VariableAssignmentModel.fromProto(userAssignmentPb.getUserId());
        if (userAssignmentPb.hasUserGroup()) {
            userGroup = VariableAssignmentModel.fromProto(userAssignmentPb.getUserGroup());
        }
    }

    @Override
    public Class<UserAssignmentPb> getProtoBaseClass() {
        return UserAssignmentPb.class;
    }
}
