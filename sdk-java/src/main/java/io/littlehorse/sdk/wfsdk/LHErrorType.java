package io.littlehorse.sdk.wfsdk;

import lombok.Getter;

@Getter
public enum LHErrorType {
    CHILD_FAILURE("CHILD_FAILURE"),
    VAR_SUB_ERROR("VAR_SUB_ERROR"),
    VAR_MUTATION_ERROR("VAR_MUTATION_ERROR"),
    USER_TASK_CANCELLED("USER_TASK_CANCELLED"),
    TIMEOUT("TIMEOUT"),
    TASK_FAILURE("TASK_FAILURE"),
    VAR_ERROR("VAR_ERROR"),
    TASK_ERROR("TASK_ERROR"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String internalName;

    LHErrorType(String internalName) {
        this.internalName = internalName;
    }
}
