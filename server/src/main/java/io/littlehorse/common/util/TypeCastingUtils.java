package io.littlehorse.common.util;

import io.littlehorse.common.exceptions.validation.InvalidMutationException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableType;

public class TypeCastingUtils {

    public static boolean canCastTo(VariableType sourceType, VariableType targetType) {
        if (sourceType == null) {
            return true;
        }
        if (targetType == null) {
            return false;
        }
        if (sourceType == targetType) {
            return true;
        }
        if (isPrimitive(sourceType) && targetType == VariableType.STR) {
            return true;
        }
        if (sourceType == VariableType.INT && targetType == VariableType.DOUBLE) {
            return true;
        }
        if (sourceType == VariableType.DOUBLE && targetType == VariableType.INT) {
            return true;
        }
        if (sourceType == VariableType.STR) {
            return targetType == VariableType.INT
                    || targetType == VariableType.DOUBLE
                    || targetType == VariableType.BOOL
                    || targetType == VariableType.BYTES
                    || targetType == VariableType.WF_RUN_ID;
        }
        // Reject all others
        return false;
    }

    public static boolean requiresManualCast(VariableType sourceType, VariableType targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }
        if (canBeType(sourceType, targetType)) {
            return false;
        }
        return canCastTo(sourceType, targetType);
    }

    public static boolean canBeType(VariableType sourceType, VariableType targetType) {
        if (sourceType == null) {
            return true;
        }
        if (targetType == null) {
            return false;
        }
        if (sourceType == targetType) {
            return true;
        }
        if (targetType == VariableType.STR && isPrimitive(sourceType)) {
            return true;
        }
        return sourceType == VariableType.INT && targetType == VariableType.DOUBLE;
    }

    public static boolean canBeType(TypeDefinitionModel sourceTypeDef, TypeDefinitionModel targetType) {
        if (sourceTypeDef.getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE
                || targetType.getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE) {
            return sourceTypeDef.equals(targetType);
        }
        return canBeType(sourceTypeDef.getPrimitiveType(), targetType.getPrimitiveType());
    }

    /**
     * Validates if a value of sourceType can be assigned to targetType, regardless of explicit cast.
     * Throws InvalidMutationException if the assignment is not possible by any means (with or without cast).
     * The caller is responsible for enforcing explicit cast requirements.
     */
    public static void validateTypeCompatibility(VariableType sourceType, VariableType targetType)
            throws InvalidMutationException {
        if (sourceType == null) {
            return;
        }
        if (targetType == null) {
            throw new InvalidMutationException("Cannot assign to null target type");
        }
        if (sourceType == targetType) {
            return;
        }
        if (canBeType(sourceType, targetType)) {
            return;
        }
        if (requiresManualCast(sourceType, targetType)) {
            if (!canCastTo(sourceType, targetType)) {
                throw new InvalidMutationException(
                        "Cannot cast from " + sourceType + " to " + targetType + ". This conversion is not supported.");
            }
            return;
        }
        throw new InvalidMutationException(
                "Cannot cast from " + sourceType + " to " + targetType + ". This conversion is not supported.");
    }

    public static VariableValueModel applyCast(VariableValueModel sourceValue, VariableType targetType) {

        DefinedTypeCase sourceDefinedType = sourceValue.getTypeDefinition().getDefinedTypeCase();
        if (sourceDefinedType != DefinedTypeCase.PRIMITIVE_TYPE) {
            return sourceValue;
        }
        VariableType sourceType = sourceValue.getTypeDefinition().getPrimitiveType();
        if (sourceType == null) {
            return sourceValue;
        }
        if (sourceType == targetType) {
            return sourceValue;
        }
        if (!canCastTo(sourceType, targetType)) {
            throw new IllegalArgumentException(
                    "Casting from " + sourceType + " to " + targetType + " is not supported. ");
        }
        try {
            if (targetType == VariableType.STR) {
                return sourceValue.asStr();
            }
            if (sourceType == VariableType.INT && targetType == VariableType.DOUBLE) {
                return sourceValue.asDouble();
            }
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
            String errorMessage = createCastingErrorMessage(sourceType, targetType, sourceValue, e);
            throw new IllegalArgumentException(errorMessage, e);
        }

        throw new IllegalArgumentException("Unsupported casting from " + sourceType + " to " + targetType
                + ". This should not happen - please report this as a bug.");
    }

    private static String createCastingErrorMessage(
            VariableType sourceType,
            VariableType targetType,
            VariableValueModel sourceValue,
            Exception originalException) {

        if (sourceType == VariableType.STR) {
            String actualValue = sourceValue.getStrVal();
            return switch (targetType) {
                case INT -> "Cannot parse '" + actualValue + "' as INT.";
                case DOUBLE -> "Cannot parse '" + actualValue + "' as DOUBLE.";
                case BOOL -> "Cannot parse '" + actualValue + "' as BOOL.";
                case BYTES -> "Invalid Base64 string: '" + actualValue + "'.";
                case WF_RUN_ID -> "Invalid UUID format: '" + actualValue + "'.";
                default -> "Failed to cast STR '" + actualValue + "' to " + targetType;
            };
        }

        if (sourceType == VariableType.DOUBLE && targetType == VariableType.INT) {
            return "Converting DOUBLE " + sourceValue.getDoubleVal() + " to INT. "
                    + "Note: decimal part will be truncated";
        }

        return "Failed to cast " + sourceType + " to " + targetType + ": " + originalException.getMessage();
    }

    private static boolean isPrimitive(VariableType type) {
        if (type == null) {
            return false;
        }

        return switch (type) {
            case STR, INT, DOUBLE, BOOL, BYTES, WF_RUN_ID -> true;
            default -> false;
        };
    }
}
