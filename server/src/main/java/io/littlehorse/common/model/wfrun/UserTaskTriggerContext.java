package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskTriggerContextPb;

public class UserTaskTriggerContext extends LHSerializable<UserTaskTriggerContextPb> {

    private User user;
    private Group group;

    public UserTaskTriggerContext() {}

    public UserTaskTriggerContext(User user) {
        this.user = user;
    }

    public UserTaskTriggerContext(Group group) {
        this.group = group;
    }

    public UserTaskTriggerContext(User user, Group group) {
        this.user = user;
        this.group = group;
    }

    @Override
    public UserTaskTriggerContextPb.Builder toProto() {
        UserTaskTriggerContextPb.Builder builder = UserTaskTriggerContextPb.newBuilder();
        if (user != null) builder.setUser(user.toProto());
        if (group != null) builder.setGroup(group.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserTaskTriggerContextPb taskTriggerContext = (UserTaskTriggerContextPb) proto;
        if (taskTriggerContext.hasUser()) {
            user = LHSerializable.fromProto(taskTriggerContext.getUser(), User.class);
        }
        if (taskTriggerContext.hasGroup()) {
            group =
                LHSerializable.fromProto(taskTriggerContext.getGroup(), Group.class);
        }
    }

    @Override
    public Class<UserTaskTriggerContextPb> getProtoBaseClass() {
        return UserTaskTriggerContextPb.class;
    }
}
