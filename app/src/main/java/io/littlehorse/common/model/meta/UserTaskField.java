package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.jlib.common.proto.UserTaskFieldPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;

public class UserTaskField extends LHSerializable<UserTaskFieldPb> {

    public String name;
    public VariableTypePb type;
    public VariableValue defaultVal;
    public boolean required;
    public String description;

    public Class<UserTaskFieldPb> getProtoBaseClass() {
        return UserTaskFieldPb.class;
    }

    public void initFrom(Message proto) {
        UserTaskFieldPb p = (UserTaskFieldPb) proto;
        name = p.getName();
        type = p.getType();
        required = p.getRequired();
        if (p.hasDescription()) description = p.getDescription();
        if (p.hasDefaultVal()) {
            defaultVal = VariableValue.fromProto(p.getDefaultVal());
        }
    }

    public UserTaskFieldPb.Builder toProto() {
        UserTaskFieldPb.Builder out = UserTaskFieldPb
            .newBuilder()
            .setName(name)
            .setType(type)
            .setRequired(required);

        if (description != null) out.setDescription(description);
        if (defaultVal != null) out.setDefaultVal(defaultVal.toProto().build());

        return out;
    }
}
