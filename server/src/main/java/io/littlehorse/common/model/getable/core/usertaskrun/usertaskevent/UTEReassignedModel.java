package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.usertaskrun.UserGroupModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserModel;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTEReassigned;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UTEReassignedModel extends LHSerializable<UTEReassigned> {

    private UserModel oldUser;
    private UserGroupModel oldUserGroup;
    private UserModel newUser;
    private UserGroupModel newUserGroup;

    @Override
    public Class<UTEReassigned> getProtoBaseClass() {
        return UTEReassigned.class;
    }

    @Override
    public UTEReassigned.Builder toProto() {
        UTEReassigned.Builder out = UTEReassigned.newBuilder();
        if (oldUserGroup != null)
            out.setOldUserGroup(oldUserGroup.toProto());
        if (newUserGroup != null)
            out.setNewUserGroup(newUserGroup.toProto());
        if (oldUser != null)
            out.setOldUser(oldUser.toProto());
        if (newUser != null)
            out.setNewUser(newUser.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto) {
        UTEReassigned p = (UTEReassigned) proto;
        if (p.hasNewUserGroup())
            newUserGroup = LHSerializable.fromProto(p.getNewUserGroup(), UserGroupModel.class);
        if (p.hasOldUserGroup())
            oldUserGroup = LHSerializable.fromProto(p.getOldUserGroup(), UserGroupModel.class);
        if (p.hasNewUser())
            newUser = LHSerializable.fromProto(p.getNewUser(), UserModel.class);
        if (p.hasOldUser())
            oldUser = LHSerializable.fromProto(p.getOldUser(), UserModel.class);
    }
}
