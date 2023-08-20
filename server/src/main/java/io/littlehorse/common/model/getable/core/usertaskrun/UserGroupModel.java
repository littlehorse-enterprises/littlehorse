package io.littlehorse.common.model.getable.core.usertaskrun;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserGroup;
import lombok.Getter;

@Getter
public class UserGroupModel extends LHSerializable<UserGroup> {

    private String id;

    public UserGroupModel() {
    }

    public UserGroupModel(String id) {
        this.id = id;
    }

    @Override
    public UserGroup.Builder toProto() {
        return UserGroup.newBuilder().setId(id);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserGroup groupPb = (UserGroup) proto;
        this.id = groupPb.getId();
    }

    @Override
    public Class<UserGroup> getProtoBaseClass() {
        return UserGroup.class;
    }
}
