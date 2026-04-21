package io.littlehorse.server.streams.topology.core;

public class CommandException extends RuntimeException {
    private final boolean userError;
    private final boolean notifyClientOnError;

    public CommandException(Throwable cause, boolean userError, boolean notifyClientOnError) {
        super(cause);
        this.userError = userError;
        this.notifyClientOnError = notifyClientOnError;
    }

    public boolean isUserError() {
        return this.userError;
    }

    public boolean isNotifyClientOnError() {
        return this.notifyClientOnError;
    }
}
