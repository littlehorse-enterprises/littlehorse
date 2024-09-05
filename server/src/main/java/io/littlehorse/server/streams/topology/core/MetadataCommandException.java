package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.util.LHUtil;
import lombok.Getter;

@Getter
public class MetadataCommandException extends CommandException {
    private final MetadataCommandModel command;

    public MetadataCommandException(Exception cause, MetadataCommandModel command) {
        super(cause, LHUtil.isUserError(cause), command.hasResponse() && command.getCommandId() != null);
        this.command = command;
    }
}
