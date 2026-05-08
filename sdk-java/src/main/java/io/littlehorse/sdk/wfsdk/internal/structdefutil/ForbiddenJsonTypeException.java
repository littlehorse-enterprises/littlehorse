package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.VariableType;

final class ForbiddenJsonTypeException extends Exception {

    private final VariableType forbiddenType;

    ForbiddenJsonTypeException(VariableType forbiddenType) {
        super(String.format("Forbidden JSON type: %s", forbiddenType));
        this.forbiddenType = forbiddenType;
    }

    VariableType getForbiddenType() {
        return forbiddenType;
    }
}
