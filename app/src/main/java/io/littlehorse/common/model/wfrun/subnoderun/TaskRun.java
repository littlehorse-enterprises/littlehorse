package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommand.TaskResultEvent;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.observabilityevent.ObservabilityEvent;
import io.littlehorse.common.model.observabilityevent.events.TaskResultOe;
import io.littlehorse.common.model.observabilityevent.events.TaskScheduledOe;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VarNameAndVal;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.TaskRunPb;
import io.littlehorse.jlib.common.proto.VarNameAndValPb;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskRun extends SubNodeRun<TaskRunPb> {

    public int attemptNumber;
    public VariableValue output;
    public VariableValue logOutput;

    public Date startTime;
    public String taskDefName;
    public List<VarNameAndVal> inputVariables;
    public String taskWorkerVersion;

    public TaskRun() {
        inputVariables = new ArrayList<>();
    }

    public Class<TaskRunPb> getProtoBaseClass() {
        return TaskRunPb.class;
    }

    public void initFrom(Message proto) {
        TaskRunPb p = (TaskRunPb) proto;
        attemptNumber = p.getAttemptNumber();
        if (p.hasOutput()) {
            output = VariableValue.fromProto(p.getOutput());
        }
        if (p.hasLogOutput()) {
            logOutput = VariableValue.fromProto(p.getLogOutput());
        }
        if (p.hasStartTime()) {
            startTime = LHUtil.fromProtoTs(p.getStartTime());
        }
        if (p.hasTaskWorkerVersion()) {
            taskWorkerVersion = p.getTaskWorkerVersion();
        }
        taskDefName = p.getTaskDefId();

        for (VarNameAndValPb v : p.getInputVariablesList()) {
            inputVariables.add(LHSerializable.fromProto(v, VarNameAndVal.class));
        }
    }

    public TaskRunPb.Builder toProto() {
        TaskRunPb.Builder out = TaskRunPb
            .newBuilder()
            .setTaskDefId(taskDefName)
            .setAttemptNumber(attemptNumber);

        if (taskWorkerVersion != null) {
            out.setTaskWorkerVersion(taskWorkerVersion);
        }
        if (output != null) {
            out.setOutput(output.toProto());
        }
        if (logOutput != null) {
            out.setLogOutput(logOutput.toProto());
        }
        if (startTime != null) {
            out.setStartTime(LHUtil.fromDate(startTime));
        }
        for (VarNameAndVal v : inputVariables) {
            out.addInputVariables(v.toProto());
        }

        return out;
    }

    public static TaskRun fromProto(TaskRunPb proto) {
        TaskRun out = new TaskRun();
        out.initFrom(proto);
        return out;
    }

    public boolean shouldRetry() {
        if (
            nodeRun.resultCode != TaskResultCodePb.FAILED &&
            nodeRun.resultCode != TaskResultCodePb.TIMEOUT
        ) {
            // Can only retry timeout or task failure.
            return false;
        }

        return nodeRun.attemptNumber < nodeRun.getNode().taskNode.retries;
    }

    public boolean advanceIfPossible(Date time) {
        // The task currently only cares about the input from the workers, not the
        // other threads.
        return false;
    }

    public void arrive(Date time) {
        Node node = nodeRun.getNode();

        ScheduledTask scheduledTask = new ScheduledTask();

        try {
            this.inputVariables = nodeRun.threadRun.assignVarsForNode(node.taskNode);
        } catch (LHVarSubError exn) {
            // make a call to `ThreadRun::fail()`
            nodeRun.fail(
                new Failure(
                    TaskResultCodePb.VAR_MUTATION_ERROR,
                    "Failed mutating variables upon completion: " + exn.getMessage(),
                    LHConstants.VAR_MUTATION_ERROR
                ),
                time
            );
            return;
        }

        scheduledTask.wfRunEventQueue =
            nodeRun.threadRun.wfRun.cmdDao.getWfRunEventQueue();
        scheduledTask.taskDefId = node.taskNode.taskDefName;
        scheduledTask.taskDefName = node.taskNode.taskDefName;
        scheduledTask.taskRunNumber = nodeRun.number;
        scheduledTask.taskRunPosition = nodeRun.position;
        scheduledTask.threadRunNumber = nodeRun.threadRunNumber;
        scheduledTask.wfRunId = nodeRun.threadRun.wfRunId;
        scheduledTask.wfSpecId = nodeRun.threadRun.wfSpecName;
        scheduledTask.nodeName = node.name;
        scheduledTask.variables = this.inputVariables;

        nodeRun.threadRun.wfRun.cmdDao.scheduleTask(scheduledTask);

        TaskScheduledOe oe = new TaskScheduledOe();
        oe.attemptNumber = attemptNumber;
        oe.nodeName = node.name;
        oe.taskDefName = taskDefName;
        oe.taskRunPosition = nodeRun.position;
        oe.variables = inputVariables;
        oe.wfSpecName = nodeRun.threadRun.wfSpecName;
        oe.wfSpecVersion = nodeRun.threadRun.wfSpecVersion;
        nodeRun.threadRun.wfRun.cmdDao.addObservabilityEvent(
            new ObservabilityEvent(nodeRun.wfRunId, oe)
        );
    }

    public void processStartedEvent(TaskClaimEvent se) {
        ThreadRun thread = nodeRun.threadRun;

        if (nodeRun.position != se.taskRunPosition) {
            // Out-of-order event due to race conditions between task worker
            // transactional producer and regular producer
            return;
        }

        nodeRun.status = LHStatusPb.RUNNING;
        this.taskWorkerVersion = se.taskWorkerVersion;
        Node node = nodeRun.getNode();

        // create a timer to mark the task is timeout if it does not finish
        TaskResultEvent taskResult = new TaskResultEvent();
        taskResult.resultCode = TaskResultCodePb.TIMEOUT;
        taskResult.taskRunNumber = nodeRun.number;
        taskResult.taskRunPosition = nodeRun.position;
        taskResult.threadRunNumber = nodeRun.threadRunNumber;
        taskResult.wfRunId = nodeRun.wfRunId;

        try {
            taskResult.time =
                new Date(
                    new Date().getTime() +
                    (
                        1000 *
                        thread
                            .assignVariable(node.taskNode.timeoutSeconds)
                            .asInt()
                            .intVal
                    )
                );
        } catch (LHVarSubError exn) {
            // This should be impossible.
            throw new RuntimeException(exn);
        }

        LHTimer timer = new LHTimer();
        timer.topic = nodeRun.threadRun.wfRun.cmdDao.getWfRunEventQueue();
        timer.key = nodeRun.wfRunId;
        timer.maturationTime = taskResult.time;

        // TODO: This evades encryption...
        Command taskResultCmd = new Command();
        taskResultCmd.setSubCommand(taskResult);
        taskResultCmd.time = timer.maturationTime;
        timer.payload = taskResultCmd.toProto().build().toByteArray();
        thread.wfRun.cmdDao.scheduleTimer(timer);

        startTime = se.time;
        nodeRun.status = LHStatusPb.RUNNING;
    }

    public void processTaskResult(TaskResultEvent ce) {
        if (
            nodeRun.status == LHStatusPb.COMPLETED ||
            nodeRun.status == LHStatusPb.ERROR
        ) {
            // Ignoring old event for completed task.
            // Example:
            // 1. Out-of-order TASK_START event.
            // 2. Timer event coming to signify timeout on a task that successfully
            //    completed already.
            // 3. For ERROR tasks, it's possible the TASK_COMPLETED event was late.
            //    Currently, we don't resurrect the workflow. However, in the future,
            //    we may not want to lose that data.
            return;
        }

        if (ce.taskRunPosition != nodeRun.position) {
            throw new RuntimeException("Not possible");
        }
        this.output = ce.stdout;
        this.logOutput = ce.stderr;

        switch (ce.resultCode) {
            case SUCCESS:
                nodeRun.complete(output, ce.time);

                break;
            case TIMEOUT:
                if (shouldRetry()) {
                    nodeRun.doRetry(ce.resultCode, ce.resultCode.toString(), ce.time);
                } else {
                    nodeRun.fail(
                        new Failure(
                            TaskResultCodePb.TIMEOUT,
                            "Task Timed Out: " + ce.resultCode,
                            LHConstants.TIMEOUT
                        ),
                        ce.time
                    );
                }
                break;
            case FAILED:
                if (shouldRetry()) {
                    nodeRun.doRetry(ce.resultCode, ce.resultCode.toString(), ce.time);
                } else {
                    nodeRun.fail(
                        new Failure(
                            TaskResultCodePb.FAILED,
                            "Task Failed: " + ce.resultCode,
                            LHConstants.TASK_FAILURE,
                            ce.stderr
                        ),
                        ce.time
                    );
                }
                break;
            case VAR_MUTATION_ERROR:
            case VAR_SUB_ERROR:
                // This shouldn't be possible.
                throw new RuntimeException("Impossible TaskResultCodePb");
            default:
            case UNRECOGNIZED:
                throw new RuntimeException(
                    "Unrecognized TaskResultCode: " + ce.resultCode
                );
        }

        TaskResultOe oe = new TaskResultOe();
        oe.logOutput = ce.stderr;
        oe.output = ce.stdout;
        oe.taskRunPosition = nodeRun.position;
        oe.threadRunNumber = nodeRun.threadRunNumber;
        oe.resultCode = ce.resultCode;
        nodeRun.threadRun.wfRun.cmdDao.addObservabilityEvent(
            new ObservabilityEvent(ce.wfRunId, oe)
        );
    }
}
