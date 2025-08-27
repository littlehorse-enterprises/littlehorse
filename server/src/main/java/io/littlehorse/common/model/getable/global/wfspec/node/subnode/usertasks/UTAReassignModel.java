package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.DeadlineReassignUserTaskModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTAReassign;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.LHTaskManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UTAReassignModel extends LHSerializable<UTAReassign> {

    private VariableAssignmentModel userId;
    private VariableAssignmentModel userGroup;
    private ExecutionContext executionContext;

    @Override
    public UTAReassign.Builder toProto() {
        UTAReassign.Builder out = UTAReassign.newBuilder();
        if (userId != null) out.setUserId(userId.toProto());
        if (userGroup != null) out.setUserGroup(userGroup.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UTAReassign p = (UTAReassign) proto;
        if (p.hasUserGroup()) userGroup = VariableAssignmentModel.fromProto(p.getUserGroup(), context);
        if (p.hasUserId()) userId = VariableAssignmentModel.fromProto(p.getUserId(), context);
        this.executionContext = context;
    }

    @Override
    public Class<UTAReassign> getProtoBaseClass() {
        return UTAReassign.class;
    }

    public void schedule(UserTaskRunModel utr, UTActionTriggerModel trigger, CoreProcessorContext processorContext)
            throws LHVarSubError {
        LHTaskManager taskManager = processorContext.getTaskManager();
        NodeRunModel nodeRunModel = utr.getNodeRun();

        // Figure out when the task should be scheduled.
        VariableValueModel delaySeconds = nodeRunModel.getThreadRun().assignVariable(trigger.delaySeconds);
        if (delaySeconds.getTypeDefinition().getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE) {
            throw new LHVarSubError(
                    null, "Delay for User Task Action was not an INT, got a " + delaySeconds.getTypeDefinition());
        }
        if (delaySeconds.getTypeDefinition().getPrimitiveType() != VariableType.INT) {
            throw new LHVarSubError(
                    null,
                    "Delay for User Task Action was not an INT, got a "
                            + delaySeconds.getTypeDefinition().getPrimitiveType());
        }

        Instant maturationTime = Instant.now().plus(delaySeconds.asInt().getIntVal(), ChronoUnit.SECONDS);

        // Create the command
        CommandModel command = new CommandModel(
                new DeadlineReassignUserTaskModel(utr.getId(), userId, userGroup, utr.getEpoch()),
                Date.from(maturationTime));

        // Schedule the task
        LHTimer timer = new LHTimer(command);

        taskManager.scheduleTimer(timer);
    }
}
