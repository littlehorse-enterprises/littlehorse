package io.littlehorse.sdk.common.exception;

import io.littlehorse.sdk.common.proto.VariableValue;
import lombok.Getter;

/**
 * Maps an Exception that contains a client specific error in a Task Method.
 */
@Getter
public class LHTaskException extends RuntimeException {

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
