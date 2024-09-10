package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.util.LHUtil;
import lombok.Getter;

@Getter
public class CoreCommandException extends CommandException {
    private final CommandModel command;

    public CoreCommandException(Exception cause, CommandModel command) {
        super(cause, LHUtil.isUserError(cause), command.hasResponse() && command.getCommandId() != null);
        this.command = command;
    }
}
