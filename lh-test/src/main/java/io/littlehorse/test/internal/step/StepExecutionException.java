package io.littlehorse.test.internal.step;

import io.littlehorse.test.internal.LHTestException;

public class StepExecutionException extends LHTestException {

    private final int id;

    public StepExecutionException(int id, Throwable cause) {
        super(cause);
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "Failed to execute step %s".formatted(id);
    }
}
