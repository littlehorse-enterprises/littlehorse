package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.VariableDefPb;
import io.littlehorse.common.proto.VariableDefPbOrBuilder;
import io.littlehorse.common.proto.VariableTypePb;

public class VariableDef extends LHSerializable<VariableDefPb> {

    public VariableTypePb type;
    public VariableValue defaultValue;
    public boolean required;

    @JsonIgnore
    public String name;

    @JsonIgnore
    public ThreadSpec threadSpec;

    public Class<VariableDefPb> getProtoBaseClass() {
        return VariableDefPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        VariableDefPbOrBuilder p = (VariableDefPbOrBuilder) proto;
        if (p.hasDefaultVal()) defaultValue =
            VariableValue.fromProto(p.getDefaultVal());
        type = p.getType();
        required = p.getRequired();
    }

    public VariableDefPb.Builder toProto() {
        VariableDefPb.Builder out = VariableDefPb
            .newBuilder()
            .setType(type)
            .setRequired(required);

        if (defaultValue != null) {
            out.setDefaultVal(defaultValue.toProto());
        }

        return out;
    }
}
