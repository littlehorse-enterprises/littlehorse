package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserPb;
import lombok.Getter;

@Getter
public class User extends LHSerializable<UserPb> {

    private String id;
    private UserGroup userGroup;

    public User() {}

    public User(String id) {
        this.id = id;
    }

    public User(String id, UserGroup userGroup) {
        this.id = id;
        this.userGroup = userGroup;
    }

    @Override
    public UserPb.Builder toProto() {
        UserPb.Builder builder = UserPb.newBuilder().setId(id);
        if (userGroup != null) builder.setUserGroup(userGroup.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserPb userPb = (UserPb) proto;
        this.id = userPb.getId();
        if (userPb.hasUserGroup()) {
            this.userGroup =
                LHSerializable.fromProto(userPb.getUserGroup(), UserGroup.class);
        }
    }

    @Override
    public Class<UserPb> getProtoBaseClass() {
        return UserPb.class;
    }
}
