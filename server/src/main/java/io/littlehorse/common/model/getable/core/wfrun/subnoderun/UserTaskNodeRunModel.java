package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.UserTaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.CheckpointIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.UserTaskNodeRun;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskNodeRunModel extends SubNodeRun<UserTaskNodeRun> {

    private UserTaskRunIdModel userTaskRunId;
    private ExecutionContext executionContext;
    private CoreProcessorContext processorContext;

    public UserTaskNodeRunModel() {
        // used by lh deserializer
    }

    public UserTaskNodeRunModel(CoreProcessorContext processorContext) {
        this.executionContext = processorContext;
        this.processorContext = processorContext;
    }

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
        this.processorContext = context.castOnSupport(CoreProcessorContext.class);
    }

    @Override
    public UserTaskNodeRun.Builder toProto() {
        UserTaskNodeRun.Builder out = UserTaskNodeRun.newBuilder();

        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());

        return out;
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        UserTaskRunModel utr = processorContext.getableManager().get(userTaskRunId);
        if (utr.getFailureToThrowKenobi() != null) {
            NodeFailureException toThrow = new NodeFailureException(utr.getFailureToThrowKenobi());
            utr.setFailureToThrowKenobi(null);
            throw toThrow;
        }
        return (utr.getStatus() == UserTaskRunStatus.DONE);
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        UserTaskRunModel userTask = processorContext.getableManager().get(userTaskRunId);

        if (userTask.getStatus() != UserTaskRunStatus.DONE) {
            throw new IllegalStateException("Tried to get output of non-DONE user task");
        }

        Map<String, Object> rawOutput = new HashMap<>();
        for (Map.Entry<String, VariableValueModel> entry : userTask.getResults().entrySet()) {
            rawOutput.put(entry.getKey(), entry.getValue().getVal());
        }
        return Optional.of(VariableValueModel.fromProto(LHLibUtil.objToVarVal(rawOutput), executionContext));
    }

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {
        // The UserTaskNode arrive() function should create a UserTaskRun.
        NodeModel node = getNodeRun().getNode();
        UserTaskNodeModel utn = node.getUserTaskNode();

        UserTaskDefModel utd =
                processorContext.service().getUserTaskDef(utn.getUserTaskDefName(), utn.getUserTaskDefVersion());
        if (utd == null) {
            // that means the UserTaskDef was deleted between now and the time that the
            // WfSpec was first created. Yikers!
            throw new NodeFailureException(
                    new FailureModel("Appears that UserTaskDef was deleted!", LHConstants.TASK_ERROR));
        }
        UserTaskRunModel out = new UserTaskRunModel(utd, utn, getNodeRun(), processorContext);
        // Now we create a new UserTaskRun.

        userTaskRunId = out.getObjectId();
        out.onArrival(time);
        processorContext.getableManager().put(out);
    }

    @Override
    public boolean maybeHalt(CoreProcessorContext processorContext) {
        UserTaskRunModel utr = processorContext.getableManager().get(userTaskRunId);
        utr.cancel();
        return true;
    }

    @Override
    public List<? extends CoreObjectId<?, ?, ?>> getCreatedSubGetableIds(CoreProcessorContext context) {
        List<CoreObjectId<?, ?, ?>> out = new ArrayList<>();
        out.add(userTaskRunId);

        UserTaskRunModel userTaskRun = context.getableManager().get(userTaskRunId);
        if (userTaskRun == null) return out;

        for (int i = 0; i < userTaskRun.getEvents().size(); i++) {
            UserTaskEventModel event = userTaskRun.getEvents().get(i);
            if (event.getExecuted() != null) {
                TaskRunModel taskRun =
                        context.getableManager().get(event.getExecuted().getTaskRunId());
                if (taskRun != null) {
                    out.add(event.getExecuted().getTaskRunId());
                    for (int j = 0; j < taskRun.getTotalCheckpoints(); j++) {
                        out.add(new CheckpointIdModel(taskRun.getId(), j));
                    }
                }
            }
        }

        return out;
    }
}
