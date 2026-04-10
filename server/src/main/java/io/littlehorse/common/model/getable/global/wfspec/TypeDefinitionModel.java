package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.UnknownStructDefException;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructValidationException;
import io.littlehorse.common.model.getable.global.wfspec.variable.LHPathModel;
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
import io.littlehorse.common.util.LHUtil.LHComparisonRule;
import io.littlehorse.common.util.TypeCastingUtils;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.List;
import java.util.Map;
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

    public TypeDefinitionModel(TypeDefinitionModel other) {
        if (other == null) {
            this.definedTypeCase = DefinedTypeCase.DEFINEDTYPE_NOT_SET;
            return;
        }
        this.masked = other.masked;
        this.definedTypeCase = other.definedTypeCase;

        switch (other.definedTypeCase) {
            case PRIMITIVE_TYPE:
                this.primitiveType = other.primitiveType;
                break;
            case STRUCT_DEF_ID:
                this.structDefId = other.structDefId == null
                        ? null
                        : new StructDefIdModel(other.structDefId.getName(), other.structDefId.getVersion());
                break;
            case INLINE_ARRAY_DEF:
                this.inlineArrayDef =
                        other.inlineArrayDef == null ? null : new InlineArrayDefModel(other.inlineArrayDef);
                break;
            case DEFINEDTYPE_NOT_SET:
            default:
                this.definedTypeCase = DefinedTypeCase.DEFINEDTYPE_NOT_SET;
                break;
        }
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
                this.definedTypeCase = DefinedTypeCase.PRIMITIVE_TYPE;
                this.primitiveType = VariableType.JSON_OBJ;
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

    public List<LHComparisonRule> getComparisonRules() {
        switch (this.definedTypeCase) {
            case PRIMITIVE_TYPE:
                switch (primitiveType) {
                    case INT:
                    case DOUBLE:
                    case STR:
                    case BOOL:
                    case TIMESTAMP:
                        return List.of(LHComparisonRule.IDENTITY, LHComparisonRule.MAGNITUDE);
                    case WF_RUN_ID:
                    case BYTES:
                        return List.of(LHComparisonRule.IDENTITY);
                    case JSON_ARR:
                    case JSON_OBJ:
                        return List.of(LHComparisonRule.IDENTITY, LHComparisonRule.INCLUDES);
                    case UNRECOGNIZED:
                }
                break;
            case STRUCT_DEF_ID:
                return List.of(LHComparisonRule.IDENTITY, LHComparisonRule.INCLUDES);
            case INLINE_ARRAY_DEF:
                return List.of(LHComparisonRule.IDENTITY, LHComparisonRule.INCLUDES);
            case DEFINEDTYPE_NOT_SET:
                return List.of(LHComparisonRule.IDENTITY, LHComparisonRule.INCLUDES, LHComparisonRule.MAGNITUDE);
            default:
        }
        return List.of();
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

    /**
     * If this TypeDefinition references a StructDef, validates that the StructDef exists.
     * @param metadataManager the metadata manager to look up the StructDef.
     * @throws UnknownStructDefException if the referenced StructDef does not exist.
     */
    public void validateStructDefExists(ReadOnlyMetadataManager metadataManager) throws UnknownStructDefException {
        if (definedTypeCase != DefinedTypeCase.STRUCT_DEF_ID) return;

        WfService wfService = new WfService(metadataManager);
        if (wfService.getStructDef(structDefId) == null) {
            throw new UnknownStructDefException(structDefId.getName());
        }
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
     * Gets the nested type in this TypeDefinition according to an LHPath
     * @param lhPath an LHPath to a nested type
     * @param metadataManager a metadata manager for fetching additional StructDefs
     * @return an Optional with you
     * @throws InvalidExpressionException
     */
    public Optional<TypeDefinitionModel> getNestedType(LHPathModel lhPath, ReadOnlyMetadataManager metadataManager)
            throws InvalidExpressionException {
        TypeDefinitionModel currentTypeDef = this;

        for (Selector selector : lhPath.getPath()) {
            switch (currentTypeDef.getDefinedTypeCase()) {
                case PRIMITIVE_TYPE:
                    switch (currentTypeDef.getPrimitiveType()) {
                        case JSON_ARR:
                        case JSON_OBJ:
                            return Optional.empty();
                        default:
                            throw new InvalidExpressionException(String.format(
                                    "Failed fetching LHPath to '%s' on Type '%s'",
                                    lhPath.toJsonPathStr(), currentTypeDef));
                    }
                case STRUCT_DEF_ID:
                    StructDefModel structDef =
                            new WfService(metadataManager).getStructDef(currentTypeDef.getStructDefId());

                    if (structDef == null) {
                        throw new InvalidExpressionException("StructDef not found: " + currentTypeDef);
                    }

                    Map<String, StructFieldDefModel> fieldDefs =
                            structDef.getStructDef().getFields();

                    if (!fieldDefs.containsKey(selector.getKey())) {
                        throw new InvalidExpressionException(String.format(
                                "could not find field '%s' on type %s", selector.getKey(), currentTypeDef));
                    }

                    currentTypeDef = fieldDefs.get(selector.getKey()).getFieldType();
                    break;
                case INLINE_ARRAY_DEF:
                    if (selector.getSelectorTypeCase() != Selector.SelectorTypeCase.INDEX) {
                        throw new InvalidExpressionException(String.format(
                                "Expected numeric index selector for Array type, got key selector '%s'",
                                selector.getKey()));
                    }
                    currentTypeDef = currentTypeDef.getInlineArrayDef().getArrayType();
                    break;
                case DEFINEDTYPE_NOT_SET:
                    break;
            }
        }

        return Optional.of(currentTypeDef);
    }

    /**
     * Returns true if the VariableValueModel matches this type.
     * @throws StructValidationException
     */
    public boolean isCompatibleWith(VariableValueModel value, ReadOnlyMetadataManager readOnlyMetadataManager) {
        if (value.getValueType() == ValueCase.STRUCT) {
            try {
                value.getStruct().validateAgainstStructDefId(readOnlyMetadataManager);
            } catch (StructValidationException e) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        String.format("Provided Struct incompatible with type %s: %s", this, e.getMessage()));
            }
        } else if (value.getValueType() == ValueCase.VALUE_NOT_SET) {
            return true;
        }

        TypeDefinitionModel other = value.getTypeDefinition();

        // If this is an inline array type, validate every element in the provided
        // array value against the array's element type. The VariableValueModel
        // derives its array type from the first element only, which can allow
        // mixed-typed arrays to appear compatible. Enforce per-element checks here.
        if (this.getDefinedTypeCase() == DefinedTypeCase.INLINE_ARRAY_DEF && value.getValueType() == ValueCase.ARRAY) {
            TypeDefinitionModel expectedElementType = this.getInlineArrayDef().getArrayType();
            for (VariableValueModel item : value.getArray().getItems()) {
                TypeDefinitionModel itemType = item.getTypeDefinition();
                if (!expectedElementType.isCompatibleWith(itemType)) {
                    return false;
                }
            }
        }

        return this.isCompatibleWith(other);
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
                return TypeCastingUtils.canBeType(other.getPrimitiveType(), this.getPrimitiveType());
            case STRUCT_DEF_ID:
                return this.getStructDefId().equals(other.getStructDefId());
            case INLINE_ARRAY_DEF:
                // If the other array's item type is undefined (reported for empty/native arrays),
                // treat it as a wildcard that is compatible with any array element type.
                if (other.getInlineArrayDef() == null
                        || other.getInlineArrayDef().getArrayType() == null
                        || other.getInlineArrayDef().getArrayType().isNull()) {
                    return true;
                }
                return this.getInlineArrayDef().equals(other.getInlineArrayDef());
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
                result = String.format("Array<%s>", inlineArrayDef.getArrayType());
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
