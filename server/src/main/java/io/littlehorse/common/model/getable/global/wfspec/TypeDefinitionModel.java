package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.BoolReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.BytesReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.DoubleReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.IntReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.JsonArrReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.JsonObjReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.LHTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.StrReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.WfRunIdReturnTypeStrategy;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TypeDefinitionModel extends LHSerializable<TypeDefinition> {

    private boolean masked;

    private DefinedTypeCase definedTypeCase;

    private VariableType primitiveType;
    private StructDefIdModel structDefId;

    public TypeDefinitionModel() {}

    public TypeDefinitionModel(VariableType primitiveType) {
        this.definedTypeCase = DefinedTypeCase.PRIMITIVE_TYPE;
        this.primitiveType = primitiveType;
        this.masked = false;
    }

    public TypeDefinitionModel(VariableType type, boolean masked) {
        this.definedTypeCase = DefinedTypeCase.PRIMITIVE_TYPE;
        this.primitiveType = type;
        this.masked = masked;
    }

    @Override
    public Class<TypeDefinition> getProtoBaseClass() {
        return TypeDefinition.class;
    }

    @Override
    public TypeDefinition.Builder toProto() {
        TypeDefinition.Builder out =
                TypeDefinition.newBuilder().setMasked(masked);

        if (primitiveType != null) {
            out.setPrimitiveType(primitiveType);
        } else if (structDefId != null) {
            out.setStructDefId(structDefId.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TypeDefinition p = (TypeDefinition) proto;
        this.masked = p.getMasked();
        this.definedTypeCase = p.getDefinedTypeCase();

        if (p.hasPrimitiveType()) {
            this.primitiveType = p.getPrimitiveType();
        } else if (p.hasStructDefId()) {
            this.structDefId = StructDefIdModel.fromProto(p.getStructDefId(), ctx);
        }
    }

    public Optional<TypeDefinitionModel> resolveTypeAfterMutationWith(
            VariableMutationType operation, TypeDefinitionModel rhs, ReadOnlyMetadataManager manager)
            throws InvalidExpressionException {
        return getTypeStrategy().resolveOperation(manager, operation, rhs.getTypeStrategy());
    }

    public LHTypeStrategy getTypeStrategy() {
        // TODO: Support StructDefs
        switch (primitiveType) {
            case INT:
                return new IntReturnTypeStrategy();
            case DOUBLE:
                return new DoubleReturnTypeStrategy();
            case STR:
                return new StrReturnTypeStrategy();
            case BOOL:
                return new BoolReturnTypeStrategy();
            case WF_RUN_ID:
                return new WfRunIdReturnTypeStrategy();
            case BYTES:
                return new BytesReturnTypeStrategy();
            case JSON_ARR:
                return new JsonArrReturnTypeStrategy();
            case JSON_OBJ:
                return new JsonObjReturnTypeStrategy();
            case UNRECOGNIZED:
        }
        throw new IllegalStateException();
    }

    /**
     * Being primitive means that a variable can be used as a leaf in a json tree, and that its value
     * can be serialized to a string easily.
     */
    public boolean isPrimitive() {
        if (this.primitiveType == null) return false;

        // TODO: Extend this when adding Struct and StructDef.
        switch (primitiveType) {
            case INT:
            case BOOL:
            case DOUBLE:
            case WF_RUN_ID:
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
        return value.getType() == primitiveType;
    }

    /**
     * Returns true if the other type is compatible with this type. Note that it requires
     * exact match for now. In the future we'll support casting.
     */
    public boolean isCompatibleWith(TypeDefinitionModel other) {
        if (this.getDefinedTypeCase() != other.getDefinedTypeCase()) {
            return false;
        }

        switch (this.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                if (primitiveType == VariableType.INT || primitiveType == VariableType.DOUBLE) {
                    return other.getPrimitiveType() == VariableType.INT || other.getPrimitiveType() == VariableType.DOUBLE;
                }
                return this.getPrimitiveType().equals(other.getPrimitiveType());
            case STRUCT_DEF_ID:
                return this.getStructDefId().equals(other.getStructDefId());
            case DEFINEDTYPE_NOT_SET:
            case INLINE_ARRAY_DEF:
            case INLINE_STRUCT_DEF:
            default:
                break;

        }

        return false;
    }

    @Override
    public String toString() {
        String result = "";

        switch (this.definedTypeCase) {
            case PRIMITIVE_TYPE:
                result = primitiveType.toString();
                break;
            case STRUCT_DEF_ID:
                result = String.format("<%s,%d>", structDefId.getName(), structDefId.getVersion());
                break;
            case DEFINEDTYPE_NOT_SET:
            case INLINE_ARRAY_DEF:
            case INLINE_STRUCT_DEF:
            default:
                break;
        }
        if (masked) result += " MASKED";
        return result;
    }
}
