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
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
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
    private VariableType type;

    public TypeDefinitionModel() {}

    public TypeDefinitionModel(VariableType type) {
        // TODO: determine whether this should be refactored to fail when type == Struct.
        this.type = type;
        this.masked = false;
    }

    public TypeDefinitionModel(VariableType type, boolean masked) {
        this.type = type;
        this.masked = masked;
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

    public Optional<TypeDefinitionModel> resolveTypeAfterMutationWith(
            VariableMutationType operation, TypeDefinitionModel rhs, ReadOnlyMetadataManager manager)
            throws InvalidExpressionException {
        return getTypeStrategy().resolveOperation(manager, operation, rhs.getTypeStrategy());
    }

    public LHTypeStrategy getTypeStrategy() {
        // TODO: Support StructDefs
        switch (type) {
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
        // TODO: Extend this when adding Struct and StructDef.
        switch (type) {
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

    private static boolean isPrimitive(VariableType type) {
        return switch (type) {
            case INT, DOUBLE, STR, BOOL, WF_RUN_ID, BYTES -> true;
            default -> false;
        };
    }

    public static TypeDefinitionModel fromProto(TypeDefinition proto, ExecutionContext context) {
        TypeDefinitionModel out = new TypeDefinitionModel();
        out.initFrom(proto, context);
        return out;
    }

    public boolean isJson() {
        return type == VariableType.JSON_ARR || type == VariableType.JSON_OBJ;
    }

    /**
     * Returns true if the VariableValueModel matches this type or can be casted to it (autocast).
     * Supports automatic casting for primitive types:
     * - Any primitive type → STR
     * - INT → DOUBLE
     */
    public boolean isCompatibleWith(VariableValueModel value) {
        return canCastTo(value.getTypeDefinition().getType(), this.type);
    }

    /**
     * Returns true if this type is compatible with the other type.
     * This method answers: "Can this type be used where the other type is expected?"
     * Supports automatic casting for primitive types:
     * - Any primitive type → STR
     * - INT → DOUBLE
     */
    public boolean isCompatibleWith(TypeDefinitionModel other) {
        return canCastTo(this.getType(), other.getType());
    }

    /**
     * Checks if the source type can be cast to the target type.
     * Implements both automatic and manual casting rules:
     * - Any primitive type → STR (automatic)
     * - INT → DOUBLE (automatic)
     * - DOUBLE → INT, STR → INT/DOUBLE/BOOL/BYTES/WF_RUN_ID (manual)
     */
    public static boolean canCastTo(VariableType sourceType, VariableType targetType) {
        if (sourceType == targetType) {
            return true;
        }

        // Automatic casting rules
        if (targetType == VariableType.STR && isPrimitive(sourceType)) {
            return true;
        }
        if (sourceType == VariableType.INT && targetType == VariableType.DOUBLE) {
            return true;
        }

        // Manual casting rules - all require explicit cast() calls
        if (!isPrimitive(sourceType) || !isPrimitive(targetType)) {
            return false;
        }

        return switch (sourceType) {
            case DOUBLE -> targetType == VariableType.INT;
            case STR -> targetType == VariableType.INT
                    || targetType == VariableType.DOUBLE
                    || targetType == VariableType.BOOL
                    || targetType == VariableType.BYTES
                    || targetType == VariableType.WF_RUN_ID;
            default -> false;
        };
    }

    /**
     * Checks if manual casting is required between source and target types.
     * Returns true for conversions that need explicit .cast() calls.
     */
    public static boolean requiresManualCast(VariableType sourceType, VariableType targetType) {
        if (sourceType == targetType) {
            return false;
        }

        // Check if it's an automatic cast
        if (targetType == VariableType.STR && isPrimitive(sourceType)) {
            return false; // Automatic
        }
        if (sourceType == VariableType.INT && targetType == VariableType.DOUBLE) {
            return false; // Automatic
        }

        // If canCastTo returns true but it's not automatic, then it's manual
        return canCastTo(sourceType, targetType);
    }

    @Override
    public String toString() {
        // TODO: when we have Structs, print out structdefid
        String result = type.toString();
        if (masked) result += " MASKED";
        return result;
    }

    /**
     * Performs casting of a VariableValueModel to this type.
     * Handles both automatic and manual casting rules:
     * - Any primitive type → STR (automatic)
     * - INT → DOUBLE (automatic)
     * - DOUBLE → INT, STR → INT/DOUBLE/BOOL/BYTES/WF_RUN_ID (manual)
     *
     * @param sourceValue The value to cast
     * @return A new VariableValueModel with the target type, or the original if no casting is needed
     * @throws IllegalArgumentException if casting is not supported for this type combination
     */
    public VariableValueModel castTo(VariableValueModel sourceValue) {
        VariableType sourceType = sourceValue.getTypeDefinition().getType();
        VariableType targetType = this.type;

        if (sourceType == targetType) {
            return sourceValue;
        }

        if (!canCastTo(sourceType, targetType)) {
            throw new IllegalArgumentException(
                    "Casting from " + sourceType + " to " + targetType + " is not supported");
        }

        try {
            // Automatic casting rules
            if (targetType == VariableType.STR) {
                return sourceValue.asStr();
            }
            if (sourceType == VariableType.INT && targetType == VariableType.DOUBLE) {
                return sourceValue.asDouble();
            }

            // Manual casting rules
            if (sourceType == VariableType.DOUBLE && targetType == VariableType.INT) {
                return sourceValue.asInt();
            }

            if (sourceType == VariableType.STR) {
                return switch (targetType) {
                    case INT -> sourceValue.asInt();
                    case DOUBLE -> sourceValue.asDouble();
                    case BOOL -> sourceValue.asBool();
                    case BYTES -> sourceValue.asBytes();
                    case WF_RUN_ID -> sourceValue.asWfRunId();
                    default -> throw new IllegalArgumentException("Unsupported STR cast to " + targetType);
                };
            }
        } catch (Exception e) {
            // Provide user-friendly error messages for manual casting failures
            String errorMessage =
                    switch (sourceType) {
                        case STR -> switch (targetType) {
                            case INT -> "Cannot parse '" + sourceValue.getStrVal() + "' as INT";
                            case DOUBLE -> "Cannot parse '" + sourceValue.getStrVal() + "' as DOUBLE";
                            case BOOL -> "Cannot parse '" + sourceValue.getStrVal() + "' as BOOL (use 'true'/'false')";
                            case BYTES -> "Invalid Base64 string: '" + sourceValue.getStrVal() + "'";
                            case WF_RUN_ID -> "Invalid UUID format: '" + sourceValue.getStrVal() + "'";
                            default -> "Failed to cast STR to " + targetType;
                        };
                        default -> "Failed to cast " + sourceType + " to " + targetType + ": " + e.getMessage();
                    };
            throw new IllegalArgumentException(errorMessage, e);
        }

        throw new IllegalArgumentException("Unsupported casting from " + sourceType + " to " + targetType);
    }
}
