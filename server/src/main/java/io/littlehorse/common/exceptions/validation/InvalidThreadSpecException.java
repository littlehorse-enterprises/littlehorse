package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;

public class InvalidThreadSpecException extends LHValidationException {
    
    public InvalidThreadSpecException(ThreadSpecModel threadSpec, LHValidationException cause) {
        super("ThreadSpec " + threadSpec.getName() + " invalid: " + cause.getMessage());
    }

    public InvalidThreadSpecException(ThreadSpecModel threadSpec, String message) {
        super("ThreadSpec " + threadSpec.getName() + " invalid: " + message);
    }
}
