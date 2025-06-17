package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.CancelUserTaskRunRequestModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class UTACancelModel extends LHSerializable<UTActionTrigger.UTACancel> {

    @Override
    public UTActionTrigger.UTACancel.Builder toProto() {
        return UTActionTrigger.UTACancel.newBuilder();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {}

    @Override
    public Class<UTActionTrigger.UTACancel> getProtoBaseClass() {
        return UTActionTrigger.UTACancel.class;
    }

    public void schedule(UserTaskRunModel utr, UTActionTriggerModel trigger, CoreProcessorContext processorContext)
            throws LHVarSubError {
        VariableValueModel delaySeconds = utr.getNodeRun().getThreadRun().assignVariable(trigger.delaySeconds);
        if (delaySeconds.getType() != VariableType.INT) {
            throw new LHVarSubError(null, "Delay for User Task Action was not an INT, got a " + delaySeconds.getType());
        }

        Instant maturationTime = Instant.now().plus(delaySeconds.asInt().getIntVal(), ChronoUnit.SECONDS);

        // Create the command
        CommandModel command =
                new CommandModel(new CancelUserTaskRunRequestModel(utr.getId()), Date.from(maturationTime));

        // Schedule the task
        LHTimer timer = new LHTimer(command);

        processorContext.getTaskManager().scheduleTimer(timer);
    }
}
