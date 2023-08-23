package io.littlehorse.sdk.common.exception;

public class TaskSchemaMismatchError extends RuntimeException {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TaskSchemaMismatchError(String message) {
        this.message = message;
    }
}
