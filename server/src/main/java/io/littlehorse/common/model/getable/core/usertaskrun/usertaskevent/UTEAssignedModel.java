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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UTEAssignedModel)) return false;
        final UTEAssignedModel other = (UTEAssignedModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$oldUserId = this.getOldUserId();
        final Object other$oldUserId = other.getOldUserId();
        if (this$oldUserId == null ? other$oldUserId != null : !this$oldUserId.equals(other$oldUserId)) return false;
        final Object this$newUserId = this.getNewUserId();
        final Object other$newUserId = other.getNewUserId();
        if (this$newUserId == null ? other$newUserId != null : !this$newUserId.equals(other$newUserId)) return false;
        final Object this$oldUserGroup = this.getOldUserGroup();
        final Object other$oldUserGroup = other.getOldUserGroup();
        if (this$oldUserGroup == null ? other$oldUserGroup != null : !this$oldUserGroup.equals(other$oldUserGroup))
            return false;
        final Object this$newUserGroup = this.getNewUserGroup();
        final Object other$newUserGroup = other.getNewUserGroup();
        if (this$newUserGroup == null ? other$newUserGroup != null : !this$newUserGroup.equals(other$newUserGroup))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UTEAssignedModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $oldUserId = this.getOldUserId();
        result = result * PRIME + ($oldUserId == null ? 43 : $oldUserId.hashCode());
        final Object $newUserId = this.getNewUserId();
        result = result * PRIME + ($newUserId == null ? 43 : $newUserId.hashCode());
        final Object $oldUserGroup = this.getOldUserGroup();
        result = result * PRIME + ($oldUserGroup == null ? 43 : $oldUserGroup.hashCode());
        final Object $newUserGroup = this.getNewUserGroup();
        result = result * PRIME + ($newUserGroup == null ? 43 : $newUserGroup.hashCode());
        return result;
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

    @Override
    public String toString() {
        return "UTEAssignedModel(oldUserId=" + this.getOldUserId() + ", newUserId=" + this.getNewUserId()
                + ", oldUserGroup=" + this.getOldUserGroup() + ", newUserGroup=" + this.getNewUserGroup() + ")";
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
