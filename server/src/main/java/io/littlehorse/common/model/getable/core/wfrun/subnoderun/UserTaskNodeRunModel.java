package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.UserTaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.UserTaskNodeRun;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskNodeRunModel extends SubNodeRun<UserTaskNodeRun> {

    private UserTaskRunIdModel userTaskRunId;
    private ExecutionContext executionContext;
    private ProcessorExecutionContext processorContext;

    public UserTaskNodeRunModel() {}

    @Override
    public Class<UserTaskNodeRun> getProtoBaseClass() {
        return UserTaskNodeRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskNodeRun p = (UserTaskNodeRun) proto;
        if (p.hasUserTaskRunId()) {
            userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
        }
        this.executionContext = context;
        this.processorContext = processorContext.castOnSupport(ProcessorExecutionContext.class);
    }

    @Override
    public UserTaskNodeRun.Builder toProto() {
        UserTaskNodeRun.Builder out = UserTaskNodeRun.newBuilder();

        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());

        return out;
    }

    @Override
    public boolean advanceIfPossible(Date time) {
        // UserTask Node does not care about other ThreadRuns or ExternalEvents;
        // therefore we only advance the node when the UserTaskRun completes.
        return false;
    }

    @Override
    public void arrive(Date time) {
        // The UserTaskNode arrive() function should create a UserTaskRun.
        NodeModel node = getNodeRunModel().getNode();
        UserTaskNodeModel utn = node.getUserTaskNode();

        UserTaskDefModel utd =
                processorContext.service().getUserTaskDef(utn.getUserTaskDefName(), utn.getUserTaskDefVersion());
        if (utd == null) {
            // that means the UserTaskDef was deleted between now and the time that the
            // WfSpec was first created. Yikers!
            nodeRunModel.fail(new FailureModel("Appears that UserTaskDef was deleted!", LHConstants.TASK_ERROR), time);
            return;
        }
        UserTaskRunModel out = new UserTaskRunModel(utd, utn, getNodeRunModel());
        // Now we create a new UserTaskRun.

        userTaskRunId = out.getObjectId();
        out.onArrival(time);
        processorContext.getableManager().put(out);
    }

    @Override
    public void halt() {
        UserTaskRunModel userTaskRunModel = processorContext.getableManager().get(getUserTaskRunId());
        userTaskRunModel.setStatus(UserTaskRunStatus.CANCELLED);
    }
}
