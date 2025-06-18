package io.littlehorse.test;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.internal.TestContext;
import io.littlehorse.test.internal.TestExecutionContext;
import io.littlehorse.test.internal.step.*;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class WfRunVerifier extends AbstractVerifier {

    public WfRunVerifier(TestContext context, Workflow workflow, Collection<Arg> workflowArgs) {
        super(context, workflow, workflowArgs);
    }

    public WfRunVerifier thenVerifyTaskRun(int threadRunNumber, int nodeRunNumber, Consumer<TaskRun> matcher) {
        steps.add(new VerifyTaskExecution(threadRunNumber, nodeRunNumber, matcher, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenVerifyVariable(
            int threadRunNumber, String variableName, Consumer<VariableValue> expectedValueMatcher) {
        steps.add(new VerifyVariableStep(threadRunNumber, variableName, expectedValueMatcher, steps.size() + 1));
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
        steps.add(new VerifyTaskExecution(threadRunNumber, nodeRunNumber, taskRunConsumer, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenVerifyAllTaskRuns(Consumer<List<TaskRun>> verifier) {
        steps.add(new VerifyAllTaskRunsStep(verifier, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenVerifyWfRun(Consumer<WfRun> wfRunMatcher) {
        steps.add(new VerifyWfRunStep(wfRunMatcher, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenSendCorrelatedEvent(String externalEventName, String key, Object content) {
        VariableValue contentVal = LHLibUtil.objToVarVal(content);
        steps.add(new SendCorrelatedEventStep(externalEventName, key, contentVal, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenSendExternalEventWithContent(String externalEventName, Object content) {
        VariableValue externalEventContent = LHLibUtil.objToVarVal(content);
        steps.add(new SendExternalEventStep(externalEventName, externalEventContent, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenAwaitWorkflowEvent(String workflowEventDefName, Consumer<WorkflowEvent> verifier) {
        steps.add(new AwaitWorkflowEventStep(workflowEventDefName, verifier, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenRescueThreadRun(int threadRunNumber, boolean skipCurrentNode) {
        steps.add(new RescueThreadRunStep(threadRunNumber, skipCurrentNode, null, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenRescueThreadRun(
            int threadRunNumber, boolean skipCurrentNode, Consumer<StatusRuntimeException> exceptionConsumer) {
        steps.add(new RescueThreadRunStep(threadRunNumber, skipCurrentNode, exceptionConsumer, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForStatus(LHStatus status) {
        return this.waitForStatus(status, null);
    }

    public WfRunVerifier waitForStatus(LHStatus status, Duration timeout) {
        Function<TestExecutionContext, LHStatus> objectLHStatusFunction =
                context -> lhClient.getWfRun(context.getWfRunId()).getStatus();
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, status, timeout, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForUserTaskRunStatus(
            int threadRunNumber, int nodeRunNumber, UserTaskRunStatus status, Duration timeout) {
        Function<TestExecutionContext, UserTaskRunStatus> objectUserTaskRunStatusFunction = context -> {
            NodeRun nodeRun = lhClient.getNodeRun(nodeRunIdFrom(context.getWfRunId(), threadRunNumber, nodeRunNumber));
            if (nodeRun != null && nodeRun.hasUserTask()) {
                UserTaskRunId userTaskRunId = nodeRun.getUserTask().getUserTaskRunId();
                UserTaskRun userTaskRun = lhClient.getUserTaskRun(userTaskRunId);
                return userTaskRun.getStatus();
            }
            return null;
        };
        steps.add(new WaitForStatusStep<>(objectUserTaskRunStatusFunction, status, timeout, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForUserTaskRunStatus(int threadRunNumber, int nodeRunNumber, UserTaskRunStatus status) {
        return this.waitForUserTaskRunStatus(threadRunNumber, nodeRunNumber, status, null);
    }

    public WfRunVerifier waitForNodeRunStatus(int threadRunNumber, int nodeRunNumber, LHStatus status) {
        return this.waitForNodeRunStatus(threadRunNumber, nodeRunNumber, status, null);
    }

    public WfRunVerifier waitForNodeRunStatus(
            int threadRunNumber, int nodeRunNumber, LHStatus status, Duration timeout) {
        Function<TestExecutionContext, LHStatus> objectLHStatusFunction = context -> {
            return lhClient.getNodeRun(nodeRunIdFrom(context.getWfRunId(), threadRunNumber, nodeRunNumber))
                    .getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, status, timeout, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForThreadRunStatus(int threadRunNumber, LHStatus threadRunStatus) {
        Function<TestExecutionContext, LHStatus> objectLHStatusFunction = context -> {
            ThreadRun threadRun = lhClient.getWfRun(context.getWfRunId()).getThreadRuns(threadRunNumber);
            return threadRun.getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, threadRunStatus, steps.size() + 1));
        return this;
    }

    /**
     * Accepts a list of objects. Checks every TaskRun in the WfRun and verifies that
     * the ordered list of TaskRun outputs matches the
     * @param taskOutputs
     * @return
     */
    public WfRunVerifier verifyAllTaskRunOutputs(List<Object> taskOutputs) {
        steps.add(new VerifyTaskRunOutputsStep(steps.size() + 1, taskOutputs));
        return this;
    }

    public WfRunVerifier thenVerifyNodeRun(int threadRunNumber, int nodeRunNumber, Consumer<NodeRun> matcher) {
        steps.add(new VerifyNodeRunStep(threadRunNumber, nodeRunNumber, matcher, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenVerifyLastNodeRun(int threadRunNumber, Consumer<NodeRun> matcher) {
        steps.add(new VerifyLastNodeRunStep(threadRunNumber, matcher, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForTaskStatus(int threadRunNumber, int nodeRunNumber, TaskStatus taskStatus) {
        Function<TestExecutionContext, TaskStatus> objectLHStatusFunction = context -> {
            NodeRun nodeRun = lhClient.getNodeRun(nodeRunIdFrom(context.getWfRunId(), threadRunNumber, nodeRunNumber));
            TaskRun taskRun = lhClient.getTaskRun(nodeRun.getTask().getTaskRunId());
            return taskRun.getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, taskStatus, steps.size() + 1));
        return this;
    }

    private NodeRunId nodeRunIdFrom(WfRunId wfRunId, int threadRunNumber, int nodeRunNumber) {
        return NodeRunId.newBuilder()
                .setWfRunId(wfRunId)
                .setThreadRunNumber(threadRunNumber)
                .setPosition(nodeRunNumber)
                .build();
    }

    public <I, O> WfRunVerifier doSearch(
            Class<I> requestType, CapturedResult<O> capture, Function<TestExecutionContext, I> buildId) {
        steps.add(new SearchStep<>(requestType, buildId, capture));
        return this;
    }

    public WfRunVerifier thenAssignUserTask(
            int threadRunNumber, int nodeRunNumber, boolean overrideClaim, String userId, String groupId) {
        steps.add(new AssignUserTask(steps.size() + 1, threadRunNumber, nodeRunNumber, overrideClaim, userId, groupId));
        return this;
    }

    public WfRunVerifier thenCancelUserTaskRun(int threadRunNumber, int nodeRunNumber) {
        steps.add(new CancelUserTaskRun(steps.size() + 1, threadRunNumber, nodeRunNumber));
        return this;
    }

    public WfRunVerifier thenCommentUserTaskRun(int threadRunNumber, int nodeRunNumber, String userId, String comment) {
        steps.add(new UserTaskRunCommentStep(steps.size() + 1, threadRunNumber, nodeRunNumber, userId, comment));
        return this;
    }

    public WfRunVerifier thenDeleteCommentUserTaskRun(
            int threadNumber, int nodeRunNumber, int commentId, String userId) {
        steps.add(new DeleteUserTaskRunCommentStep(steps.size() + 1, threadNumber, nodeRunNumber, commentId, userId));
        return this;
    }

    public WfRunVerifier thenEditComment(
            int threadNumber, int nodeRunNumber, String userId, String comment, int commentId) {
        steps.add(new EditUserTaskRunCommentStep(
                steps.size() + 1, threadNumber, nodeRunNumber, userId, comment, commentId));
        return this;
    }
}
