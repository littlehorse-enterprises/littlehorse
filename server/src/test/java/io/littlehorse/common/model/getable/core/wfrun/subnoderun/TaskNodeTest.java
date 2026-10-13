package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.server.AbstractWorkflowExecutionTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskNodeTest extends AbstractWorkflowExecutionTest {
    @BeforeEach
    void registerTaskDefinition() {
        TaskDefModel task = TestUtil.taskDef("greet");
        task.inputVars.add(VariableDefModel.fromProto(
                VariableDef.newBuilder()
                        .setName("name")
                        .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                        .build(),
                readContext()));
        seedMetadata(task);
    }

    @Test
    void resolvesInputsAndCompletesWithTaskOutput() {
        WfRun run = startWorkflow(
                thread -> {
                    WfRunVariable name = thread.declareStr("name");
                    thread.complete(thread.execute("greet", name));
                },
                Map.of("name", str("Ada")));

        NodeRunModel node = currentNode(run);
        TaskRunModel task = currentTask(run);
        assertThat(node.getStatus()).isEqualTo(LHStatus.RUNNING);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TASK_SCHEDULED);
        assertThat(task.getTaskDefId().getName()).isEqualTo("greet");
        assertThat(task.toProto().getInputVariablesList())
                .containsExactly(VarNameAndVal.newBuilder()
                        .setVarName("name")
                        .setValue(str("Ada"))
                        .build());
        assertThat(readContext().getableManager().getScheduledTask(task.getId()))
                .isNotNull();

        assertThat(claimTask(task).hasResult()).isTrue();
        assertThat(readGetable(task.getId()).getStatus()).isEqualTo(TaskStatus.TASK_RUNNING);
        assertThat(readContext().getableManager().getScheduledTask(task.getId()))
                .isNull();
        reportTask(task, TaskStatus.TASK_SUCCESS, str("Hello Ada"));

        WfRun.Builder completed =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(completed.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(completed.getThreadRuns(0).getOutput()).isEqualTo(str("Hello Ada"));
        assertThat(readGetable(node.getId()).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(readGetable(task.getId()).getStatus()).isEqualTo(TaskStatus.TASK_SUCCESS);
    }

    @Test
    void passesOutputToTheNextTask() {
        WfRun run = startWorkflow(
                thread -> {
                    TaskNodeOutput first = thread.execute("greet", "Ada");
                    thread.complete(thread.execute("greet", first));
                },
                Map.of());
        TaskRunModel first = currentTask(run);
        claimTask(first);
        reportTask(first, TaskStatus.TASK_SUCCESS, str("Grace"));

        TaskRunModel second = currentTask(run);
        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(second.getStatus()).isEqualTo(TaskStatus.TASK_SCHEDULED);
        assertThat(second.toProto().getInputVariables(0).getValue()).isEqualTo(str("Grace"));
        assertThat(readGetable(new WfRunIdModel(run.getId().getId())).getStatus())
                .isEqualTo(LHStatus.RUNNING);
        claimTask(second);
        reportTask(second, TaskStatus.TASK_SUCCESS, str("Hello Grace"));

        WfRun.Builder completed =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(completed.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(completed.getThreadRuns(0).getOutput()).isEqualTo(str("Hello Grace"));
    }

    @Test
    void failsWhenTaskOutputDoesNotMatchDeclaredType() {
        TaskDefModel definition = TestUtil.taskDef("expects-int");
        definition.setReturnType(new ReturnTypeModel(VariableType.INT));
        seedMetadata(definition);
        WfRun run = startWorkflow(thread -> thread.execute("expects-int"), Map.of());
        NodeRunModel node = currentNode(run);
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTask(task, TaskStatus.TASK_SUCCESS, str("not-an-int"));

        assertThat(readGetable(task.getId()).getStatus()).isEqualTo(TaskStatus.TASK_OUTPUT_SERDE_ERROR);
        assertThat(readGetable(node.getId()).getStatus()).isEqualTo(LHStatus.ERROR);
        assertThat(readGetable(new WfRunIdModel(run.getId().getId())).getStatus())
                .isEqualTo(LHStatus.ERROR);
    }

    private static VariableValue str(String value) {
        return VariableValue.newBuilder().setStr(value).build();
    }

    @Test
    void failsWorkflowOnUnhandledTaskError() {
        WfRun run = startWorkflow(thread -> thread.execute("greet", "Ada"), Map.of());
        NodeRunModel node = currentNode(run);
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTaskError(task, taskError());

        TaskRun.Builder failedTask = readGetable(task.getId()).toProto();
        assertThat(failedTask.getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
        assertThat(failedTask.getAttemptsCount()).isEqualTo(1);
        assertThat(failedTask.getAttempts(0).getError()).isEqualTo(taskError());
        assertThat(readGetable(node.getId()).getStatus()).isEqualTo(LHStatus.ERROR);
        WfRun.Builder failedRun =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(failedRun.getStatus()).isEqualTo(LHStatus.ERROR);
        assertThat(failedRun.getThreadRunsCount()).isEqualTo(1);
        assertThat(readContext().getableManager().getScheduledTask(task.getId()))
                .isNull();
    }

    @Test
    void recoversOnRetryWithoutRunningErrorHandler() {
        WfRun run = startWorkflow(
                thread -> {
                    TaskNodeOutput output = thread.execute("greet", "Ada").withRetries(1);
                    thread.handleError(output, handler -> handler.execute("greet", "unexpected-handler"));
                    thread.complete(output);
                },
                Map.of());
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTaskError(task, taskError());

        TaskRunModel retry = readGetable(task.getId());
        assertThat(retry.toProto().getAttemptsCount()).isEqualTo(2);
        assertThat(retry.toProto().getAttempts(0).getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
        assertThat(retry.toProto().getAttempts(1).getStatus()).isEqualTo(TaskStatus.TASK_SCHEDULED);
        assertThat(readGetable(new WfRunIdModel(run.getId().getId())).toProto().getThreadRunsCount())
                .isEqualTo(1);
        assertThat(claimTask(retry).hasResult()).isTrue();
        reportTask(retry, TaskStatus.TASK_SUCCESS, str("Hello Ada"));

        WfRun.Builder completed =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(completed.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(completed.getThreadRuns(0).getOutput()).isEqualTo(str("Hello Ada"));
        assertThat(completed.getThreadRunsCount()).isEqualTo(1);
        assertThat(readGetable(task.getId()).toProto().getAttemptsList())
                .extracting(TaskAttempt::getStatus)
                .containsExactly(TaskStatus.TASK_FAILED, TaskStatus.TASK_SUCCESS);
    }

    @Test
    void runsErrorHandlerOnlyAfterRetriesAreExhausted() {
        WfRun run = startWorkflow(
                thread -> {
                    WfRunVariable result = thread.declareStr("result").withDefault("not-handled");
                    TaskNodeOutput output = thread.execute("greet", "Ada").withRetries(1);
                    thread.handleError(
                            output,
                            LHErrorType.TASK_ERROR,
                            handler -> handler.mutate(result, VariableMutationType.ASSIGN, "recovered"));
                    thread.complete(result);
                },
                Map.of());
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTaskError(task, taskError());

        WfRun.Builder pending =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(pending.getStatus()).isEqualTo(LHStatus.RUNNING);
        assertThat(pending.getThreadRunsCount()).isEqualTo(1);
        TaskRunModel retry = readGetable(task.getId());
        assertThat(claimTask(retry).hasResult()).isTrue();
        reportTaskError(retry, taskError());

        WfRun.Builder completed =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(completed.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(completed.getThreadRuns(0).getOutput()).isEqualTo(str("recovered"));
        assertThat(completed.getThreadRunsCount()).isEqualTo(2);
        assertThat(completed.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(readGetable(task.getId()).getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
        assertThat(readGetable(task.getId()).toProto().getAttemptsList())
                .extracting(TaskAttempt::getStatus)
                .containsExactly(TaskStatus.TASK_FAILED, TaskStatus.TASK_FAILED);
        assertThat(readContext().getableManager().getScheduledTask(task.getId()))
                .isNull();
    }

    @Test
    void handlesMatchingBusinessExceptionWithoutRetrying() {
        WfRun run = startWorkflow(
                thread -> {
                    WfRunVariable result = thread.declareStr("result").withDefault("not-handled");
                    TaskNodeOutput output = thread.execute("greet", "Ada").withRetries(2);
                    thread.handleException(
                            output,
                            "greeting-denied",
                            handler -> handler.mutate(result, VariableMutationType.ASSIGN, "denied"));
                    thread.complete(result);
                },
                Map.of());
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTaskException(task, businessException());

        TaskRun.Builder failedTask = readGetable(task.getId()).toProto();
        assertThat(failedTask.getStatus()).isEqualTo(TaskStatus.TASK_EXCEPTION);
        assertThat(failedTask.getAttemptsCount()).isEqualTo(1);
        assertThat(failedTask.getAttempts(0).getException()).isEqualTo(businessException());
        WfRun.Builder completed =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(completed.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(completed.getThreadRuns(0).getOutput()).isEqualTo(str("denied"));
        assertThat(completed.getThreadRunsCount()).isEqualTo(2);
        assertThat(completed.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.COMPLETED);
    }

    @Test
    void failsWhenNoHandlerMatchesBusinessException() {
        WfRun run = startWorkflow(
                thread -> {
                    TaskNodeOutput output = thread.execute("greet", "Ada");
                    thread.handleException(output, "different-exception", handler -> {});
                    thread.handleError(output, handler -> {});
                },
                Map.of());
        NodeRunModel node = currentNode(run);
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTaskException(task, businessException());

        assertThat(readGetable(task.getId()).getStatus()).isEqualTo(TaskStatus.TASK_EXCEPTION);
        assertThat(readGetable(node.getId()).getStatus()).isEqualTo(LHStatus.EXCEPTION);
        WfRun.Builder failedRun =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(failedRun.getStatus()).isEqualTo(LHStatus.EXCEPTION);
        assertThat(failedRun.getThreadRunsCount()).isEqualTo(1);
    }

    private static LHTaskError taskError() {
        return LHTaskError.newBuilder()
                .setType(LHErrorType.TASK_ERROR)
                .setMessage("Worker unavailable")
                .build();
    }

    private static LHTaskException businessException() {
        return LHTaskException.newBuilder()
                .setName("greeting-denied")
                .setMessage("Recipient opted out")
                .setContent(str("Ada"))
                .build();
    }

    @Test
    void waitsForPendingTaskWhenWorkflowIsStopped() {
        WfRun run = startWorkflow(thread -> thread.execute("greet", "Ada"), Map.of());
        processWithoutResponse(
                run.getId().getId(),
                command -> command.setStopWfRun(
                        StopWfRunRequest.newBuilder().setWfRunId(run.getId()).build()));
        ThreadRun thread =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto().getThreadRuns(0);
        assertThat(thread.getStatus()).isEqualTo(LHStatus.HALTING);
        assertThat(thread.getHaltReasons(0).hasManualHalt()).isTrue();
        assertThat(currentTask(run).getStatus()).isEqualTo(TaskStatus.TASK_SCHEDULED);
    }
}
