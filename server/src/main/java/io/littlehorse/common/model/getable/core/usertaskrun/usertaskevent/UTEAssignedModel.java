package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTEAssigned;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class UTEAssignedModel extends LHSerializable<UTEAssigned> {

    private String oldUserId;
    private String newUserId;
    private String oldUserGroup;
    private String newUserGroup;

    @Override
    public Class<UTEAssigned> getProtoBaseClass() {
        return UTEAssigned.class;
    }

    @Override
    public UTEAssigned.Builder toProto() {
        UTEAssigned.Builder out = UTEAssigned.newBuilder();
        if (oldUserGroup != null) out.setOldUserGroup(oldUserGroup);
        if (newUserGroup != null) out.setNewUserGroup(newUserGroup);
        if (oldUserId != null) out.setOldUserId(oldUserId);
        if (newUserId != null) out.setNewUserId(newUserId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UTEAssigned p = (UTEAssigned) proto;
        if (p.hasNewUserGroup()) newUserGroup = p.getNewUserGroup();
        if (p.hasOldUserGroup()) oldUserGroup = p.getOldUserGroup();
        if (p.hasNewUserId()) newUserId = p.getNewUserId();
        if (p.hasOldUserId()) oldUserId = p.getOldUserId();
    }

    public String getOldUserId() {
        return this.oldUserId;
    }

    public String getNewUserId() {
        return this.newUserId;
    }

    public String getOldUserGroup() {
        return this.oldUserGroup;
    }

    public String getNewUserGroup() {
        return this.newUserGroup;
    }

    public void setOldUserId(final String oldUserId) {
        this.oldUserId = oldUserId;
    }

    public void setNewUserId(final String newUserId) {
        this.newUserId = newUserId;
    }

    public void setOldUserGroup(final String oldUserGroup) {
        this.oldUserGroup = oldUserGroup;
    }

    public void setNewUserGroup(final String newUserGroup) {
        this.newUserGroup = newUserGroup;
    }

    public UTEAssignedModel() {}

    public UTEAssignedModel(
            final String oldUserId, final String newUserId, final String oldUserGroup, final String newUserGroup) {
        this.oldUserId = oldUserId;
        this.newUserId = newUserId;
        this.oldUserGroup = oldUserGroup;
        this.newUserGroup = newUserGroup;
    }
}
