package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.internal.step.VerifyTaskExecution;
import io.littlehorse.test.internal.step.VerifyVariableStep;
import io.littlehorse.test.internal.step.WaitForStatusStep;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class WfRunVerifier extends AbstractVerifier {

    public WfRunVerifier(LHClient lhClient, Workflow workflow, Collection<Arg> workflowArgs) {
        super(new LHClientTestWrapper(lhClient), workflow, workflowArgs);
    }

    public WfRunVerifier thenVerifyTaskRun(int threadRunNumber, int nodeRunNumber, Consumer<TaskRun> matcher) {
        steps.add(new VerifyTaskExecution(threadRunNumber, nodeRunNumber, matcher));
        return this;
    }

    public WfRunVerifier thenVerifyVariable(
            int threadRunNumber, String variableName, Consumer<VariableValue> expectedValueMatcher) {
        steps.add(new VerifyVariableStep(threadRunNumber, variableName, expectedValueMatcher));
        return this;
    }

    public WfRunVerifier thenVerifyTaskRunResult(
            int threadRunNumber, int nodeRunNumber, Consumer<VariableValue> expectedOutput) {
        Consumer<TaskRun> taskRunConsumer = taskRun -> {
            TaskAttempt completedTask = taskRun.getAttemptsList().stream()
                    .filter(taskAttempt -> taskAttempt.getStatus().equals(TaskStatus.TASK_SUCCESS))
                    .findFirst()
                    .orElse(null);
            VariableValue actualOutput = null;
            if (completedTask != null) {
                actualOutput = completedTask.getOutput();
            }
            expectedOutput.accept(actualOutput);
        };
        steps.add(new VerifyTaskExecution(threadRunNumber, nodeRunNumber, taskRunConsumer));
        return this;
    }

    public WfRunVerifier waitForStatus(LHStatus lhStatus) {
        Function<Object, LHStatus> objectLHStatusFunction = context -> {
            String wfRunId = context.toString();
            return lhClientTestWrapper.getWfRunStatus(wfRunId);
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, lhStatus));
        return this;
    }

    public WfRunVerifier waitForTaskStatus(int threadRunNumber, int nodeRunNumber, TaskStatus taskStatus) {
        Function<Object, TaskStatus> objectLHStatusFunction = context -> {
            NodeRun nodeRun = lhClientTestWrapper.getNodeRun(context.toString(), threadRunNumber, nodeRunNumber);
            TaskRun taskRun = lhClientTestWrapper.getTaskRun(nodeRun.getTask().getTaskRunId());
            return taskRun.getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, taskStatus));
        return this;
    }
}
