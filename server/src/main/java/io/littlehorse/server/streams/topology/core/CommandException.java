package io.littlehorse.server.streams.topology.core;

import lombok.Getter;

@Getter
public class CommandException extends RuntimeException {

    private final boolean userError;
    private final boolean notifyClientOnError;

    public CommandException(Throwable cause, boolean userError, boolean notifyClientOnError) {
        super(cause);
        this.userError = userError;
        this.notifyClientOnError = notifyClientOnError;
    }
}
