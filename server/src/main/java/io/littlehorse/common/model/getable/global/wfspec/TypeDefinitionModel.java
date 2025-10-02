package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.ArrayReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.BoolReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.BytesReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.DoubleReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.IntReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.JsonArrReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.JsonObjReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.LHTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.NullReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.StrReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.StructReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.TimestampReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.WfRunIdReturnTypeStrategy;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.util.TypeCastingUtils;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Objects;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class TypeDefinitionModel extends LHSerializable<TypeDefinition> {

    private boolean masked;

    private DefinedTypeCase definedTypeCase;

    private VariableType primitiveType;
    private StructDefIdModel structDefId;
    private InlineArrayDefModel inlineArrayDef;

    public TypeDefinitionModel() {
        this.definedTypeCase = DefinedTypeCase.DEFINEDTYPE_NOT_SET;
    }

    public TypeDefinitionModel(StructDefIdModel structDefId) {
        this.definedTypeCase = DefinedTypeCase.STRUCT_DEF_ID;
        this.structDefId = Objects.requireNonNull(structDefId);
        this.masked = false;
    }

    public TypeDefinitionModel(StructDefIdModel structDefId, boolean masked) {
        this.definedTypeCase = DefinedTypeCase.STRUCT_DEF_ID;
        this.structDefId = Objects.requireNonNull(structDefId);
        this.masked = masked;
    }

    public TypeDefinitionModel(VariableType primitiveType) {
        this.definedTypeCase = DefinedTypeCase.PRIMITIVE_TYPE;
        this.primitiveType = Objects.requireNonNull(primitiveType);
        this.masked = false;
    }

    public TypeDefinitionModel(VariableType type, boolean masked) {
        this.definedTypeCase = DefinedTypeCase.PRIMITIVE_TYPE;
        this.primitiveType = Objects.requireNonNull(type);
        this.masked = masked;
    }

    public TypeDefinitionModel(InlineArrayDefModel inlineArrayDef) {
        this.definedTypeCase = DefinedTypeCase.INLINE_ARRAY_DEF;
        this.inlineArrayDef = Objects.requireNonNull(inlineArrayDef);
        this.masked = false;
    }

    public TypeDefinitionModel(InlineArrayDefModel inlineArrayDef, boolean masked) {
        this.definedTypeCase = DefinedTypeCase.INLINE_ARRAY_DEF;
        this.inlineArrayDef = Objects.requireNonNull(inlineArrayDef);
        this.masked = masked;
    }

    @Override
    public Class<TypeDefinition> getProtoBaseClass() {
        return TypeDefinition.class;
    }

    @Override
    public TypeDefinition.Builder toProto() {
        TypeDefinition.Builder out = TypeDefinition.newBuilder().setMasked(masked);

        switch (definedTypeCase) {
            case PRIMITIVE_TYPE:
                out.setPrimitiveType(primitiveType);
                break;
            case STRUCT_DEF_ID:
                out.setStructDefId(structDefId.toProto());
                break;
            case INLINE_ARRAY_DEF:
                out.setInlineArrayDef(inlineArrayDef.toProto());
                break;
            case DEFINEDTYPE_NOT_SET:
            default:
                break;
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TypeDefinition p = (TypeDefinition) proto;
        this.masked = p.getMasked();
        this.definedTypeCase = p.getDefinedTypeCase();

        switch (definedTypeCase) {
            case PRIMITIVE_TYPE:
                this.primitiveType = p.getPrimitiveType();
                break;
            case STRUCT_DEF_ID:
                this.structDefId = StructDefIdModel.fromProto(p.getStructDefId(), ctx);
                break;
            case INLINE_ARRAY_DEF:
                this.inlineArrayDef = InlineArrayDefModel.fromProto(p.getInlineArrayDef(), ctx);
                break;
            case DEFINEDTYPE_NOT_SET:
            default:
                break;
        }
    }

    public boolean isNull() {
        return this.definedTypeCase == DefinedTypeCase.DEFINEDTYPE_NOT_SET;
    }

    public Optional<TypeDefinitionModel> resolveTypeAfterMutationWith(
            VariableMutationType operation, TypeDefinitionModel rhs, ReadOnlyMetadataManager manager)
            throws InvalidExpressionException {
        return getTypeStrategy().resolveOperation(manager, operation, rhs.getTypeStrategy());
    }

    public LHTypeStrategy getTypeStrategy() {
        if (this.isNull()) {
            return new NullReturnTypeStrategy();
        }

        switch (this.definedTypeCase) {
            case PRIMITIVE_TYPE:
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
                    case TIMESTAMP:
                        return new TimestampReturnTypeStrategy();
                    case UNRECOGNIZED:
                }
                break;
            case STRUCT_DEF_ID:
                return new StructReturnTypeStrategy(this.structDefId);
            case INLINE_ARRAY_DEF:
                return new ArrayReturnTypeStrategy(this.inlineArrayDef);
            default:
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
            case TIMESTAMP:
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
        if (definedTypeCase != DefinedTypeCase.PRIMITIVE_TYPE) return false;
        return primitiveType == VariableType.JSON_ARR || primitiveType == VariableType.JSON_OBJ;
    }

    /**
     * Returns true if the VariableValueModel matches this type.
     */
    public boolean isCompatibleWith(VariableValueModel value, ReadOnlyMetadataManager readOnlyMetadataManager) {
        if (value.getValueType() == ValueCase.STRUCT) {
            value.getStruct().validateAgainstStructDefId(readOnlyMetadataManager);
        }

        TypeDefinitionModel other = value.getTypeDefinition();

        if (this.getDefinedTypeCase() != other.getDefinedTypeCase()) {
            return false;
        }

        switch (this.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                return TypeCastingUtils.canBeType(other.getPrimitiveType(), this.primitiveType);
            case STRUCT_DEF_ID:
                return this.structDefId.equals(other.getStructDefId());
            case INLINE_ARRAY_DEF:
                return this.inlineArrayDef.equals(other.getInlineArrayDef());
            case DEFINEDTYPE_NOT_SET:
            default:
                break;
        }

        return false;
    }

    /**
     * Returns true if this type can be assigned from the other type, without casting.
     */
    public boolean isCompatibleWith(TypeDefinitionModel other) {
        if (this.isNull() || other.isNull()) {
            return true;
        }

        if (this.getDefinedTypeCase() != other.getDefinedTypeCase()) {
            return false;
        }

        switch (this.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                return TypeCastingUtils.canBeType(this.primitiveType, other.getPrimitiveType());
            case STRUCT_DEF_ID:
                return this.getStructDefId().equals(other.getStructDefId());
            case INLINE_ARRAY_DEF:
                return this.inlineArrayDef.equals(other.getInlineArrayDef());
            case DEFINEDTYPE_NOT_SET:
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
                result = String.format("Struct<%s,v%d>", structDefId.getName(), structDefId.getVersion());
                break;
            case INLINE_ARRAY_DEF:
                result = String.format("Array<%s>", inlineArrayDef.getElementType());
                break;
            case DEFINEDTYPE_NOT_SET:
            default:
                result = this.definedTypeCase.toString();
                break;
        }
        if (masked) result += " MASKED";
        return result;
    }

    /**
     * Performs casting of a VariableValueModel to this type.
     *
     * @param sourceValue The value to cast
     * @return A new VariableValueModel with the target type, or the original if no casting is needed
     * @throws IllegalArgumentException if casting is not supported for this type combination
     */
    public VariableValueModel applyCast(VariableValueModel sourceValue) {
        return TypeCastingUtils.applyCast(sourceValue, this.primitiveType);
    }
}
