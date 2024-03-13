package io.littlehorse.sdk.common.exception;

import io.littlehorse.sdk.common.proto.VariableValue;
import lombok.Getter;

/**
 * Thrown to indicate that a Task method reached a client specific error.
 */
@Getter
public class LHTaskException extends Exception {

    private final String name;
    private final VariableValue content;

    public LHTaskException(String name, String message) {
        super(message);
        this.name = name;
        this.content = VariableValue.newBuilder().build(); // null content
    }

    public LHTaskException(String name, String message, VariableValue content) {
        super(message);
        this.name = name;
        this.content = content;
    }
}
