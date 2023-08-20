package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.sdk.common.proto.VariableType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserTaskFieldModel extends LHSerializable<UserTaskField> {

    private String name;
    private VariableType type;
    private String description;
    private String displayName;
    private boolean required;

    public Class<UserTaskField> getProtoBaseClass() {
        return UserTaskField.class;
    }

    public void initFrom(Message proto) {
        UserTaskField p = (UserTaskField) proto;
        name = p.getName();
        type = p.getType();
        displayName = p.getDisplayName();
        required = p.getRequired();

        if (p.hasDescription())
            description = p.getDescription();
    }

    public UserTaskField.Builder toProto() {
        UserTaskField.Builder out = UserTaskField.newBuilder()
                .setName(name)
                .setType(type)
                .setRequired(required)
                .setDisplayName(displayName);

        if (description != null)
            out.setDescription(description);
        return out;
    }
}
