package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskNode.UserAssignment;
import lombok.Getter;

@Getter
public class UserAssignmentModel extends LHSerializable<UserAssignment> {

    private VariableAssignmentModel userId;
    private VariableAssignmentModel userGroup;

    @Override
    public UserAssignment.Builder toProto() {
        UserAssignment.Builder userAssignmentPbBuilder = UserAssignment.newBuilder();
        userAssignmentPbBuilder.setUserId(userId.toProto());
        if (userGroup != null) {
            userAssignmentPbBuilder.setUserGroup(userGroup.toProto());
        }
        return userAssignmentPbBuilder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserAssignment UserAssignment = (UserAssignment) proto;
        userId = VariableAssignmentModel.fromProto(UserAssignment.getUserId());
        if (UserAssignment.hasUserGroup()) {
            userGroup =
                VariableAssignmentModel.fromProto(UserAssignment.getUserGroup());
        }
    }

    @Override
    public Class<UserAssignment> getProtoBaseClass() {
        return UserAssignment.class;
    }
}
