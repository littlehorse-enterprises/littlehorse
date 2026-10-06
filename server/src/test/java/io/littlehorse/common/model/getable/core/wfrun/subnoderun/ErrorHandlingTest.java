package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.wfsdk.*;
import io.littlehorse.server.AbstractWorkflowExecutionTest;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErrorHandlingTest extends AbstractWorkflowExecutionTest {
    private static final String EXCEPTION = "payment-rejected";
    private static final VariableValue CONTENT =
            VariableValue.newBuilder().setStr("payment-123").build();

    enum FailureKind {
        ERROR(LHStatus.ERROR, "TASK_FAILURE"),
        EXCEPTION(LHStatus.EXCEPTION, ErrorHandlingTest.EXCEPTION);

        final LHStatus status;
        final String name;

        FailureKind(LHStatus status, String name) {
            this.status = status;
            this.name = name;
        }
    }

    @BeforeEach
    void registerTasks() {
        seedMetadata(TestUtil.taskDef("work"));
        seedMetadata(TestUtil.taskDef("handler"));
        TaskDefModel typed = TestUtil.taskDef("typed-task");
        typed.inputVars.add(VariableDefModel.fromProto(
                VariableDef.newBuilder()
                        .setName("input")
                        .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                        .build(),
                readContext()));
        typed.setReturnType(new ReturnTypeModel(VariableType.INT));
        seedMetadata(typed);
    }

    @Test
    void specificExceptionHandlerRunsForMatchingName() {
        ThreadFunc workflow = thread -> {
            WfRunVariable result = thread.declareStr("result").withDefault("no-handler-ran");
            TaskNodeOutput work = thread.execute("work");
            thread.handleException(
                    work,
                    "other-exception",
                    recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "other-exception"));
            thread.handleException(
                    work,
                    EXCEPTION,
                    recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "specific-exception"));
            thread.handleException(
                    work, recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "any-exception"));
            thread.handleError(
                    work,
                    LHErrorType.TASK_FAILURE,
                    recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "task-error"));
            thread.handleError(work, recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "any-error"));
            thread.handleAnyFailure(
                    work, recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "any-failure"));
            thread.complete(result);
        };
        Supplier<WfRun> newRun = () -> startWorkflow(workflow, Map.of());
        WfRun run = newRun.get();
        failTask(currentTask(reload(run)), TaskStatus.TASK_FAILED, LHErrorType.TASK_FAILURE);
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("task-error");

        run = newRun.get();
        failTask(currentTask(reload(run)), TaskStatus.TASK_TIMEOUT, LHErrorType.TIMEOUT);
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("any-error");

        run = newRun.get();
        failTask(currentTask(reload(run)), FailureKind.EXCEPTION);
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("specific-exception");

        run = newRun.get();
        failTaskWithException(currentTask(reload(run)), "other-exception");
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("other-exception");

        run = newRun.get();
        failTaskWithException(currentTask(reload(run)), "unmatched-exception");
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("any-exception");
    }

    @Test
    void earlierHandleAnyFailureTakesPrecedenceOverLaterSpecificHandlers() {
        ThreadFunc workflow = thread -> {
            WfRunVariable result = thread.declareStr("result").withDefault("none");
            TaskNodeOutput work = thread.execute("work");
            thread.handleAnyFailure(work, recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "first"));
            thread.handleError(
                    work,
                    LHErrorType.TASK_FAILURE,
                    recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "later-error-handler"));
            thread.handleException(
                    work,
                    EXCEPTION,
                    recovery -> recovery.mutate(result, VariableMutationType.ASSIGN, "later-exception-handler"));
            thread.complete(result);
        };
        Supplier<WfRun> newRun = () -> startWorkflow(workflow, Map.of());

        WfRun run = newRun.get();
        failTask(currentTask(run), FailureKind.ERROR);
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("first");
        assertThat(reload(run).getThreadRunsCount()).isEqualTo(2);

        run = newRun.get();
        failTask(currentTask(run), FailureKind.EXCEPTION);
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("first");
        assertThat(reload(run).getThreadRunsCount()).isEqualTo(2);
    }

    @Test
    void passesExceptionContentIntoHandler() {
        WfRun run = startWorkflow(
                thread -> {
                    WfRunVariable result = thread.declareStr("result");
                    TaskNodeOutput work = thread.execute("work");
                    thread.handleException(work, EXCEPTION, recovery -> {
                        WfRunVariable input = recovery.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
                        recovery.mutate(result, VariableMutationType.ASSIGN, input);
                    });
                    thread.complete(result);
                },
                Map.of());
        failTask(currentTask(run), FailureKind.EXCEPTION);
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput()).isEqualTo(CONTENT);
    }

    @Test
    void ancestorReceivesReplacementExceptionFromPerChildHandler() {
        String replacementName = "recovery-rejected";
        String replacementMessage = "Recovery could not proceed";
        VariableValue replacementContent =
                VariableValue.newBuilder().setStr("recovery-456").build();
        LHTaskException replacementException = LHTaskException.newBuilder()
                .setName(replacementName)
                .setMessage(replacementMessage)
                .setContent(replacementContent)
                .build();

        ThreadFunc workflow = root -> {
            WfRunVariable result = root.declareStr("result");
            SpawnedThread child = root.spawnThread(
                    leaf -> {
                        TaskNodeOutput work = leaf.execute("work");
                        leaf.handleException(work, "caught-exception", recovery -> recovery.execute("handler"));
                    },
                    "leaf",
                    Map.of());
            WaitForThreadsNodeOutput wait = root.waitForThreads(SpawnedThreads.of(child));
            wait.handleExceptionOnChild("uncaught-exception", recovery -> recovery.execute("handler"));
            root.handleException(wait, replacementName, recovery -> {
                WfRunVariable input = recovery.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
                recovery.mutate(result, VariableMutationType.ASSIGN, input);
            });
            root.complete(result);
        };

        WfRun run = startWorkflow(workflow, Map.of());
        NodeRunModel waitForThreadNode = currentNode(run);
        NodeRunModel originalNode = node(run, thread(run, "leaf"));
        failTaskWithException(task(run, thread(run, "leaf")), "uncaught-exception");
        TaskRunModel uncaughtExceptionHandlerTask = task(run, reload(run).getThreadRuns(2));
        failTaskWithException(uncaughtExceptionHandlerTask, replacementException);

        Failure propagated = readGetable(waitForThreadNode.getId()).toProto().getFailures(0);
        assertThat(propagated.getFailureName()).isEqualTo(replacementName);
        assertThat(propagated.getMessage()).isEqualTo(replacementMessage);
        assertThat(propagated.getContent()).isEqualTo(replacementContent);
        assertThat(propagated.getWasProperlyHandled()).isTrue();
        assertThat(readGetable(originalNode.getId()).toProto().getFailures(0).getFailureName())
                .isEqualTo("uncaught-exception");
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput()).isEqualTo(replacementContent);
        assertThat(reload(run).getThreadRunsCount()).isEqualTo(4);
    }

    @Test
    void ancestorReceivesReplacementExceptionFromNodeHandler() {
        String replacementName = "recovery-rejected";
        String replacementMessage = "Recovery could not proceed";
        VariableValue replacementContent =
                VariableValue.newBuilder().setStr("recovery-456").build();
        LHTaskException replacementException = LHTaskException.newBuilder()
                .setName(replacementName)
                .setMessage(replacementMessage)
                .setContent(replacementContent)
                .build();

        WfRun run = startWorkflow(
                root -> {
                    WfRunVariable result = root.declareStr("result");
                    SpawnedThread child = root.spawnThread(
                            leaf -> {
                                TaskNodeOutput work = leaf.execute("work");
                                leaf.handleException(work, "caught-exception", recovery -> recovery.execute("handler"));
                            },
                            "leaf",
                            Map.of());
                    WaitForThreadsNodeOutput wait = root.waitForThreads(SpawnedThreads.of(child));
                    root.handleException(wait, replacementName, recovery -> result.assign("handled-with-root-handler"));
                    root.complete(result);
                },
                Map.of());

        failTaskWithException(task(run, thread(run, "leaf")), "caught-exception");
        TaskRunModel recoveryTask = task(run, reload(run).getThreadRuns(2));
        failTaskWithException(recoveryTask, replacementException);

        NodeRunModel waitForThreadNode = currentNode(reload(run));
        assertThat(waitForThreadNode.getFailures()).hasSize(1);
        FailureModel failure = waitForThreadNode.getFailures().getFirst();
        assertThat(failure.isProperlyHandled()).isTrue();
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRuns(0).getOutput().getStr()).isEqualTo("handled-with-root-handler");
        assertThat(failure.getFailureName()).isEqualTo(replacementName);
    }

    @Test
    void perChildHandlerRejectsMissingRequiredExceptionContent() {
        LHTaskException declinedWithoutContent = LHTaskException.newBuilder()
                .setName(EXCEPTION)
                .setMessage("Declined")
                .build();
        WfRun run = startWorkflow(
                root -> {
                    SpawnedThread child = root.spawnThread(leaf -> leaf.execute("work"), "leaf", Map.of());
                    root.waitForThreads(SpawnedThreads.of(child)).handleExceptionOnChild(EXCEPTION, handler -> {
                        handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR).required();
                        handler.execute("work");
                    });
                    root.complete("unexpected");
                },
                Map.of());
        NodeRunModel waitNode = currentNode(run);
        TaskRunModel work = task(run, thread(run, "leaf"));
        claimTask(work);
        reportTaskException(work, declinedWithoutContent);
        Integer handlerThreadRunId =
                waitNode.getWaitForThreadsRun().getThreads().get(0).getThreadRunNumber();
        TaskRunModel handlerTask = task(reload(run), thread(run, handlerThreadRunId));
        claimTask(handlerTask);

        WfRun failed = reload(run);
        assertThat(failed.getStatus()).isEqualTo(LHStatus.ERROR);
        NodeRun waitForThreadNode = readGetable(waitNode.getId()).toProto().build();
        assertThat(waitForThreadNode.getFailuresList()).hasSize(1);
    }

    @Test
    void perChildHandlerRejectsIncompatibleExceptionContent() {
        VariableValue content = VariableValue.newBuilder().setInt(2).build();
        LHTaskException declinedWithInvalidContent = LHTaskException.newBuilder()
                .setName(EXCEPTION)
                .setMessage("Declined")
                .setContent(content)
                .build();
        WfRun run = startWorkflow(
                root -> {
                    SpawnedThread child = root.spawnThread(leaf -> leaf.execute("work"), "leaf", Map.of());
                    root.waitForThreads(SpawnedThreads.of(child)).handleExceptionOnChild(EXCEPTION, handler -> {
                        handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR).required();
                        handler.execute("work");
                    });
                    root.complete("unexpected");
                },
                Map.of());
        NodeRunModel waitNode = currentNode(run);
        TaskRunModel work = task(run, thread(run, "leaf"));
        claimTask(work);
        reportTaskException(work, declinedWithInvalidContent);
        int handlerThreadRunId =
                waitNode.getWaitForThreadsRun().getThreads().get(0).getThreadRunNumber();
        TaskRunModel handlerTask = task(reload(run), thread(run, handlerThreadRunId));
        claimTask(handlerTask);

        WfRun failed = reload(run);
        assertThat(failed.getStatus()).isEqualTo(LHStatus.ERROR);
        NodeRun waitForThreadNode = readGetable(waitNode.getId()).toProto().build();
        assertThat(waitForThreadNode.getFailuresList()).hasSize(1);
    }

    @Test
    void explicitWorkflowExceptionPropagatesWithContent() {
        WfRun run = startWorkflow(
                root -> {
                    SpawnedThread child =
                            root.spawnThread(leaf -> leaf.fail("payment-123", EXCEPTION, "Declined"), "leaf", Map.of());
                    root.waitForThreads(SpawnedThreads.of(child));
                },
                Map.of());
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.EXCEPTION);
        assertPropagatedFailure(currentNode(run), FailureKind.EXCEPTION);
    }

    @Test
    void successfulNodeDoesNotStartFailureHandlers() {
        WfRun run = startWorkflow(
                thread -> {
                    TaskNodeOutput work = thread.execute("work");
                    thread.handleError(work, recovery -> recovery.fail("unexpected-error-handler", "Unexpected"));
                    thread.handleException(
                            work, recovery -> recovery.fail("unexpected-exception-handler", "Unexpected"));
                    thread.handleAnyFailure(
                            work, recovery -> recovery.fail("unexpected-failure-handler", "Unexpected"));
                    thread.complete(work);
                },
                Map.of());
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTask(task, TaskStatus.TASK_SUCCESS, CONTENT);
        assertThat(reload(run).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(reload(run).getThreadRunsCount()).isEqualTo(1);
        assertThat(reload(run).getThreadRuns(0).getOutput()).isEqualTo(CONTENT);
    }

    private void failTask(TaskRunModel task, FailureKind kind) {
        assertThat(claimTask(task).hasResult()).isTrue();
        if (kind == FailureKind.ERROR) {
            reportTaskError(
                    task,
                    LHTaskError.newBuilder()
                            .setType(LHErrorType.TASK_FAILURE)
                            .setMessage("Worker failed")
                            .build());
        } else {
            reportTaskException(
                    task,
                    LHTaskException.newBuilder()
                            .setName(EXCEPTION)
                            .setMessage("Declined")
                            .setContent(CONTENT)
                            .build());
        }
    }

    private void failTask(TaskRunModel task, TaskStatus taskStatus, LHErrorType errorType) {
        assertThat(claimTask(task).hasResult()).isTrue();
        reportTaskError(
                task,
                taskStatus,
                LHTaskError.newBuilder()
                        .setType(errorType)
                        .setMessage("Worker failed")
                        .build());
    }

    private void failTaskWithException(TaskRunModel task, String exceptionName) {
        assertThat(claimTask(task).hasResult()).isTrue();
        reportTaskException(
                task,
                LHTaskException.newBuilder()
                        .setName(exceptionName)
                        .setMessage("Declined")
                        .setContent(CONTENT)
                        .build());
    }

    private void failTaskWithException(TaskRunModel task, LHTaskException exception) {
        assertThat(claimTask(task).hasResult()).isTrue();
        reportTaskException(task, exception);
    }

    private WfRun reload(WfRun run) {
        return readGetable(new WfRunIdModel(run.getId().getId())).toProto().build();
    }

    private ThreadRun thread(WfRun run, String name) {
        return reload(run).getThreadRunsList().stream()
                .filter(candidate -> candidate.getThreadSpecName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private ThreadRun thread(WfRun run, int id) {
        return reload(run).getThreadRunsList().stream()
                .filter(candidate -> candidate.getNumber() == id)
                .findFirst()
                .orElseThrow();
    }

    private NodeRunModel node(WfRun run, ThreadRun thread) {
        return readGetable(new NodeRunIdModel(
                new WfRunIdModel(run.getId().getId()), thread.getNumber(), thread.getCurrentNodePosition()));
    }

    private TaskRunModel task(WfRun run, ThreadRun thread) {
        return readGetable(node(run, thread).getTaskRun().getTaskRunId());
    }

    private void assertPropagatedFailure(NodeRunModel node, FailureKind kind) {
        Failure failure = readGetable(node.getId()).toProto().getFailures(0);
        assertThat(failure.getFailureName()).isEqualTo(kind == FailureKind.ERROR ? "CHILD_FAILURE" : EXCEPTION);
        if (kind == FailureKind.EXCEPTION) assertThat(failure.getContent()).isEqualTo(CONTENT);
    }
}
