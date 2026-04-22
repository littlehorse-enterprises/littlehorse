package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class UserTaskFieldModel extends LHSerializable<UserTaskField> {
    private String name;
    private VariableType type;
    private String description;
    private String displayName;
    private boolean required;

    public Class<UserTaskField> getProtoBaseClass() {
        return UserTaskField.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskField p = (UserTaskField) proto;
        name = p.getName();
        type = p.getType();
        displayName = p.getDisplayName();
        required = p.getRequired();
        if (p.hasDescription()) description = p.getDescription();
    }

    public UserTaskField.Builder toProto() {
        UserTaskField.Builder out = UserTaskField.newBuilder()
                .setName(name)
                .setType(type)
                .setRequired(required)
                .setDisplayName(displayName);
        if (description != null) out.setDescription(description);
        return out;
    }

    public String getName() {
        return this.name;
    }

    public VariableType getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setType(final VariableType type) {
        this.type = type;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }
}
