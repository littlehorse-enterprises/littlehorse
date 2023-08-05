package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.GroupPb;
import lombok.Getter;

@Getter
public class Group extends LHSerializable<GroupPb> {

    private String id;

    public Group() {}

    public Group(String id) {
        this.id = id;
    }

    @Override
    public GroupPb.Builder toProto() {
        return GroupPb.newBuilder().setId(id);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        GroupPb groupPb = (GroupPb) proto;
        this.id = groupPb.getId();
    }

    @Override
    public Class<GroupPb> getProtoBaseClass() {
        return GroupPb.class;
    }
}
