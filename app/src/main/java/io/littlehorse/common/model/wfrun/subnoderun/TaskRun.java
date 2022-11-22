package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.command.subcommand.TaskResultEvent;
import io.littlehorse.common.model.command.subcommand.TaskStartedEvent;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.TaskRunPb;
import io.littlehorse.common.proto.TaskRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class TaskRun extends SubNodeRun<TaskRunPb> {

    public int attemptNumber;
    public VariableValue output;
    public VariableValue logOutput;

    public Date startTime;
    public String taskDefName;

    public Class<TaskRunPb> getProtoBaseClass() {
        return TaskRunPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskRunPbOrBuilder p = (TaskRunPbOrBuilder) proto;
        attemptNumber = p.getAttemptNumber();
        if (p.hasOutput()) {
            output = VariableValue.fromProto(p.getOutputOrBuilder());
        }
        if (p.hasLogOutput()) {
            logOutput = VariableValue.fromProto(p.getLogOutput());
        }

        if (p.hasStartTime()) {
            startTime = LHUtil.fromProtoTs(p.getStartTime());
        }
        taskDefName = p.getTaskDefId();
    }

    public TaskRunPb.Builder toProto() {
        TaskRunPb.Builder out = TaskRunPb
            .newBuilder()
            .setTaskDefId(taskDefName)
            .setAttemptNumber(attemptNumber);

        if (output != null) {
            out.setOutput(output.toProto());
        }
        if (logOutput != null) {
            out.setLogOutput(logOutput.toProto());
        }
        if (startTime != null) {
            out.setStartTime(LHUtil.fromDate(startTime));
        }

        return out;
    }

    public static TaskRun fromProto(TaskRunPbOrBuilder proto) {
        TaskRun out = new TaskRun();
        out.initFrom(proto);
        return out;
    }

    public List<Tag> getTags(NodeRun parent) {
        List<Tag> out = new ArrayList<>();
        out.add(
            new Tag(
                parent,
                Pair.of("taskDefId", taskDefName),
                Pair.of("status", parent.status.toString())
            )
        );

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

        TaskScheduleRequest tsr = new TaskScheduleRequest();

        Map<String, VariableValue> varVals;
        try {
            varVals = nodeRun.threadRun.assignVarsForNode(node.taskNode);
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

        tsr.wfRunEventQueue = nodeRun.threadRun.wfRun.cmdDao.getWfRunEventQueue();
        tsr.taskDefId = node.taskNode.taskDefName;
        tsr.taskDefName = node.taskNode.taskDefName;
        tsr.taskRunNumber = nodeRun.number;
        tsr.taskRunPosition = nodeRun.position;
        tsr.threadRunNumber = nodeRun.threadRunNumber;
        tsr.wfRunId = nodeRun.threadRun.wfRunId;
        tsr.wfSpecId = nodeRun.threadRun.wfSpecName;
        tsr.nodeName = node.name;
        tsr.variables = varVals;

        nodeRun.threadRun.wfRun.cmdDao.scheduleTask(tsr);
    }

    public void processStartedEvent(TaskStartedEvent se) {
        ThreadRun thread = nodeRun.threadRun;

        if (nodeRun.position != se.taskRunPosition) {
            // Out-of-order event due to race conditions between task worker
            // transactional producer and regular producer
            return;
        }
        nodeRun.status = LHStatusPb.RUNNING;
        Node node = nodeRun.getNode();

        TaskResultEvent taskResult = new TaskResultEvent();
        taskResult.resultCode = TaskResultCodePb.TIMEOUT;
        taskResult.taskRunNumber = nodeRun.number;
        taskResult.taskRunPosition = nodeRun.position;
        taskResult.threadRunNumber = nodeRun.threadRunNumber;

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
        timer.payload = taskResult.toProto().build().toByteArray();
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
                            TaskResultCodePb.TIMEOUT,
                            "Task Failed: " + ce.resultCode,
                            LHConstants.TIMEOUT,
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
    }
}
