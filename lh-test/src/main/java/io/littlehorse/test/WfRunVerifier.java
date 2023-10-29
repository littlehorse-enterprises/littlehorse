package io.littlehorse.test;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.TestContext;
import io.littlehorse.test.internal.step.SearchStep;
import io.littlehorse.test.internal.step.SendExternalEventStep;
import io.littlehorse.test.internal.step.VerifyNodeRunStep;
import io.littlehorse.test.internal.step.VerifyTaskExecution;
import io.littlehorse.test.internal.step.VerifyVariableStep;
import io.littlehorse.test.internal.step.VerifyWfRunStep;
import io.littlehorse.test.internal.step.WaitForStatusStep;
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

    public WfRunVerifier thenVerifyAllTaskRuns(int threadRunNumber, Consumer<List<TaskRun>> verifier) {
        return this;
    }

    public WfRunVerifier thenVerifyWfRun(Consumer<WfRun> wfRunMatcher) {
        steps.add(new VerifyWfRunStep(wfRunMatcher, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenSendExternalEventJsonContent(String externalEventName, Object content) {
        try {
            String json = LHLibUtil.serializeToJson(content);
            VariableValue externalEventContent = VariableValue.newBuilder()
                    .setType(VariableType.JSON_OBJ)
                    .setJsonObj(json)
                    .build();
            steps.add(new SendExternalEventStep(externalEventName, externalEventContent, steps.size() + 1));
        } catch (JsonProcessingException e) {
            throw new LHTestInitializationException(e);
        }
        return this;
    }

    public WfRunVerifier waitForStatus(LHStatus status) {
        Function<Object, LHStatus> objectLHStatusFunction =
                context -> lhClient.getWfRun(wfRunIdFrom(context.toString())).getStatus();
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, status, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForUserTaskRunStatus(
            int threadRunNumber, int nodeRunNumber, UserTaskRunStatus status, Duration timeout) {
        Function<Object, UserTaskRunStatus> objectUserTaskRunStatusFunction = context -> {
            String wfRunId = context.toString();
            NodeRun nodeRun = lhClient.getNodeRun(nodeRunIdFrom(wfRunId, threadRunNumber, nodeRunNumber));
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
        Function<Object, LHStatus> objectLHStatusFunction = context -> {
            String wfRunId = context.toString();
            return lhClient.getNodeRun(nodeRunIdFrom(wfRunId, threadRunNumber, nodeRunNumber))
                    .getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, status, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForThreadRunStatus(int threadRunNumber, LHStatus threadRunStatus) {
        Function<Object, LHStatus> objectLHStatusFunction = context -> {
            ThreadRun threadRun =
                    lhClient.getWfRun(wfRunIdFrom(context.toString())).getThreadRuns(threadRunNumber);
            return threadRun.getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, threadRunStatus, steps.size() + 1));
        return this;
    }

    public WfRunVerifier thenVerifyNodeRun(int threadRunNumber, int nodeRunNumber, Consumer<NodeRun> matcher) {
        steps.add(new VerifyNodeRunStep(threadRunNumber, nodeRunNumber, matcher, steps.size() + 1));
        return this;
    }

    public WfRunVerifier waitForTaskStatus(int threadRunNumber, int nodeRunNumber, TaskStatus taskStatus) {
        Function<Object, TaskStatus> objectLHStatusFunction = context -> {
            NodeRun nodeRun = lhClient.getNodeRun(nodeRunIdFrom(context.toString(), threadRunNumber, nodeRunNumber));
            TaskRun taskRun = lhClient.getTaskRun(nodeRun.getTask().getTaskRunId());
            return taskRun.getStatus();
        };
        steps.add(new WaitForStatusStep<>(objectLHStatusFunction, taskStatus, steps.size() + 1));
        return this;
    }

    private WfRunId wfRunIdFrom(String wfRunId) {
        return WfRunId.newBuilder().setId(wfRunId).build();
    }

    private NodeRunId nodeRunIdFrom(String wfRunId, int threadRunNumber, int nodeRunNumber) {
        return NodeRunId.newBuilder()
                .setWfRunId(wfRunId)
                .setThreadRunNumber(threadRunNumber)
                .setPosition(nodeRunNumber)
                .build();
    }

    public <I, O> WfRunVerifier doSearch(
            Class<I> requestType, CapturedResult<O> capture, Function<WfRunTestContext, I> buildId) {
        steps.add(new SearchStep<>(requestType, buildId, capture));
        return this;
    }
}
