package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTEAssigned;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
