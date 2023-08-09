package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.User;
import io.littlehorse.common.model.wfrun.UserGroup;
import io.littlehorse.sdk.common.proto.UserTaskEventPb.UTEReassignedPb;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UTEReassigned extends LHSerializable<UTEReassignedPb> {

    private User oldUser;
    private UserGroup oldUserGroup;
    private User newUser;
    private UserGroup newUserGroup;

    @Override
    public Class<UTEReassignedPb> getProtoBaseClass() {
        return UTEReassignedPb.class;
    }

    @Override
    public UTEReassignedPb.Builder toProto() {
        UTEReassignedPb.Builder out = UTEReassignedPb.newBuilder();
        if (oldUserGroup != null) out.setOldUserGroup(oldUserGroup.toProto());
        if (newUserGroup != null) out.setNewUserGroup(newUserGroup.toProto());
        if (oldUser != null) out.setOldUser(oldUser.toProto());
        if (newUser != null) out.setNewUser(newUser.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto) {
        UTEReassignedPb p = (UTEReassignedPb) proto;
        if (p.hasNewUserGroup()) newUserGroup =
            LHSerializable.fromProto(p.getNewUserGroup(), UserGroup.class);
        if (p.hasOldUserGroup()) oldUserGroup =
            LHSerializable.fromProto(p.getOldUserGroup(), UserGroup.class);
        if (p.hasNewUser()) newUser =
            LHSerializable.fromProto(p.getNewUser(), User.class);
        if (p.hasOldUser()) oldUser =
            LHSerializable.fromProto(p.getOldUser(), User.class);
    }
}
