package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.subcommand.ExternalEvent;
import io.littlehorse.common.model.command.subcommand.PutExternalEventDef;
import io.littlehorse.common.model.command.subcommand.PutTaskDef;
import io.littlehorse.common.model.command.subcommand.PutWfSpec;
import io.littlehorse.common.model.command.subcommand.TaskResultEvent;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.proto.CommandPb;
import io.littlehorse.common.proto.CommandPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.coreserver.CoreServerProcessorDaoImpl;
import java.util.Date;

public class Command extends LHSerializable<CommandPb> {

    public String commandId;
    public Date time;

    public TaskResultEvent taskResultEvent;
    public TaskStartedEvent taskStartedEvent;
    public ExternalEvent externalEvent;
    public PutWfSpec putWfSpec;
    public PutTaskDef putTaskDef;
    public PutExternalEventDef putExternalEventDef;
    public RunWf runWf;

    public Class<CommandPb> getProtoBaseClass() {
        return CommandPb.class;
    }

    public CommandPb.Builder toProto() {
        CommandPb.Builder out = CommandPb.newBuilder();
        out.setTime(LHUtil.fromDate(time));

        if (commandId != null) {
            out.setCommandId(commandId);
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        CommandPbOrBuilder p = (CommandPbOrBuilder) proto;
        time = LHUtil.fromProtoTs(p.getTime());

        if (p.hasCommandId()) {
            commandId = p.getCommandId();
        }
    }

    public SubCommand<?> getSubCommand() {
        return null;
    }

    public boolean hasResponse() {
        return getSubCommand().hasResponse();
    }

    public LHSerializable<?> process(
        CoreServerProcessorDaoImpl dao,
        LHConfig config
    ) {
        return getSubCommand().process(dao, config);
    }
}
