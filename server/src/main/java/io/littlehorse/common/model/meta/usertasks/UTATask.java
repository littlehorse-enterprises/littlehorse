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
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.UTATaskPb;
import io.littlehorse.sdk.common.proto.VariableMutationPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UTATask extends LHSerializable<UTATaskPb> {

    public TaskNode task;
    public List<VariableMutation> mutations;

    public UTATask() {
        mutations = new ArrayList<>();
    }

    public Class<UTATaskPb> getProtoBaseClass() {
        return UTATaskPb.class;
    }

    public UTATaskPb.Builder toProto() {
        UTATaskPb.Builder out = UTATaskPb.newBuilder().setTask(task.toProto());
        for (VariableMutation vm : mutations) {
            out.addMutations(vm.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        UTATaskPb p = (UTATaskPb) proto;
        task = LHSerializable.fromProto(p.getTask(), TaskNode.class);
        for (VariableMutationPb vm : p.getMutationsList()) {
            mutations.add(VariableMutation.fromProto(vm));
        }
    }

    // TODO: There is a lot of duplicated code between here and in the TaskRun
    // infrastructure. See if possible to combine it.
    // Like hey both use the same TaskNode
    public void schedule(LHDAO dao, UserTaskRun utr, UTActionTrigger trigger)
        throws LHVarSubError {
        NodeRun nodeRun = utr.getNodeRun();

        // Next, figure out when the task should be scheduled.
        VariableValue delaySeconds = nodeRun
            .getThreadRun()
            .assignVariable(trigger.delaySeconds);

        if (delaySeconds.getType() != VariableTypePb.INT) {
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
