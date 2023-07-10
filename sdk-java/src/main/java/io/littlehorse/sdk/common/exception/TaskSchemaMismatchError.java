package io.littlehorse.jlib.common.exception;

public class TaskSchemaMismatchError extends Exception {

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
