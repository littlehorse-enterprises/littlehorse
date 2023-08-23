package io.littlehorse.common.model.getable.core.usertaskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.User;
import lombok.Getter;

@Getter
public class UserModel extends LHSerializable<User> {

    private String id;
    private UserGroupModel userGroup;

    public UserModel() {}

    public UserModel(String id) {
        this.id = id;
    }

    public UserModel(String id, UserGroupModel userGroup) {
        this.id = id;
        this.userGroup = userGroup;
    }

    @Override
    public User.Builder toProto() {
        User.Builder builder = User.newBuilder().setId(id);
        if (userGroup != null) builder.setUserGroup(userGroup.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        User User = (User) proto;
        this.id = User.getId();
        if (User.hasUserGroup()) {
            this.userGroup = LHSerializable.fromProto(User.getUserGroup(), UserGroupModel.class);
        }
    }

    @Override
    public Class<User> getProtoBaseClass() {
        return User.class;
    }
}
