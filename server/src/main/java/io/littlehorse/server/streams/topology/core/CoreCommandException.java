package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.util.LHUtil;
import lombok.Getter;

@Getter
public class CoreCommandException extends RuntimeException {
    private final CommandModel command;
    private final boolean userError;
    private final boolean sendErrorToClient;

    public CoreCommandException(Exception cause, CommandModel command) {
        super(cause);
        this.command = command;
        userError = LHUtil.isUserError(cause);
        this.sendErrorToClient = command.hasResponse() && command.getCommandId() != null;
    }
}
