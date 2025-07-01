package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TriggeredTaskRun;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableMutationModel;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTATask;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.LHTaskManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UTATaskModel extends LHSerializable<UTATask> {

    public TaskNodeModel task;
    public List<VariableMutationModel> mutations;
    private ExecutionContext executionContext;

    public UTATaskModel() {
        mutations = new ArrayList<>();
    }

    public Class<UTATask> getProtoBaseClass() {
        return UTATask.class;
    }

    public UTATask.Builder toProto() {
        UTATask.Builder out = UTATask.newBuilder().setTask(task.toProto());
        for (VariableMutationModel vm : mutations) {
            out.addMutations(vm.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UTATask p = (UTATask) proto;
        task = LHSerializable.fromProto(p.getTask(), TaskNodeModel.class, context);
        for (VariableMutation vm : p.getMutationsList()) {
            mutations.add(VariableMutationModel.fromProto(vm, context));
        }
        this.executionContext = context;
    }

    // TODO: There is a lot of duplicated code between here and in the TaskRun
    // infrastructure. See if possible to combine it.
    // Like hey both use the same TaskNode
    public void schedule(
            UserTaskRunModel utr, UTActionTriggerModel trigger, CoreProcessorContext processorExecutionContext)
            throws LHVarSubError {
        NodeRunModel nodeRunModel = utr.getNodeRun();

        // Next, figure out when the task should be scheduled.
        VariableValueModel delaySeconds = nodeRunModel.getThreadRun().assignVariable(trigger.delaySeconds);

        if (delaySeconds.getType() != VariableType.INT) {
            throw new LHVarSubError(null, "Delay for User Task Action was not an INT, got a " + delaySeconds.getType());
        }

        Date maturationTime = new Date(System.currentTimeMillis() + (1000 * delaySeconds.getIntVal()));
        LHTimer timer = new LHTimer(
                new CommandModel(new TriggeredTaskRun(task, utr.getNodeRun().getObjectId()), maturationTime));
        LHTaskManager taskManager = processorExecutionContext.getTaskManager();
        taskManager.scheduleTimer(timer);
    }
}
