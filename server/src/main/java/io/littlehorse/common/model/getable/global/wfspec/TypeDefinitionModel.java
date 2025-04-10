package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TypeDefinitionModel extends LHSerializable<TypeDefinition> {

    private boolean masked;
    private VariableType type;

    public TypeDefinitionModel() {}

    public TypeDefinitionModel(VariableType type) {
        this.type = type;
    }

    @Override
    public Class<TypeDefinition> getProtoBaseClass() {
        return TypeDefinition.class;
    }

    @Override
    public TypeDefinition.Builder toProto() {
        TypeDefinition.Builder out =
                TypeDefinition.newBuilder().setMasked(masked).setType(type);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TypeDefinition p = (TypeDefinition) proto;
        this.masked = p.getMasked();
        this.type = p.getType();
    }

    public boolean isPrimitive() {
        // TODO: Extend this when adding Struct and StructDef.
        switch (type) {
            case INT:
            case BOOL:
            case DOUBLE:
            case STR:
                return true;
            case JSON_OBJ:
            case JSON_ARR:
            case BYTES:
            case UNRECOGNIZED:
        }
        return false;
    }

    public boolean isJson() {
        return type == VariableType.JSON_ARR || type == VariableType.JSON_OBJ;
    }

    /**
     * Returns true if the VariableValueModel matches this type.
     */
    public boolean isCompatibleWith(VariableValueModel value) {
        // TODO: Extend this when we add StructDef's and Structs.
        return value.getType() == type;
    }
}
