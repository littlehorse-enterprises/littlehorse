package io.littlehorse.test.internal.step;

import io.littlehorse.test.internal.LHTestException;

public class StepExecutionException extends LHTestException {

    private final int id;
    private final String message;

    public StepExecutionException(int id, String message) {
        super();
        this.id = id;
        this.message = message;
    }

    public StepExecutionException(int id, Throwable cause) {
        super(cause);
        this.id = id;
        this.message = cause.getMessage();
    }

    @Override
    public String getMessage() {
        return "Failed to execute step %s: %s".formatted(id, message);
    }
}
