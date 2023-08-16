package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.TriggeredTaskRun;
import io.littlehorse.common.model.meta.VariableMutation;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTATask;
import io.littlehorse.sdk.common.proto.VariableMutationPb;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UTATaskModel extends LHSerializable<UTATask> {

    public TaskNode task;
    public List<VariableMutation> mutations;

    public UTATaskModel() {
        mutations = new ArrayList<>();
    }

    public Class<UTATask> getProtoBaseClass() {
        return UTATask.class;
    }

    public UTATask.Builder toProto() {
        UTATask.Builder out = UTATask.newBuilder().setTask(task.toProto());
        for (VariableMutation vm : mutations) {
            out.addMutations(vm.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        UTATask p = (UTATask) proto;
        task = LHSerializable.fromProto(p.getTask(), TaskNode.class);
        for (VariableMutationPb vm : p.getMutationsList()) {
            mutations.add(VariableMutation.fromProto(vm));
        }
    }

    // TODO: There is a lot of duplicated code between here and in the TaskRun
    // infrastructure. See if possible to combine it.
    // Like hey both use the same TaskNode
    public void schedule(LHDAO dao, UserTaskRun utr, UTActionTriggerModel trigger)
        throws LHVarSubError {
        NodeRunModel nodeRunModel = utr.getNodeRun();

        // Next, figure out when the task should be scheduled.
        VariableValueModel delaySeconds = nodeRunModel
            .getThreadRun()
            .assignVariable(trigger.delaySeconds);

        if (delaySeconds.getType() != VariableType.INT) {
            throw new LHVarSubError(
                null,
                "Delay for User Task Action was not an INT, got a " +
                delaySeconds.getType()
            );
        }

        Date maturationTime = new Date(
            System.currentTimeMillis() + (1000 * delaySeconds.intVal)
        );
        LHTimer timer = new LHTimer(
            new Command(
                new TriggeredTaskRun(task, utr.getNodeRun().getObjectId()),
                maturationTime
            ),
            dao
        );

        dao.scheduleTimer(timer);
    }
}
