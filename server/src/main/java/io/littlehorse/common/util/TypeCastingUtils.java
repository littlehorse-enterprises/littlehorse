package io.littlehorse.common.util;

import io.littlehorse.common.exceptions.validation.InvalidMutationException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.VariableType;

public class TypeCastingUtils {

    public static boolean canCastTo(VariableType sourceType, VariableType targetType) {
        if (sourceType == targetType) {
            return true;
        }

        if (targetType == VariableType.STR && isPrimitive(sourceType)) {
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
        if (sourceType == targetType) {
            return false;
        }
        if (isAutomaticCast(sourceType, targetType)) {
            return false;
        }
        return canCastTo(sourceType, targetType);
    }

    public static boolean isAutomaticCast(VariableType sourceType, VariableType targetType) {
        if (sourceType == targetType) {
            return true;
        }
        if (targetType == VariableType.STR && isPrimitive(sourceType)) {
            return true;
        }
        return sourceType == VariableType.INT && targetType == VariableType.DOUBLE;
    }

    public static boolean canAssignWithoutCast(VariableType sourceType, VariableType targetType) {
        return isAutomaticCast(sourceType, targetType);
    }

    public static void validateAssignment(
            VariableType sourceType, VariableType targetType, boolean hasCast, String context)
            throws InvalidMutationException {
        if (sourceType == targetType) {
            return;
        }
        if (isAutomaticCast(sourceType, targetType)) {
            return;
        }
        if (requiresManualCast(sourceType, targetType)) {
            if (!hasCast) {
                throw new InvalidMutationException("Cannot assign " + sourceType + " to " + targetType
                        + " without explicit casting. Use .cast() method to perform manual type conversion."
                        + (context != null ? " Context: " + context : ""));
            }
            return;
        }

        throw new InvalidMutationException("Cannot cast from " + sourceType + " to " + targetType
                + ". This conversion is not supported." + (context != null ? " Context: " + context : ""));
    }

    public static VariableValueModel performCast(VariableValueModel sourceValue, VariableType targetType) {
        VariableType sourceType = sourceValue.getTypeDefinition().getType();

        if (sourceType == targetType) {
            return sourceValue;
        }

        if (!canCastTo(sourceType, targetType)) {
            throw new IllegalArgumentException(
                    "Casting from " + sourceType + " to " + targetType + " is not supported");
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

    private static boolean isPrimitive(VariableType type) {
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
