package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.*;
import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.assertj.core.api.Assertions;
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

    @LHWorkflow("handle-exception-on-children")
    private Workflow handleExceptionOnChildren;

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
                .thenSendExternalEventJsonContent("thread-1-event", failingEvent)
                .waitForStatus(ERROR)
                .thenSendExternalEventJsonContent("thread-2-event", event2)
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
                    WaitForThreadsRun waitForThreadsRun = nodeRun.getWaitThreads();
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
                .thenSendExternalEventJsonContent("person-1-approves", person1DenyEvent)
                .waitForTaskStatus(exceptionHandlerThreadNumber, 1, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(
                        exceptionHandlerThreadNumber, 1, variableValue -> assertThat(variableValue.getStr())
                                .isEqualTo("result"))
                .thenSendExternalEventJsonContent("person-2-approves", person2Approves)
                .thenSendExternalEventJsonContent("person-3-approves", person3Approves)
                .waitForStatus(COMPLETED)
                .start();
    }

    @Test
    void shouldTerminateWorkflowExecutionWhenExceptionHandlerIsNotPresent() {
        Map<String, Object> person1DenyEvent = Map.of("approval", false);
        workflowVerifier
                .prepareRun(waitForThreadsWithoutExceptionHandlerWorkflow)
                .waitForStatus(RUNNING)
                .thenSendExternalEventJsonContent("person-1-approves", person1DenyEvent)
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

            NodeOutput nodeOutput = thread.waitForThreads(SpawnedThreads.of(p1Thread, p2Thread, p3Thread));

            thread.handleException(nodeOutput, "denied-by-user", xnHandler -> {
                xnHandler.execute("exc-handler");
            });

            // Tell the reminder workflow to stop
            thread.mutate(allApproved, VariableMutationType.ASSIGN, true);
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

            thread.waitForThreads(SpawnedThreads.of(p1Thread, p2Thread, p3Thread));

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

            thread.waitForThreads(SpawnedThreads.of(child1, child2));
        });
    }

    @LHWorkflow("handle-exception-on-children")
    public Workflow buildHandleExceptionOnChildren() {
        return new WorkflowImpl("handle-exception-on-children", wf -> {
            WfRunVariable threadsToSpawn =
                    wf.addVariable("to-spawn", VariableType.JSON_ARR).required();
            WfRunVariable emptyJson = wf.addVariable("json-obj", VariableType.JSON_OBJ);

            SpawnedThreads childThreads = wf.spawnThreadForEach(
                    threadsToSpawn,
                    "child",
                    child -> {
                        WfRunVariable input =
                                child.addVariable(WorkflowThread.HANDLER_INPUT_VAR, VariableType.JSON_OBJ);

                        // We want to be able to handle two different types of cases:
                        // 1. The child thread fails immediately (i.e. in the same Command) when it's started
                        // 2. The child thread fails later during execution (i.e. in a later Command)
                        //
                        // Putting in a TaskRun that is optionally executed allows us to test both cases with the
                        // same WfSpec.
                        child.doIf(
                                child.condition(input.jsonPath("$.executeTask"), Comparator.EQUALS, true), ifBody -> {
                                    ifBody.execute("add-1", 136);
                                });

                        child.doIf(
                                child.condition(
                                        input.jsonPath("$.endResult"), Comparator.EQUALS, "exception-but-recover"),
                                ifBody -> {
                                    ifBody.fail("exception-but-recover", "failed due to exception");
                                });
                        child.doIf(
                                child.condition(
                                        input.jsonPath("$.endResult"), Comparator.EQUALS, "exception-but-still-fail"),
                                ifBody -> {
                                    ifBody.fail("exception-but-still-fail", "failed due to exception");
                                });
                        child.doIf(
                                child.condition(
                                        input.jsonPath("$.endResult"), Comparator.EQUALS, "exception-dont-handle"),
                                ifBody -> {
                                    ifBody.fail("exception-dont-handle", "failed due to exception");
                                });

                        child.doIf(
                                child.condition(input.jsonPath("$.endResult"), Comparator.EQUALS, "error"), ifBody -> {
                                    // Cause a VarSubError
                                    ifBody.mutate(
                                            emptyJson.jsonPath("$.notarealpath"),
                                            VariableMutationType.MULTIPLY,
                                            emptyJson.jsonPath("$.asdfasdf"));
                                });
                    },
                    Map.of());

            WaitForThreadsNodeOutput wftn = wf.waitForThreads(childThreads);
            wftn.handleErrorOnChild(LHErrorType.VAR_SUB_ERROR, handler -> {});
            wftn.handleExceptionOnChild("exception-but-recover", handler -> {});
            wftn.handleExceptionOnChild("exception-but-still-fail", handler -> {
                handler.fail("still-fail", "should still fail");
            });
        });
    }

    @Test
    void testSpawnThreadForEachHappyPath() {
        workflowVerifier
                .prepareRun(
                        handleExceptionOnChildren,
                        Arg.of("to-spawn", List.of(new MyInput("succeed", false), new MyInput("succeed", true))))
                .waitForStatus(COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(3);
                })
                .thenVerifyAllTaskRuns(1, taskRuns -> {
                    Assertions.assertThat(taskRuns).isEmpty();
                })
                .thenVerifyAllTaskRuns(2, taskRuns -> {
                    Assertions.assertThat(taskRuns.size()).isEqualTo(1);
                })
                .start();
    }

    @Test
    void testExceptionPropagatedToParentWhenNoHandler() {
        workflowVerifier
                .prepareRun(
                        handleExceptionOnChildren,
                        Arg.of("to-spawn", List.of(new MyInput("exception-dont-handle", true))))
                .waitForStatus(EXCEPTION)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(2);
                })
                .thenVerifyNodeRun(0, 2, nr -> {
                    assertThat(nr.getFailuresCount()).isEqualTo(1);
                    Failure latestFailure = nr.getFailures(0);
                    assertThat(latestFailure.getFailureName()).isEqualTo("exception-dont-handle");
                })
                .start();
    }

    @Test
    void testOtherThreadHaltedWhenFirstFailsWithoutHandler() {
        workflowVerifier
                .prepareRun(
                        handleExceptionOnChildren,
                        Arg.of(
                                "to-spawn",
                                List.of(
                                        // First thread fails immediately (before second thread finishes)
                                        new MyInput("exception-dont-handle", false),
                                        // We will verify that second thread goes to HALTING then HALTED.
                                        // To ensure that it goes to another command, we make it execute the
                                        // task.
                                        new MyInput("success", true))))
                .waitForThreadRunStatus(1, LHStatus.EXCEPTION)
                .waitForThreadRunStatus(2, LHStatus.HALTED)
                .waitForStatus(EXCEPTION)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(3);

                    // Ensure that the second thread didn't advance past the taskrun
                    Assertions.assertThat(wfRun.getThreadRunsList().get(2).getCurrentNodePosition())
                            .isEqualTo(2);
                })
                .thenVerifyNodeRun(2, 2, nr -> {
                    // Make sure the second thread still executed the task
                    assertThat(nr.getNodeTypeCase()).isEqualTo(NodeTypeCase.TASK);
                    assertThat(nr.getStatus()).isEqualByComparingTo(LHStatus.COMPLETED);
                })
                .start();
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
