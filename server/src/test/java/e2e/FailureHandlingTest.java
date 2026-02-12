package e2e;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHJsonProcessingException;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@LHTest
public class FailureHandlingTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("handle-error-wf")
    private Workflow handleErrorWf;

    @LHWorkflow("handle-exception-wf")
    private Workflow handleExceptionWf;

    @LHWorkflow("handle-any-exception-wf")
    private Workflow handleAnyExceptionWf;

    @LHWorkflow("handle-client-specific-exception")
    private Workflow handleClientSpecificException;

    @LHWorkflow("handle-any-error-wf")
    private Workflow handleAnyErrorWf;

    @LHWorkflow("handle-any-failure-wf")
    private Workflow handleAnyFailureWf;

    @LHWorkflow("exception-failure-wf")
    private Workflow exceptionFailureWf;

    @Test
    public void shouldHandleAnyError() {
        workflowVerifier
                .prepareRun(handleErrorWf)
                .waitForStatus(LHStatus.COMPLETED)
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .waitForNodeRunStatus(1, 1, LHStatus.COMPLETED)
                .waitForNodeRunStatus(0, 3, LHStatus.COMPLETED)
                .start();
    }

    @Test
    public void shouldHandleSpecificException() {
        workflowVerifier
                .prepareRun(handleExceptionWf)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    public void failureHanlderIdShouldBeNotedOnFailure() {
        workflowVerifier
                .prepareRun(handleClientSpecificException)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getFailuresCount()).isEqualTo(1);
                    Failure failure = nodeRun.getFailures(0);
                    Assertions.assertThat(failure.getContent()).isNotNull();
                    try {
                        Map<String, String> outputData = LHLibUtil.deserializeFromjson(
                                failure.getContent().getJsonObj(), Map.class);
                        Assertions.assertThat(outputData).containsOnlyKeys("date", "server");
                    } catch (LHJsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    Assertions.assertThat(failure.getFailureHandlerThreadrunId())
                            .isEqualTo(1);

                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.EXCEPTION);
                })
                .start();
    }

    @Test
    public void shouldMarkWorkflowBusinessException() {
        workflowVerifier
                .prepareRun(exceptionFailureWf)
                .waitForTaskStatus(0, 1, TaskStatus.TASK_EXCEPTION)
                .waitForNodeRunStatus(0, 1, LHStatus.EXCEPTION)
                .waitForStatus(LHStatus.EXCEPTION)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    List<Failure> failures = nodeRun.getFailuresList();
                    Assertions.assertThat(failures).hasSize(1);
                    for (Failure failure : failures) {
                        Assertions.assertThat(failure.getFailureName()).isEqualTo("client-exception");
                        Assertions.assertThat(failure.getMessage()).contains("This is a business exception!");
                    }
                })
                .start();
    }

    @Nested
    class HandleAnyException {
        @Test
        public void shouldCompleteOnUserDefinedException() {
            workflowVerifier
                    .prepareRun(handleAnyExceptionWf, Arg.of("fail-with-user-defined-exception", true))
                    .waitForStatus(LHStatus.COMPLETED)
                    .start();
        }

        @Test
        public void shouldFailWithErrorOnTaskFailure() {
            workflowVerifier
                    .prepareRun(handleAnyExceptionWf, Arg.of("fail-with-user-defined-exception", false))
                    .waitForStatus(LHStatus.ERROR)
                    .start();
        }

        @Test
        public void shouldHandleTaskException() {
            workflowVerifier
                    .prepareRun(handleClientSpecificException)
                    .waitForStatus(LHStatus.COMPLETED)
                    .start();
        }
    }

    @Nested
    class HandleAnyError {

        @Test
        public void shouldCompleteOnTaskFailure() {
            workflowVerifier
                    .prepareRun(handleAnyErrorWf, Arg.of("fail-with-user-defined-exception", false))
                    .waitForStatus(LHStatus.COMPLETED)
                    .start();
        }

        @Test
        public void shouldFailWithExceptionOnUserDefinedException() {
            workflowVerifier
                    .prepareRun(handleAnyErrorWf, Arg.of("fail-with-user-defined-exception", true))
                    .waitForStatus(LHStatus.EXCEPTION)
                    .start();
        }
    }

    @Nested
    class HandleAnyFailure {
        @Test
        public void shouldCompleteOnTaskFailure() {
            workflowVerifier
                    .prepareRun(handleAnyFailureWf, Arg.of("fail-with-user-defined-exception", false))
                    .waitForStatus(LHStatus.COMPLETED)
                    .start();
        }

        @Test
        public void shouldCompleteOnUserDefinedException() {
            workflowVerifier
                    .prepareRun(handleAnyFailureWf, Arg.of("fail-with-user-defined-exception", true))
                    .waitForStatus(LHStatus.COMPLETED)
                    .start();
        }
    }

    @LHWorkflow("handle-error-wf")
    public Workflow handleErrorWorkflow() {
        return new WorkflowImpl("handle-error-wf", thread -> {
            NodeOutput node = thread.execute("fail");
            thread.handleError(node, handler -> {
                handler.execute("my-handler");
            });
            thread.execute("my-task");
        });
    }

    @LHWorkflow("handle-client-specific-exception")
    public Workflow handleClientSpecificException() {
        return new WorkflowImpl("handle-client-specific-exception", thread -> {
            NodeOutput node = thread.execute("business-exception-failure");
            thread.handleException(node, handler -> {
                WfRunVariable failureContent =
                        handler.addVariable(WorkflowThread.HANDLER_INPUT_VAR, VariableType.JSON_OBJ);
                handler.execute("my-handler-with-args", failureContent.jsonPath("server"));
            });
            thread.execute("my-task");
        });
    }

    @LHWorkflow("handle-exception-wf")
    public Workflow handleExceptionWorkflow() {
        return new WorkflowImpl("handle-exception-wf", thread -> {
            SpawnedThread spawnThread = thread.spawnThread(
                    subThread -> {
                        subThread.fail("my-exception", "this is a exception");
                    },
                    "sub-thread",
                    Map.of());
            WaitForThreadsNodeOutput waitForThread = thread.waitForThreads(SpawnedThreads.of(spawnThread));
            thread.handleException(waitForThread, "my-exception", handler -> {
                handler.execute("my-task");
            });
        });
    }

    @LHWorkflow("handle-any-exception-wf")
    public Workflow handleAnyExceptionWorkflow() {
        return new WorkflowImpl("handle-any-exception-wf", thread -> {
            WfRunVariable failWithUserDefinedException =
                    thread.addVariable("fail-with-user-defined-exception", VariableType.BOOL);
            SpawnedThread spawnThread = thread.spawnThread(
                    subThread -> {
                        WfRunVariable shouldItFails = subThread.addVariable("should-it-fails", VariableType.BOOL);
                        subThread.doIfElse(
                                subThread.condition(shouldItFails, Operation.EQUALS, true),
                                ifBody -> subThread.fail("custom-exception", "this is a exception"),
                                elseBody -> subThread.execute("fail"));
                    },
                    "sub-thread",
                    Map.of("should-it-fails", failWithUserDefinedException));
            WaitForThreadsNodeOutput waitForThread = thread.waitForThreads(SpawnedThreads.of(spawnThread));
            thread.handleException(waitForThread, handler -> {
                handler.execute("my-task");
            });
        });
    }

    @LHWorkflow("handle-any-error-wf")
    public Workflow handleAnyErrorWorkflow() {
        return new WorkflowImpl("handle-any-error-wf", thread -> {
            WfRunVariable failWithUserDefinedException =
                    thread.addVariable("fail-with-user-defined-exception", VariableType.BOOL);
            SpawnedThread spawnThread = thread.spawnThread(
                    subThread -> {
                        WfRunVariable shouldItFails = subThread.addVariable("should-it-fails", VariableType.BOOL);
                        subThread.doIfElse(
                                subThread.condition(shouldItFails, Operation.EQUALS, true),
                                ifBody -> subThread.fail("custom-exception", "this is a exception"),
                                elseBody -> subThread.execute("fail"));
                    },
                    "sub-thread",
                    Map.of("should-it-fails", failWithUserDefinedException));
            WaitForThreadsNodeOutput waitForThread = thread.waitForThreads(SpawnedThreads.of(spawnThread));
            thread.handleError(waitForThread, handler -> {
                handler.execute("my-task");
            });
        });
    }

    @LHWorkflow("exception-failure-wf")
    public Workflow exceptionFailureWf() {
        return new WorkflowImpl("exception-failure-wf", thread -> {
            thread.execute("business-exception-failure");
            thread.execute("my-task");
        });
    }

    @LHWorkflow("handle-any-failure-wf")
    public Workflow handleAnyFailureWorkflow() {
        return new WorkflowImpl("handle-any-failure-wf", thread -> {
            WfRunVariable failWithUserDefinedException =
                    thread.addVariable("fail-with-user-defined-exception", VariableType.BOOL);
            SpawnedThread spawnThread = thread.spawnThread(
                    subThread -> {
                        WfRunVariable shouldItFails = subThread.addVariable("should-it-fails", VariableType.BOOL);
                        subThread.doIfElse(
                                subThread.condition(shouldItFails, Operation.EQUALS, true),
                                ifBody -> ifBody.fail("custom-exception", "this is a exception"),
                                elseBody -> elseBody.execute("fail"));
                    },
                    "sub-thread",
                    Map.of("should-it-fails", failWithUserDefinedException));
            WaitForThreadsNodeOutput waitForThread = thread.waitForThreads(SpawnedThreads.of(spawnThread));
            thread.handleAnyFailure(waitForThread, handler -> {
                handler.execute("my-task");
            });
        });
    }

    @LHTaskMethod("fail")
    public String fail() {
        throw new RuntimeException("something went wrong!");
    }

    @LHTaskMethod("business-exception-failure")
    public String businessExceptionFailure() throws LHTaskException {
        Map<String, String> data = Map.of("date", new Date().toString(), "server", "127.0.0.1");
        VariableValue failureContent = LHLibUtil.objToVarVal(data);
        throw new LHTaskException("client-exception", "This is a business exception!", failureContent);
    }

    @LHTaskMethod("my-handler")
    public String handler() {
        return "Exception handled";
    }

    @LHTaskMethod("my-handler-with-args")
    public String handlerWithArgs(String detail) {
        return "Exception handled: " + detail;
    }

    @LHTaskMethod("my-task")
    public String task() {
        return "Task executed";
    }
}
