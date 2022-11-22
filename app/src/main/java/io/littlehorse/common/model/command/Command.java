package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.subcommand.PutExternalEvent;
import io.littlehorse.common.model.command.subcommand.PutExternalEventDef;
import io.littlehorse.common.model.command.subcommand.PutTaskDef;
import io.littlehorse.common.model.command.subcommand.PutWfSpec;
import io.littlehorse.common.model.command.subcommand.RunWf;
import io.littlehorse.common.model.command.subcommand.TaskResultEvent;
import io.littlehorse.common.model.command.subcommand.TaskStartedEvent;
import io.littlehorse.common.proto.CommandPb;
import io.littlehorse.common.proto.CommandPb.CommandCase;
import io.littlehorse.common.proto.CommandPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.coreserver.CommandProcessorDaoImpl;
import java.util.Date;

public class Command extends LHSerializable<CommandPb> {

    public String commandId;
    public Date time;

    public CommandCase type;
    public TaskResultEvent taskResultEvent;
    public TaskStartedEvent taskStartedEvent;
    public PutExternalEvent putExternalEvent;
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

        switch (type) {
            case TASK_RESULT_EVENT:
                out.setTaskResultEvent(taskResultEvent.toProto());
                break;
            case TASK_STARTED_EVENT:
                out.setTaskStartedEvent(taskStartedEvent.toProto());
                break;
            case PUT_EXTERNAL_EVENT:
                out.setPutExternalEvent(putExternalEvent.toProto());
                break;
            case PUT_WF_SPEC:
                out.setPutWfSpec(putWfSpec.toProto());
                break;
            case PUT_TASK_DEF:
                out.setPutTaskDef(putTaskDef.toProto());
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                out.setPutExternalEventDef(putExternalEventDef.toProto());
                break;
            case RUN_WF:
                out.setRunWf(runWf.toProto());
                break;
            case COMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        CommandPbOrBuilder p = (CommandPbOrBuilder) proto;
        time = LHUtil.fromProtoTs(p.getTime());

        if (p.hasCommandId()) {
            commandId = p.getCommandId();
        }

        type = p.getCommandCase();
        switch (type) {
            case TASK_RESULT_EVENT:
                taskResultEvent = TaskResultEvent.fromProto(p.getTaskResultEvent());
                break;
            case TASK_STARTED_EVENT:
                taskStartedEvent =
                    TaskStartedEvent.fromProto(p.getTaskStartedEventOrBuilder());
                break;
            case PUT_EXTERNAL_EVENT:
                putExternalEvent =
                    PutExternalEvent.fromProto(p.getPutExternalEventOrBuilder());
                break;
            case PUT_WF_SPEC:
                putWfSpec = PutWfSpec.fromProto(p.getPutWfSpecOrBuilder());
                break;
            case PUT_TASK_DEF:
                putTaskDef = PutTaskDef.fromProto(p.getPutTaskDefOrBuilder());
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                putExternalEventDef =
                    PutExternalEventDef.fromProto(
                        p.getPutExternalEventDefOrBuilder()
                    );
                break;
            case RUN_WF:
                runWf = RunWf.fromProto(p.getRunWfOrBuilder());
                break;
            case COMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SubCommand<?> getSubCommand() {
        switch (type) {
            case TASK_RESULT_EVENT:
                return taskResultEvent;
            case TASK_STARTED_EVENT:
                return taskStartedEvent;
            case PUT_EXTERNAL_EVENT:
                return putExternalEvent;
            case PUT_WF_SPEC:
                return putWfSpec;
            case PUT_TASK_DEF:
                return putTaskDef;
            case PUT_EXTERNAL_EVENT_DEF:
                return putExternalEventDef;
            case RUN_WF:
                return runWf;
            case COMMAND_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }

    public boolean hasResponse() {
        return getSubCommand().hasResponse();
    }

    public LHSerializable<?> process(CommandProcessorDaoImpl dao, LHConfig config) {
        return getSubCommand().process(dao, config);
    }
}
