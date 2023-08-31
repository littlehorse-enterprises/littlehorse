package io.littlehorse.e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.*;
import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
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

    @Test
    void shouldExecuteSimpleWorkflowSuccessfully() {
        int entrypointThreadNumber = 0;
        int event1ThreadNumber = 1;
        Map<String, Integer> event1 = Map.of("myInt", 5);
        Map<String, Integer> event2 = Map.of("myInt", 10);
        Consumer<NodeRun> verifyWaitForThreadNodeBeforeFirstExternalEvent = nodeRun -> {
            WaitForThreadsRun waitForThreadsRun = nodeRun.getWaitThreads();
            assertThat(waitForThreadsRun.getThreadsCount()).isEqualTo(2);
            assertThat(waitForThreadsRun.getThreads(0).getThreadRunNumber()).isEqualTo(1);
            assertThat(waitForThreadsRun.getThreads(1).getThreadRunNumber()).isEqualTo(2);
        };
        Consumer<NodeRun> verifyWaitForThreadNodeAfterFirstExternalEvent = nodeRun -> {
            WaitForThreadsRun waitForThreadsRun = nodeRun.getWaitThreads();
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
                .thenSendExternalEventJsonContent("thread-1-event", event1)
                .waitForThreadRunStatus(event1ThreadNumber, COMPLETED)
                .waitForStatus(RUNNING)
                .waitForNodeRunStatus(entrypointThreadNumber, 3, RUNNING)
                .thenVerifyNodeRun(entrypointThreadNumber, 3, verifyWaitForThreadNodeAfterFirstExternalEvent)
                .thenSendExternalEventJsonContent("thread-2-event", event2)
                .waitForStatus(COMPLETED)
                .thenVerifyNodeRun(entrypointThreadNumber, 3, nodeRun -> {
                    assertThat(nodeRun.getStatus()).isEqualTo(COMPLETED);
                    assertThat(nodeRun.getWaitThreads().getThreads(0).getThreadStatus())
                            .isEqualTo(COMPLETED);
                    assertThat(nodeRun.getWaitThreads().getThreads(1).getThreadStatus())
                            .isEqualTo(COMPLETED);
                })
                .start();
    }

    @Test
    void shouldFailWorkflowExecutionWhenFirstSpawnedThreadFails() {
        Map<String, String> event1 = Map.of("myInt", "invalidInt");
        Map<String, Integer> event2 = Map.of("myInt", 10);
        workflowVerifier
                .prepareRun(simpleWaitForThreadsWorkflow)
                .waitForStatus(RUNNING)
                .thenSendExternalEventJsonContent("thread-1-event", event1)
                .waitForNodeRunStatus(1, 2, ERROR)
                .waitForThreadRunStatus(1, ERROR)
                .thenVerifyNodeRun(1, 2, nodeRun -> {
                    assertThat(nodeRun.getTask().hasTaskRunId()).isFalse();
                    assertThat(nodeRun.getFailuresCount()).isEqualTo(1);
                    assertThat(nodeRun.getFailures(0).getFailureName()).isEqualTo("VAR_SUB_ERROR");
                })
                .waitForThreadRunStatus(2, RUNNING) // change this
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    WaitForThreadsRun waitForThreadsRun = nodeRun.getWaitThreads();
                    assertThat(waitForThreadsRun.getThreads(0).getThreadStatus())
                            .isEqualTo(ERROR);
                    assertThat(waitForThreadsRun.getThreads(1).getThreadStatus())
                            .isEqualTo(RUNNING);
                })
                .waitForNodeRunStatus(2, 1, RUNNING)
                .thenSendExternalEventJsonContent("thread-2-event", event2)
                .waitForStatus(ERROR)
                .waitForThreadRunStatus(2, COMPLETED)
                .waitForNodeRunStatus(2, 1, COMPLETED)
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    assertThat(nodeRun.getWaitThreads().getThreads(1).getThreadStatus())
                            .isEqualTo(COMPLETED);
                })
                .start();
    }

    @Test
    void shouldExecuteExceptionHandlerWhenChildThreadTrowsAUserDefinedException() {
        int person1ApprovalThreadNumber = 1;
        int person2ApprovalThreadNumber = 2;
        int exceptionHandlerThreadNumber = 4;
        Map person1DenyEvent = Map.of("approval", false);
        workflowVerifier
                .prepareRun(waitForThreadsWithExceptionHandlerWorkflow)
                .waitForStatus(LHStatus.RUNNING)
                .thenSendExternalEventJsonContent("person-1-approves", person1DenyEvent)
                .waitForStatus(LHStatus.RUNNING)
                .waitForNodeRunStatus(person1ApprovalThreadNumber, 3, LHStatus.EXCEPTION)
                .waitForThreadRunStatus(person2ApprovalThreadNumber, LHStatus.HALTED)
                .waitForNodeRunStatus(person2ApprovalThreadNumber, 1, LHStatus.RUNNING)
                .waitForTaskStatus(exceptionHandlerThreadNumber, 1, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(
                        exceptionHandlerThreadNumber, 1, variableValue -> assertThat(variableValue.getStr())
                                .isEqualTo("result"))
                .start();
    }

    @LHWorkflow("wait-for-threads-with-exception-handler")
    public Workflow buildWaitForThreadsWithExceptionHandlerWorkflow() {
        return new WorkflowImpl("parallel-approval", thread -> {
            // Initialize variables.
            WfRunVariable person1Approved = thread.addVariable("person-1-approved", VariableType.BOOL);
            WfRunVariable person2Approved = thread.addVariable("person-2-approved", VariableType.BOOL);
            WfRunVariable person3Approved = thread.addVariable("person-3-approved", VariableType.BOOL);
            WfRunVariable allApproved = thread.addVariable("all-approved", VariableType.BOOL);

            // Variables are initialized to NULL. Need to set to a real value.
            thread.mutate(allApproved, VariableMutationType.ASSIGN, false);
            thread.mutate(person1Approved, VariableMutationType.ASSIGN, false);
            thread.mutate(person2Approved, VariableMutationType.ASSIGN, false);
            thread.mutate(person3Approved, VariableMutationType.ASSIGN, false);

            BiFunction<WfRunVariable, String, ThreadFunc> buildChildThread = (approvalVariable, approvalName) -> {
                return approvalThread -> {
                    WfRunVariable jsonVariable =
                            approvalThread.addVariable(approvalName + "-response", VariableType.JSON_OBJ);
                    approvalThread.mutate(
                            jsonVariable,
                            VariableMutationType.ASSIGN,
                            approvalThread.waitForEvent(approvalName + "-approves"));
                    approvalThread.doIfElse(
                            approvalThread.condition(jsonVariable.jsonPath("$.approval"), Comparator.EQUALS, true),
                            ifHandler -> {
                                approvalThread.mutate(person2Approved, VariableMutationType.ASSIGN, true);
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

            NodeOutput nodeOutput = thread.waitForThreads(p1Thread, p2Thread, p3Thread);

            thread.handleException(nodeOutput, "denied-by-user", xnHandler -> {
                xnHandler.execute("exc-handler");
            });

            // Tell the reminder workflow to stop
            thread.mutate(allApproved, VariableMutationType.ASSIGN, true);
        });
    }

    @LHWorkflow("simple-wait-for-threads-workflow")
    public Workflow buildSimpleWaitForThreadWorkflow() {
        BiFunction<String, String, ThreadFunc> buildSpawnThread = (variableName, externalEventName) -> {
            return spawnedThread -> {
                WfRunVariable eventOutput = spawnedThread.addVariable(variableName, VariableType.JSON_OBJ);
                spawnedThread.mutate(
                        eventOutput, VariableMutationType.ASSIGN, spawnedThread.waitForEvent(externalEventName));
                spawnedThread.execute("add-1", eventOutput.jsonPath("$.myInt"));
            };
        };
        return new WorkflowImpl("simple-wait-for-thread-workflow", thread -> {
            SpawnedThread child1 = thread.spawnThread(
                    buildSpawnThread.apply("input1", "thread-1-event"), "child-1", Map.of("input1", Map.of()));
            SpawnedThread child2 = thread.spawnThread(
                    buildSpawnThread.apply("input2", "thread-2-event"), "child-2", Map.of("input2", Map.of()));

            thread.waitForThreads(child1, child2);
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
