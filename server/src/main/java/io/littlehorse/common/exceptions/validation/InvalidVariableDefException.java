package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;

public class InvalidVariableDefException extends LHValidationException {

    public InvalidVariableDefException(VariableDefModel variableDef, LHValidationException cause) {
        super("Variable " + variableDef.getName() + " invalid: " + cause.getMessage());
    }

    public InvalidVariableDefException(VariableDefModel variableDef, String message) {
        super("Variable " + variableDef.getName() + " invalid: " + message);
    }
}
