package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.VariableType;

final class ForbiddenJsonTypeException extends Exception {

    private final VariableType forbiddenType;

    ForbiddenJsonTypeException(VariableType forbiddenType) {
        super(String.format(
                "Forbidden JSON type: %s. Within StructDefs, use native equivalents such as StructDefs for nested object types and Java arrays for native LH arrays. You can also opt to use a Type Adapter and map your class to a non-JSON primitive type.",
                forbiddenType));
        this.forbiddenType = forbiddenType;
    }

    VariableType getForbiddenType() {
        return forbiddenType;
    }
}
