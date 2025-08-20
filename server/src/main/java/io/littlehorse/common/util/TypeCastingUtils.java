package io.littlehorse.common.util;

import io.littlehorse.common.exceptions.validation.InvalidMutationException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
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

    public static boolean requiresManualCast(VariableType sourceType, VariableType targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }

        if (canAssignWithoutCast(sourceType, targetType)) {
            return false;
        }
        return canCastTo(sourceType, targetType);
    }

    public static boolean canAssignWithoutCast(VariableType sourceType, VariableType targetType) {
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

    public static void validateTypeCompatibility(
            VariableType sourceType, VariableType targetType, boolean hasExplicitCast) throws InvalidMutationException {

        if (sourceType == null) {
            return;
        }

        if (targetType == null) {
            throw new InvalidMutationException("Cannot assign to null target type");
        }

        if (sourceType == targetType) {
            return;
        }

        if (canAssignWithoutCast(sourceType, targetType)) {
            return;
        }

        if (requiresManualCast(sourceType, targetType)) {
            if (!hasExplicitCast) {
                String suggestion = getCastingSuggestion(sourceType, targetType);
                throw new InvalidMutationException("Cannot assign " + sourceType + " to " + targetType
                        + " without explicit casting. " + suggestion);
            }
            return;
        }

        throw new InvalidMutationException(
                "Cannot cast from " + sourceType + " to " + targetType + ". This conversion is not supported.");
    }

    public static VariableValueModel applyCast(VariableValueModel sourceValue, VariableType targetType) {

        if (sourceValue == null) {
            return null;
        }

        VariableType sourceType = sourceValue.getTypeDefinition().getType();

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

    public static String getCastingSuggestion(VariableType sourceType, VariableType targetType) {
        String methodName =
                switch (targetType) {
                    case INT -> "castToInt()";
                    case DOUBLE -> "castToDouble()";
                    case STR -> "castToStr()";
                    case BOOL -> "castToBool()";
                    case BYTES -> "castToBytes()";
                    case WF_RUN_ID -> "castToWfRunId()";
                    default -> "cast(VariableType." + targetType + ")";
                };

        return "Use ." + methodName + " or .cast(VariableType." + targetType + ") method.";
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

    public static boolean canBeTypeWithoutCast(VariableType targetType, boolean isNodeOutput) {

        if (isNodeOutput) {
            if (targetType == VariableType.DOUBLE) {
                return true;
            }
            if (targetType == VariableType.STR) {
                return true;
            }
            return targetType != VariableType.INT && targetType != VariableType.BOOL;
        }
        return true;
    }
}
