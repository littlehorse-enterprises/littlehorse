package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.awaitility.Awaitility;

public class WfRunVerifier extends AbstractVerifier {

    public WfRunVerifier(LHClient lhClient, Workflow workflow, Collection<Arg> workflowArgs) {
        super(new LHClientTestWrapper(lhClient), workflow, workflowArgs);
    }

    public WfRunVerifier thenVerifyTaskRun(int threadRunNumber, int nodeRunNumber, Consumer<TaskRun> matcher) {
        steps.add(new VerifyTaskExecution(threadRunNumber, nodeRunNumber, matcher));
        return this;
    }

    public WfRunVerifier thenVerifyTaskRunResult(
            int threadRunNumber, int nodeRunNumber, Consumer<VariableValue> expectedOutput) {
        Consumer<TaskRun> taskRunConsumer = taskRun -> {
            TaskAttempt taskAttempt1 = taskRun.getAttemptsList().stream()
                    .filter(taskAttempt -> taskAttempt.getStatus().equals(TaskStatus.TASK_SUCCESS))
                    .findFirst()
                    .orElse(null);
            VariableValue actualOutput = null;
            if (taskAttempt1 != null) {
                actualOutput = taskAttempt1.getOutput();
            }
            expectedOutput.accept(actualOutput);
        };
        steps.add(new VerifyTaskExecution(threadRunNumber, nodeRunNumber, taskRunConsumer));
        return this;
    }

    public WfRunVerifier waitForStatus(LHStatus lhStatus) {
        Function<Object, LHStatus> objectLHStatusFunction = o -> {
            String wfRunId = o.toString();
            return lhClientTestWrapper.getWfRunStatus(wfRunId);
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, lhStatus));
        return this;
    }

    public WfRunVerifier waitForTaskStatus(int threadRunNumber, int nodeRunNumber, TaskStatus taskStatus) {
        Function<Object, TaskStatus> objectLHStatusFunction = o -> {
            NodeRun nodeRun = lhClientTestWrapper.getNodeRun(o.toString(), threadRunNumber, nodeRunNumber);
            TaskRun taskRun = lhClientTestWrapper.getTaskRun(nodeRun.getTask().getTaskRunId());
            System.out.println(taskRun.getStatus());
            return taskRun.getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, taskStatus));
        return this;
    }

    public interface Step {
        void execute(Object context);
    }

    private class WaitForStatusStep<V> implements Step {

        private final Function<Object, V> function;
        private final V expectedStatus;

        WaitForStatusStep(Function<Object, V> function, V expectedStatus) {
            this.function = function;
            this.expectedStatus = expectedStatus;
        }

        @Override
        public void execute(Object context) {
            Awaitility.await()
                    .until(() -> function.apply(context), currentStatus -> currentStatus.equals(expectedStatus));
        }
    }

    class VerifyTaskExecution implements Step {

        private final int threadRunNumber;
        private final int nodeRunNumber;
        private final Consumer<TaskRun> matcher;

        public VerifyTaskExecution(int threadRunNumber, int nodeRunNumber, Consumer<TaskRun> matcher) {
            this.threadRunNumber = threadRunNumber;
            this.nodeRunNumber = nodeRunNumber;
            this.matcher = matcher;
        }

        @Override
        public void execute(Object context) {
            String wfRunId = (String) context;
            NodeRun nodeRun = Awaitility.await()
                    .until(
                            () -> lhClientTestWrapper.getNodeRun(wfRunId, threadRunNumber, nodeRunNumber),
                            Objects::nonNull);
            TaskRunId taskRunId = nodeRun.getTask().getTaskRunId();
            TaskRun taskRun =
                    Awaitility.await().until(() -> lhClientTestWrapper.getTaskRun(taskRunId), Objects::nonNull);
            matcher.accept(taskRun);
        }
    }
}
