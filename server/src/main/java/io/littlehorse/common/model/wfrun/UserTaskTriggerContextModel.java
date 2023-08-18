package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskTriggerContext;

public class UserTaskTriggerContextModel extends LHSerializable<UserTaskTriggerContext> {

    private UserModel user;
    private UserGroupModel userGroup;

    public UserTaskTriggerContextModel() {}

    public UserTaskTriggerContextModel(UserModel user) {
        this.user = user;
    }

    public UserTaskTriggerContextModel(UserGroupModel userGroup) {
        this.userGroup = userGroup;
    }

    public UserTaskTriggerContextModel(UserModel user, UserGroupModel userGroup) {
        this.user = user;
        this.userGroup = userGroup;
    }

    @Override
    public UserTaskTriggerContext.Builder toProto() {
        UserTaskTriggerContext.Builder builder = UserTaskTriggerContext.newBuilder();
        if (user != null) builder.setUser(user.toProto());
        if (userGroup != null) builder.setUserGroup(userGroup.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserTaskTriggerContext taskTriggerContext = (UserTaskTriggerContext) proto;
        if (taskTriggerContext.hasUser()) {
            user = LHSerializable.fromProto(taskTriggerContext.getUser(), UserModel.class);
        }
        if (taskTriggerContext.hasUserGroup()) {
            userGroup =
                    LHSerializable.fromProto(
                            taskTriggerContext.getUserGroup(), UserGroupModel.class);
        }
    }

    @Override
    public Class<UserTaskTriggerContext> getProtoBaseClass() {
        return UserTaskTriggerContext.class;
    }
}
