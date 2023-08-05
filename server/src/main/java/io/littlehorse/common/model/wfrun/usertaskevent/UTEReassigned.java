package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.Group;
import io.littlehorse.common.model.wfrun.User;
import io.littlehorse.sdk.common.proto.UserTaskEventPb.UTEReassignedPb;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UTEReassigned extends LHSerializable<UTEReassignedPb> {

    private User oldUser;
    private Group oldGroup;
    private User newUser;
    private Group newGroup;

    @Override
    public Class<UTEReassignedPb> getProtoBaseClass() {
        return UTEReassignedPb.class;
    }

    @Override
    public UTEReassignedPb.Builder toProto() {
        UTEReassignedPb.Builder out = UTEReassignedPb.newBuilder();
        if (oldGroup != null) out.setOldGroup(oldGroup.toProto());
        if (newGroup != null) out.setNewGroup(newGroup.toProto());
        if (oldUser != null) out.setOldUser(oldUser.toProto());
        if (newUser != null) out.setNewUser(newUser.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto) {
        UTEReassignedPb p = (UTEReassignedPb) proto;
        if (p.hasNewGroup()) newGroup =
            LHSerializable.fromProto(p.getNewGroup(), Group.class);
        if (p.hasOldGroup()) oldGroup =
            LHSerializable.fromProto(p.getOldGroup(), Group.class);
        if (p.hasNewUser()) newUser =
            LHSerializable.fromProto(p.getNewUser(), User.class);
        if (p.hasOldUser()) oldUser =
            LHSerializable.fromProto(p.getOldUser(), User.class);
    }
}
