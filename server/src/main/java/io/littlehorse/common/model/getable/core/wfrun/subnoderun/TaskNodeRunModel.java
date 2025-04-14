package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.taskrun.TaskAttemptModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskNodeReferenceModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunSourceModel;
import io.littlehorse.common.model.getable.core.taskrun.VarNameAndValModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.TaskNodeRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeRunModel extends SubNodeRun<TaskNodeRun> {

    private TaskRunIdModel taskRunId;
    private ExecutionContext executionContext;
    private ProcessorExecutionContext processorContext;

    public TaskNodeRunModel() {
        // used by lh deserializer
    }

    public TaskNodeRunModel(ProcessorExecutionContext processorContext) {
        this.processorContext = processorContext;
    }

    @Override
    public Class<TaskNodeRun> getProtoBaseClass() {
        return TaskNodeRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskNodeRun p = (TaskNodeRun) proto;
        if (p.hasTaskRunId()) {
            taskRunId = LHSerializable.fromProto(p.getTaskRunId(), TaskRunIdModel.class, context);
        }
        this.executionContext = context;
    }

    @Override
    public TaskNodeRun.Builder toProto() {
        TaskNodeRun.Builder out = TaskNodeRun.newBuilder();

        if (taskRunId != null) out.setTaskRunId(taskRunId.toProto());

        return out;
    }

    @Override
    public boolean checkIfProcessingCompleted(ProcessorExecutionContext processorContext) throws NodeFailureException {
        TaskRunModel taskRun = processorContext.getableManager().get(taskRunId);

        if (taskRun.isStillRunning()) return false;

        if (taskRun.getStatus() == TaskStatus.TASK_SUCCESS) {
            return true;
        }

        // If we got this far, then there was a failure!
        TaskAttemptModel lastAttempt = taskRun.getLatestAttempt();
        FailureModel failure = lastAttempt
                .getFailureContent()
                .map(content ->
                        new FailureModel(lastAttempt.getFailureMessage(), lastAttempt.getFailureCode(), content))
                .orElse(new FailureModel(lastAttempt.getFailureMessage(), lastAttempt.getFailureCode()));
        throw new NodeFailureException(failure);
    }

    @Override
    public void arrive(Date time, ProcessorExecutionContext processorContext) throws NodeFailureException {
        // The TaskNode arrive() function should create a TaskRun. Note that
        // creating a TaskRun also causes the first TaskAttempt to be scheduled.

        TaskNodeModel node = nodeRun.getNode().getTaskNode();

        TaskDefModel td;
        try {
            td = node.getTaskDef(nodeRun.getThreadRun(), processorContext);
        } catch (LHVarSubError exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failed calculating dynamic task: " + exn.getMessage(), LHErrorType.VAR_SUB_ERROR.toString()));
        }
        if (td == null) {
            // that means the TaskDef was deleted between now and the time that the
            // WfSpec was first created. Yikers!
            throw new NodeFailureException(
                    new FailureModel("Appears that TaskDef was deleted!", LHConstants.TASK_ERROR));
        }

        List<VarNameAndValModel> inputVariables;

        try {
            inputVariables = node.assignInputVars(nodeRun.getThreadRun(), processorContext);
        } catch (LHVarSubError exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failed calculating TaskRun Input Vars: " + exn.getMessage(), LHConstants.VAR_SUB_ERROR));
        }

        // Create a TaskRun
        TaskNodeReferenceModel source = new TaskNodeReferenceModel(nodeRun.getObjectId(), nodeRun.getWfSpecId());

        this.taskRunId = new TaskRunIdModel(nodeRun.getId().getWfRunId(), processorContext);
        TaskRunModel task = new TaskRunModel(
                inputVariables,
                new TaskRunSourceModel(source, processorContext),
                node,
                processorContext,
                this.taskRunId,
                td.getId());
        task.setId(taskRunId);
        task.dispatchTaskToQueue();

        // When creating a new Getable for the first time, we need to explicitly save it.
        processorContext.getableManager().put(task);
    }

    @Override
    public Optional<VariableValueModel> getOutput(ProcessorExecutionContext processorContext) {
        TaskRunModel taskRun = processorContext.getableManager().get(taskRunId);
        if (taskRun.getStatus() != TaskStatus.TASK_SUCCESS) {
            throw new IllegalStateException("somehow called getOutput() on taskRun that's not done yet");
        }
        return Optional.of(taskRun.getLatestAttempt().getOutput());
    }

    @Override
    public boolean maybeHalt(ProcessorExecutionContext processorContext) {
        // TODO as part of #606: a TaskRun should be interruptible between retries.
        // For now, we can't interrupt a TaskRun until it's fully done.
        return !processorContext.getableManager().get(getTaskRunId()).isStillRunning();
    }

    @Override
    public Set<MetricSpecIdModel> metricsToCollect() {
        return Set.of(new MetricSpecIdModel());
    }

    @Override
    public Optional<TaskRunIdModel> getCreatedSubGetableId() {
        return Optional.ofNullable(taskRunId);
    }
}
