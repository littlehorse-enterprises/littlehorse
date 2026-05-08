package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;

final class LHTypeConstraintValidator {

    private LHTypeConstraintValidator() {}

    static void ensureNoJsonPrimitiveTypes(TypeDefinition typeDefinition) throws ForbiddenJsonTypeException {
        VariableType forbiddenType = findForbiddenJsonPrimitive(typeDefinition);

        if (forbiddenType == null) {
            return;
        }

        throw new ForbiddenJsonTypeException(forbiddenType);
    }

    private static VariableType findForbiddenJsonPrimitive(TypeDefinition typeDefinition) {
        switch (typeDefinition.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                VariableType primitiveType = typeDefinition.getPrimitiveType();
                if (primitiveType == VariableType.JSON_OBJ || primitiveType == VariableType.JSON_ARR) {
                    return primitiveType;
                }
                return null;
            case INLINE_ARRAY_DEF:
                return findForbiddenJsonPrimitive(
                        typeDefinition.getInlineArrayDef().getArrayType());
            default:
                return null;
        }
    }
}
