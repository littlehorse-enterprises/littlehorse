package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableDefModel extends LHSerializable<VariableDef> {

    private VariableType type;
    private String name;
    private VariableValueModel defaultValue;

    public Class<VariableDef> getProtoBaseClass() {
        return VariableDef.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VariableDef p = (VariableDef) proto;
        type = p.getType();
        name = p.getName();
        if (p.hasDefaultValue()) {
            defaultValue = VariableValueModel.fromProto(p.getDefaultValue(), context);
        }
    }

    public VariableDef.Builder toProto() {
        VariableDef.Builder out = VariableDef.newBuilder().setType(type).setName(name);

        if (defaultValue != null) out.setDefaultValue(defaultValue.toProto());
        return out;
    }

    public static VariableDefModel fromProto(VariableDef proto, ExecutionContext context) {
        VariableDefModel o = new VariableDefModel();
        o.initFrom(proto, context);
        return o;
    }

    public boolean isJson() {
        return type == VariableType.JSON_ARR || type == VariableType.JSON_OBJ;
    }
}
