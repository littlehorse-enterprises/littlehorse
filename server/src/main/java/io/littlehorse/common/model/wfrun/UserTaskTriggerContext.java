package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskTriggerContextPb;

public class UserTaskTriggerContext extends LHSerializable<UserTaskTriggerContextPb> {

    private User user;
    private Group userGroup;

    public UserTaskTriggerContext() {}

    public UserTaskTriggerContext(User user) {
        this.user = user;
    }

    public UserTaskTriggerContext(Group userGroup) {
        this.userGroup = userGroup;
    }

    public UserTaskTriggerContext(User user, Group userGroup) {
        this.user = user;
        this.userGroup = userGroup;
    }

    @Override
    public UserTaskTriggerContextPb.Builder toProto() {
        UserTaskTriggerContextPb.Builder builder = UserTaskTriggerContextPb.newBuilder();
        if (user != null) builder.setUser(user.toProto());
        if (userGroup != null) builder.setUserGroup(userGroup.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserTaskTriggerContextPb taskTriggerContext = (UserTaskTriggerContextPb) proto;
        if (taskTriggerContext.hasUser()) {
            user = LHSerializable.fromProto(taskTriggerContext.getUser(), User.class);
        }
        if (taskTriggerContext.hasUserGroup()) {
            userGroup =
                LHSerializable.fromProto(
                    taskTriggerContext.getUserGroup(),
                    Group.class
                );
        }
    }

    @Override
    public Class<UserTaskTriggerContextPb> getProtoBaseClass() {
        return UserTaskTriggerContextPb.class;
    }
}
