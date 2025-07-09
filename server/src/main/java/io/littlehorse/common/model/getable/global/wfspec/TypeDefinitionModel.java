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
    private VariableType primitiveType;

    public TypeDefinitionModel() {}

    public TypeDefinitionModel(VariableType primitiveType) {
        this.primitiveType = primitiveType;
    }

    @Override
    public Class<TypeDefinition> getProtoBaseClass() {
        return TypeDefinition.class;
    }

    @Override
    public TypeDefinition.Builder toProto() {
        TypeDefinition.Builder out =
                TypeDefinition.newBuilder().setMasked(masked).setPrimitiveType(primitiveType);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TypeDefinition p = (TypeDefinition) proto;
        this.masked = p.getMasked();
        this.primitiveType = p.getPrimitiveType();
    }

    public boolean isPrimitive() {
        // TODO: Extend this when adding Struct and StructDef.
        switch (primitiveType) {
            case INT:
            case BOOL:
            case DOUBLE:
            case STR:
                return true;
            case JSON_OBJ:
            case JSON_ARR:
            case BYTES:
            case WF_RUN_ID:
            case UNRECOGNIZED:
        }
        return false;
    }

    public static TypeDefinitionModel fromProto(TypeDefinition proto, ExecutionContext context) {
        TypeDefinitionModel out = new TypeDefinitionModel();
        out.initFrom(proto, context);
        return out;
    }

    public boolean isJson() {
        return primitiveType == VariableType.JSON_ARR || primitiveType == VariableType.JSON_OBJ;
    }

    /**
     * Returns true if the VariableValueModel matches this type.
     */
    public boolean isCompatibleWith(VariableValueModel value) {
        // TODO: Extend this when we add StructDef's and Structs.
        return value.getType() == primitiveType;
    }
}
