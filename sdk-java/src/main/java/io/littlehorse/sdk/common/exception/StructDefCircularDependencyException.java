package io.littlehorse.sdk.common.exception;

public class StructDefCircularDependencyException extends RuntimeException {
    public StructDefCircularDependencyException(String message) {
        super(message);
    }
}
