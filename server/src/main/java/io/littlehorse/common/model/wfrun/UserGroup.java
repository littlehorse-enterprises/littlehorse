package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserGroupPb;
import lombok.Getter;

@Getter
public class UserGroup extends LHSerializable<UserGroupPb> {

    private String id;

    public UserGroup() {}

    public UserGroup(String id) {
        this.id = id;
    }

    @Override
    public UserGroupPb.Builder toProto() {
        return UserGroupPb.newBuilder().setId(id);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UserGroupPb groupPb = (UserGroupPb) proto;
        this.id = groupPb.getId();
    }

    @Override
    public Class<UserGroupPb> getProtoBaseClass() {
        return UserGroupPb.class;
    }
}
