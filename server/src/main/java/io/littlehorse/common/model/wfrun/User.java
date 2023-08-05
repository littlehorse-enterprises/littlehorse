package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserPb;
import lombok.Getter;

@Getter
public class User extends LHSerializable<UserPb> {

    private String id;
    private Group group;

    public User() {}

    public User(String id) {
        this.id = id;
    }

    public User(String id, Group group) {
        this.id = id;
        this.group = group;
    }

    @Override
    public UserPb.Builder toProto() {
        UserPb.Builder builder = UserPb.newBuilder().setId(id);
        if (group != null) builder.setGroup(group.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserPb userPb = (UserPb) proto;
        this.id = userPb.getId();
        if (userPb.hasGroup()) {
            this.group = LHSerializable.fromProto(userPb.getGroup(), Group.class);
        }
    }

    @Override
    public Class<UserPb> getProtoBaseClass() {
        return UserPb.class;
    }
}
