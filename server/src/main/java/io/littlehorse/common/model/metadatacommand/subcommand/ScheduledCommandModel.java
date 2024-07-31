package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.proto.ScheduledCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.Optional;

public class ScheduledCommandModel extends CoreSubCommand<ScheduledCommand> {
    private CommandModel commandToExecute;
    private String cronExpression;

    public ScheduledCommandModel() {}

    public ScheduledCommandModel(CommandModel commandToExecute, String cronExpression) {
        this.commandToExecute = commandToExecute;
        this.cronExpression = cronExpression;
        if (commandToExecute.getScheduleCommand() != null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ScheduledCommand p = (ScheduledCommand) proto;
        commandToExecute = LHSerializable.fromProto(p.getCommandToExecute(), CommandModel.class, context);
        cronExpression = p.getCronExpression();
    }

    @Override
    public ScheduledCommand.Builder toProto() {
        return ScheduledCommand.newBuilder()
                .setCommandToExecute(commandToExecute.toProto())
                .setCronExpression(cronExpression);
    }

    @Override
    public Class<ScheduledCommand> getProtoBaseClass() {
        return ScheduledCommand.class;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        Message response = commandToExecute.process(executionContext, config);
        Optional<Date> scheduledTime = LHUtil.nextDate(cronExpression, commandToExecute.getTime());
        if (scheduledTime.isPresent()) {
            CommandModel nextCommand = new CommandModel(commandToExecute.getSubCommand(), scheduledTime.get());
            LHTimer next = new LHTimer(nextCommand);
            executionContext.getTaskManager().scheduleTimer(next);
        }
        return response;
    }

    @Override
    public String getPartitionKey() {
        return commandToExecute.getPartitionKey();
    }
}
