package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.proto.UserTaskFieldPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserTaskField extends LHSerializable<UserTaskFieldPb> {

    private String name;
    private VariableTypePb type;
    private String description;
    private String displayName;

    public Class<UserTaskFieldPb> getProtoBaseClass() {
        return UserTaskFieldPb.class;
    }

    public void initFrom(Message proto) {
        UserTaskFieldPb p = (UserTaskFieldPb) proto;
        name = p.getName();
        type = p.getType();
        displayName = p.getDisplayName();

        if (p.hasDescription()) description = p.getDescription();
    }

    public UserTaskFieldPb.Builder toProto() {
        UserTaskFieldPb.Builder out = UserTaskFieldPb
            .newBuilder()
            .setName(name)
            .setType(type)
            .setDisplayName(displayName);

        if (description != null) out.setDescription(description);
        return out;
    }
}
