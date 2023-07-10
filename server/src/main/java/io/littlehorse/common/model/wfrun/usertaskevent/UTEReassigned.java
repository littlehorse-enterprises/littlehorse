package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskEventPb.UTEReassignedPb;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UTEReassigned extends LHSerializable<UTEReassignedPb> {

    private String oldUserId;
    private String newUserId;
    private String oldUserGroup;
    private String newUserGroup;

    public Class<UTEReassignedPb> getProtoBaseClass() {
        return UTEReassignedPb.class;
    }

    public UTEReassignedPb.Builder toProto() {
        UTEReassignedPb.Builder out = UTEReassignedPb.newBuilder();
        if (oldUserGroup != null) out.setOldUserGroup(oldUserGroup);
        if (newUserGroup != null) out.setNewUserGroup(newUserGroup);
        if (oldUserId != null) out.setOldUserId(oldUserId);
        if (oldUserGroup != null) out.setOldUserGroup(oldUserGroup);
        return out;
    }

    public void initFrom(Message proto) {
        UTEReassignedPb p = (UTEReassignedPb) proto;
        if (p.hasNewUserGroup()) newUserGroup = p.getNewUserGroup();
        if (p.hasOldUserGroup()) oldUserGroup = p.getOldUserGroup();
        if (p.hasNewUserId()) newUserId = p.getNewUserId();
        if (p.hasOldUserId()) oldUserGroup = p.getOldUserId();
    }
}
