package io.littlehorse.sdk.common.exception;

public class TaskExecutionException extends Exception {

    private Exception parent;

    public TaskExecutionException(Exception parent) {
        this.parent = parent;
    }

    public Exception getParent() {
        return parent;
    }
}
