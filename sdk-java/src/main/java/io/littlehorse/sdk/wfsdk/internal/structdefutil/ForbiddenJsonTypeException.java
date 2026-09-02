package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.VariableType;

final class ForbiddenJsonTypeException extends Exception {

    private final VariableType forbiddenType;

    ForbiddenJsonTypeException(VariableType forbiddenType) {
        super(String.format(
                "Forbidden JSON type: %s. Within LittleHorse native types, use native equivalents such as Structs, Maps, and Arrays. Alternatively, you can use a Type Adapter to map your class to a non-JSON primitive type.",
                forbiddenType));
        this.forbiddenType = forbiddenType;
    }

    VariableType getForbiddenType() {
        return forbiddenType;
    }
}
