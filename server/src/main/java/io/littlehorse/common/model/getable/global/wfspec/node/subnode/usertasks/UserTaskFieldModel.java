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

    public UserTaskFieldModel() {}

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

    @Override
    public String toString() {
        return "UserTaskFieldModel(name=" + this.getName() + ", type=" + this.getType() + ", description="
                + this.getDescription() + ", displayName=" + this.getDisplayName() + ", required=" + this.isRequired()
                + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserTaskFieldModel)) return false;
        final UserTaskFieldModel other = (UserTaskFieldModel) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isRequired() != other.isRequired()) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        final Object this$displayName = this.getDisplayName();
        final Object other$displayName = other.getDisplayName();
        if (this$displayName == null ? other$displayName != null : !this$displayName.equals(other$displayName))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserTaskFieldModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isRequired() ? 79 : 97);
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $displayName = this.getDisplayName();
        result = result * PRIME + ($displayName == null ? 43 : $displayName.hashCode());
        return result;
    }
}
