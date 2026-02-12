package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.*;
import static org.assertj.core.api.Assertions.*;


import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

@LHTest(
        externalEventNames = {
            "person-1-approves",
            "person-2-approves",
            "person-3-approves",
            "thread-1-event",
            "thread-2-event"
        })
public class WaitForThreadsTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("wait-for-threads-with-exception-handler")
    private Workflow waitForThreadsWithExceptionHandlerWorkflow;

    @LHWorkflow("simple-wait-for-threads-workflow")
    private Workflow simpleWaitForThreadsWorkflow;

    @LHWorkflow("wait-for-threads-without-exception-handler")
    private Workflow waitForThreadsWithoutExceptionHandlerWorkflow;

    @Test
    void shouldExecuteSimpleWorkflowSuccessfully() {
        int entrypointThreadNumber = 0;
        int event1ThreadNumber = 1;
        Map<String, Integer> event1 = Map.of("myInt", 5);
        Map<String, Integer> event2 = Map.of("myInt", 10);
        Consumer<NodeRun> verifyWaitForThreadNodeBeforeFirstExternalEvent = nodeRun -> {
            WaitForThreadsRun waitForThreadsRun = nodeRun.getWaitForThreads();
            assertThat(waitForThreadsRun.getThreadsCount()).isEqualTo(2);
            assertThat(waitForThreadsRun.getThreads(0).getThreadRunNumber()).isEqualTo(1);
            assertThat(waitForThreadsRun.getThreads(1).getThreadRunNumber()).isEqualTo(2);
        };
        Consumer<NodeRun> verifyWaitForThreadNodeAfterFirstExternalEvent = nodeRun -> {
            WaitForThreadsRun waitForThreadsRun = nodeRun.getWaitForThreads();
            assertThat(waitForThreadsRun.getThreads(0).getThreadRunNumber()).isEqualTo(1);
            assertThat(waitForThreadsRun.getThreads(1).getThreadRunNumber()).isEqualTo(2);
            assertThat(waitForThreadsRun.getThreads(0).getThreadStatus()).isEqualTo(COMPLETED);
            assertThat(waitForThreadsRun.getThreads(1).getThreadStatus()).isEqualTo(RUNNING);
        };
        workflowVerifier
                .prepareRun(simpleWaitForThreadsWorkflow)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> assertThat(wfRun.getThreadRunsCount()).isEqualTo(3))
                .waitForNodeRunStatus(entrypointThreadNumber, 3, RUNNING)
                .thenVerifyNodeRun(entrypointThreadNumber, 3, verifyWaitForThreadNodeBeforeFirstExternalEvent)
                .waitForThreadRunStatus(event1ThreadNumber, RUNNING)
                .thenSendExternalEventWithContent("thread-1-event", event1)
                .waitForThreadRunStatus(event1ThreadNumber, COMPLETED)
                .waitForStatus(RUNNING)
                .waitForNodeRunStatus(entrypointThreadNumber, 3, RUNNING)
                .thenVerifyNodeRun(entrypointThreadNumber, 3, verifyWaitForThreadNodeAfterFirstExternalEvent)
                .thenSendExternalEventWithContent("thread-2-event", event2)
                .waitForStatus(COMPLETED)
                .thenVerifyNodeRun(entrypointThreadNumber, 3, nodeRun -> {
                    assertThat(nodeRun.getStatus()).isEqualTo(COMPLETED);
                    assertThat(nodeRun.getWaitForThreads().getThreads(0).getThreadStatus())
                            .isEqualTo(COMPLETED);
                    assertThat(nodeRun.getWaitForThreads().getThreads(1).getThreadStatus())
                            .isEqualTo(COMPLETED);
                })
                .start();
    }

    @Test
    void shouldFailWorkflowExecutionWhenFirstSpawnedThreadFails() {
        Map<String, String> failingEvent = Map.of("myInt", "invalidInt");
        Map<String, Integer> event2 = Map.of("myInt", 10);
        int entrypointThreadNumber = 0;
        int firstEventThreadNumber = 1;
        int secondEventThreadNumber = 2;
        workflowVerifier
                .prepareRun(simpleWaitForThreadsWorkflow)
                .waitForStatus(RUNNING)
                .waitForThreadRunStatus(entrypointThreadNumber, RUNNING)
                .waitForThreadRunStatus(firstEventThreadNumber, RUNNING)
                .waitForThreadRunStatus(secondEventThreadNumber, RUNNING)
                .thenSendExternalEventWithContent("thread-1-event", failingEvent)
                .waitForStatus(ERROR)
                .thenSendExternalEventWithContent("thread-2-event", event2)
                .waitForThreadRunStatus(entrypointThreadNumber, ERROR)
                .waitForThreadRunStatus(firstEventThreadNumber, ERROR)
                .waitForThreadRunStatus(secondEventThreadNumber, HALTED)
                .waitForNodeRunStatus(entrypointThreadNumber, 3, ERROR)
                .waitForNodeRunStatus(firstEventThreadNumber, 2, ERROR)
                .waitForNodeRunStatus(secondEventThreadNumber, 1, HALTED)
                .thenVerifyNodeRun(firstEventThreadNumber, 2, nodeRun -> {
                    assertThat(nodeRun.getTask().hasTaskRunId()).isFalse();
                    assertThat(nodeRun.getFailuresCount()).isEqualTo(1);
                    assertThat(nodeRun.getFailures(0).getFailureName()).isEqualTo("VAR_SUB_ERROR");
                })
                .thenVerifyNodeRun(entrypointThreadNumber, 3, nodeRun -> {
                    WaitForThreadsRun waitForThreadsRun = nodeRun.getWaitForThreads();
                    assertThat(waitForThreadsRun.getThreads(0).getThreadStatus())
                            .isEqualTo(ERROR);
                    assertThat(waitForThreadsRun.getThreads(1).getThreadStatus())
                            .isEqualTo(HALTED);
                })
                .start();
    }

    @Test
    void shouldExecuteExceptionHandlerWhenChildThreadTrowsAUserDefinedException() {
        int exceptionHandlerThreadNumber = 4;
        Map<String, Object> person1DenyEvent = Map.of("approval", false);
        Map<String, Object> person2Approves = Map.of("approval", true);
        Map<String, Object> person3Approves = Map.of("approval", true);
        workflowVerifier
                .prepareRun(waitForThreadsWithExceptionHandlerWorkflow)
                .waitForStatus(LHStatus.RUNNING)
                .thenSendExternalEventWithContent("person-1-approves", person1DenyEvent)
                .waitForTaskStatus(exceptionHandlerThreadNumber, 1, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(
                        exceptionHandlerThreadNumber, 1, variableValue -> assertThat(variableValue.getStr())
                                .isEqualTo("result"))
                .thenSendExternalEventWithContent("person-2-approves", person2Approves)
                .thenSendExternalEventWithContent("person-3-approves", person3Approves)
                .waitForStatus(COMPLETED)
                .start();
    }

    @Test
    void shouldTerminateWorkflowExecutionWhenExceptionHandlerIsNotPresent() {
        Map<String, Object> person1DenyEvent = Map.of("approval", false);
        workflowVerifier
                .prepareRun(waitForThreadsWithoutExceptionHandlerWorkflow)
                .waitForStatus(RUNNING)
                .thenSendExternalEventWithContent("person-1-approves", person1DenyEvent)
                .waitForStatus(EXCEPTION)
                .start();
    }

    @LHWorkflow("wait-for-threads-with-exception-handler")
    public Workflow buildWaitForThreadsWithExceptionHandlerWorkflow() {
        return new WorkflowImpl("wait-for-threads-with-exception-handler", thread -> {
            // Initialize variables.
            WfRunVariable person1Approved = thread.addVariable("person-1-approved", VariableType.BOOL);
            WfRunVariable person2Approved = thread.addVariable("person-2-approved", VariableType.BOOL);
            WfRunVariable person3Approved = thread.addVariable("person-3-approved", VariableType.BOOL);
            WfRunVariable allApproved = thread.addVariable("all-approved", VariableType.BOOL);

            // Variables are initialized to NULL. Need to set to a real value.
            thread.mutate(allApproved, Operation.ASSIGN, false);
            thread.mutate(person1Approved, Operation.ASSIGN, false);
            thread.mutate(person2Approved, Operation.ASSIGN, false);
            thread.mutate(person3Approved, Operation.ASSIGN, false);

            BiFunction<WfRunVariable, String, ThreadFunc> buildChildThread = (approvalVariable, approvalName) -> {
                return approvalThread -> {
                    WfRunVariable jsonVariable =
                            approvalThread.addVariable(approvalName + "-response", VariableType.JSON_OBJ);
                    approvalThread.mutate(
                            jsonVariable,
                            Operation.ASSIGN,
                            approvalThread.waitForEvent(approvalName + "-approves"));
                    approvalThread.doIfElse(
                            approvalThread.condition(jsonVariable.jsonPath("$.approval"), Operation.EQUALS, true),
                            ifHandler -> {
                                approvalThread.mutate(person2Approved, Operation.ASSIGN, true);
                            },
                            elseHandler -> {
                                approvalThread.fail("denied-by-user", "message here");
                            });
                };
            };

            // Wait for all users to approve the transaction
            SpawnedThread p1Thread =
                    thread.spawnThread(buildChildThread.apply(person1Approved, "person-1"), "person-1", null);
            SpawnedThread p2Thread =
                    thread.spawnThread(buildChildThread.apply(person2Approved, "person-2"), "person-2", null);
            SpawnedThread p3Thread =
                    thread.spawnThread(buildChildThread.apply(person3Approved, "person-3"), "person-3", null);

            NodeOutput nodeOutput = thread.waitForThreads(SpawnedThreads.of(p1Thread, p2Thread, p3Thread));

            thread.handleException(nodeOutput, "denied-by-user", xnHandler -> {
                xnHandler.execute("exc-handler");
            });

            // Tell the reminder workflow to stop
            thread.mutate(allApproved, Operation.ASSIGN, true);
        });
    }

    @LHWorkflow("wait-for-threads-without-exception-handler")
    public Workflow buildWaitForThreadsWithoutExceptionHandlerWorkflow() {
        return new WorkflowImpl("wait-for-threads-without-exception-handler", thread -> {
            // Initialize variables.
            WfRunVariable person1Approved = thread.addVariable("person-1-approved", VariableType.BOOL);
            WfRunVariable person2Approved = thread.addVariable("person-2-approved", VariableType.BOOL);
            WfRunVariable person3Approved = thread.addVariable("person-3-approved", VariableType.BOOL);
            WfRunVariable allApproved = thread.addVariable("all-approved", VariableType.BOOL);

            // Variables are initialized to NULL. Need to set to a real value.
            thread.mutate(allApproved, Operation.ASSIGN, false);
            thread.mutate(person1Approved, Operation.ASSIGN, false);
            thread.mutate(person2Approved, Operation.ASSIGN, false);
            thread.mutate(person3Approved, Operation.ASSIGN, false);

            BiFunction<WfRunVariable, String, ThreadFunc> buildChildThread = (approvalVariable, approvalName) -> {
                return approvalThread -> {
                    WfRunVariable jsonVariable =
                            approvalThread.addVariable(approvalName + "-response", VariableType.JSON_OBJ);
                    approvalThread.mutate(
                            jsonVariable,
                            Operation.ASSIGN,
                            approvalThread.waitForEvent(approvalName + "-approves"));
                    approvalThread.doIfElse(
                            approvalThread.condition(jsonVariable.jsonPath("$.approval"), Operation.EQUALS, true),
                            ifHandler -> {
                                approvalThread.mutate(person2Approved, Operation.ASSIGN, true);
                            },
                            elseHandler -> {
                                approvalThread.fail("denied-by-user", "message here");
                            });
                };
            };

            // Wait for all users to approve the transaction
            SpawnedThread p1Thread =
                    thread.spawnThread(buildChildThread.apply(person1Approved, "person-1"), "person-1", null);
            SpawnedThread p2Thread =
                    thread.spawnThread(buildChildThread.apply(person2Approved, "person-2"), "person-2", null);
            SpawnedThread p3Thread =
                    thread.spawnThread(buildChildThread.apply(person3Approved, "person-3"), "person-3", null);

            thread.waitForThreads(SpawnedThreads.of(p1Thread, p2Thread, p3Thread));

            // Tell the reminder workflow to stop
            thread.mutate(allApproved, Operation.ASSIGN, true);
        });
    }

    @LHWorkflow("simple-wait-for-threads-workflow")
    public Workflow buildSimpleWaitForThreadWorkflow() {
        BiFunction<String, String, ThreadFunc> buildSpawnThread = (variableName, externalEventName) -> {
            return spawnedThread -> {
                WfRunVariable eventOutput = spawnedThread.addVariable(variableName, VariableType.JSON_OBJ);
                spawnedThread.mutate(
                        eventOutput, Operation.ASSIGN, spawnedThread.waitForEvent(externalEventName));
                spawnedThread.execute("add-1", eventOutput.jsonPath("$.myInt"));
            };
        };
        return new WorkflowImpl("simple-wait-for-thread-workflow", thread -> {
            SpawnedThread child1 = thread.spawnThread(
                    buildSpawnThread.apply("input1", "thread-1-event"), "child-1", Map.of("input1", Map.of()));
            SpawnedThread child2 = thread.spawnThread(
                    buildSpawnThread.apply("input2", "thread-2-event"), "child-2", Map.of("input2", Map.of()));

            thread.waitForThreads(SpawnedThreads.of(child1, child2));
        });
    }

    @LHTaskMethod("exc-handler")
    public String exceptionHandler() {
        System.out.println("Ok, handler executed");
        return "result";
    }

    @LHTaskMethod("add-1")
    public int addOne(int input) {
        return input + 1;
    }
}

@AllArgsConstructor
class MyInput {
    public String endResult;
    public boolean executeTask;
}
