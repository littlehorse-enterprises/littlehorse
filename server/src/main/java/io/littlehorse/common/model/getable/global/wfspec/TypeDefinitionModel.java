package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
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

    @Getter
    private DefinedTypeCase definedType;

    private VariableType primitiveType;
    private InlineStructDefModel inlineStructDef;
    private StructDefIdModel structRef;

    public TypeDefinitionModel() {}

    public TypeDefinitionModel(VariableType type) {
        this.primitiveType = type;
    }

    @Override
    public Class<TypeDefinition> getProtoBaseClass() {
        return TypeDefinition.class;
    }

    @Override
    public TypeDefinition.Builder toProto() {
        TypeDefinition.Builder out = TypeDefinition.newBuilder().setMasked(masked);

        if (this.primitiveType != null) {
            out.setPrimitiveType(primitiveType);
        } else if (this.inlineStructDef != null) {
            out.setInlineStructDef(inlineStructDef.toProto());
        } else if (this.structRef != null) {
            out.setStructDefId(structRef.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TypeDefinition p = (TypeDefinition) proto;
        this.masked = p.getMasked();

        this.definedType = p.getDefinedTypeCase();

        switch (definedType) {
            case PRIMITIVE_TYPE:
                this.primitiveType = p.getPrimitiveType();
                break;
            case INLINE_STRUCT_DEF:
                this.inlineStructDef = InlineStructDefModel.fromProto(p.getInlineStructDef(), ctx);
                break;
            case STRUCT_DEF_ID:
                this.structRef = StructDefIdModel.fromProto(p.getStructDefId(), ctx);
                break;
            case DEFINEDTYPE_NOT_SET:
            default:
                // TODO: Throw error here?
                break;
        }
    }

    public boolean isPrimitive() {
        if (primitiveType == null) {
            return false;
        }

        switch (primitiveType) {
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
        // if (value.getType() == Variab)
        if (primitiveType != null) {
            return value.getType() == primitiveType;
        } else if (inlineStructDef != null) {
            // value.getStructVal();
        }

        return value.getType() == primitiveType;
    }
}
