package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitForThread;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitingThreadStatus;
import io.littlehorse.sdk.common.util.Arg;
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
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class SpawnThreadsForEachWithFailureHandlerTest {

    @LHWorkflow("handle-exception-on-children")
    private Workflow handleExceptionOnChildren;

    private WorkflowVerifier verifier;

    private static final String SUCCEED = "succeed";
    private static final String EXCEPTION_BUT_STILL_FAIL = "exception-but-still-fail";
    private static final String EXCEPTION_BUT_RECOVER = "exception-but-recover";
    private static final String EXCEPTION_DONT_HANDLE = "exception-dont-handle";
    private static final String STILL_FAILED = "still-failed";

    @Test
    void testSpawnThreadForEachHappyPath() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of("to-spawn", List.of(new MyInput(SUCCEED, false), new MyInput(SUCCEED, true))))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(3);
                })
                .thenVerifyAllTaskRuns(taskRuns -> {
                    Assertions.assertThat(taskRuns.stream().filter(task -> {
                                return task.getSource()
                                                .getTaskNode()
                                                .getNodeRunId()
                                                .getThreadRunNumber()
                                        == 1;
                            }))
                            .isEmpty();

                    Assertions.assertThat(taskRuns.stream().filter(task -> {
                                return task.getSource()
                                                .getTaskNode()
                                                .getNodeRunId()
                                                .getThreadRunNumber()
                                        == 2;
                            }))
                            .hasSize(1);
                })
                .start();
    }

    @Test
    void testExceptionPropagatedToParentWhenNoHandler() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of("to-spawn", List.of(new MyInput(EXCEPTION_DONT_HANDLE, true))))
                .waitForStatus(LHStatus.EXCEPTION)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(2);
                })
                .thenVerifyNodeRun(0, 2, nr -> {
                    assertThat(nr.getFailuresCount()).isEqualTo(1);
                    Failure latestFailure = nr.getFailures(0);
                    assertThat(latestFailure.getFailureName()).isEqualTo(EXCEPTION_DONT_HANDLE);
                })
                .start();
    }

    @Test
    void testFirstThreadHasHandlerAndSecondDoesnt() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of(
                                "to-spawn",
                                List.of(
                                        new MyInput(EXCEPTION_BUT_RECOVER, false),
                                        new MyInput(EXCEPTION_BUT_STILL_FAIL, true))))
                .waitForThreadRunStatus(1, LHStatus.EXCEPTION)
                .waitForThreadRunStatus(2, LHStatus.EXCEPTION)
                .waitForThreadRunStatus(3, LHStatus.COMPLETED)
                .waitForThreadRunStatus(4, LHStatus.EXCEPTION)
                .waitForStatus(LHStatus.EXCEPTION)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(5);
                })
                .thenVerifyNodeRun(0, 2, nr -> {
                    assertThat(nr.getFailuresCount()).isEqualTo(1);
                    Failure latestFailure = nr.getFailures(0);
                    assertThat(latestFailure.getFailureName()).isEqualTo(STILL_FAILED);
                })
                .start();
    }

    @Test
    void testFirstThreadSucceedsSecondThreadRecovers() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of(
                                "to-spawn",
                                List.of(new MyInput(EXCEPTION_BUT_RECOVER, false), new MyInput(SUCCEED, true))))
                .waitForThreadRunStatus(1, LHStatus.EXCEPTION)
                .waitForThreadRunStatus(2, LHStatus.COMPLETED)
                .waitForThreadRunStatus(3, LHStatus.COMPLETED)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(4);
                })
                .start();
    }

    @Test
    void testOtherThreadHaltedWhenFirstFailsWithoutHandler() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of(
                                "to-spawn",
                                List.of(
                                        // First thread fails immediately (before second thread finishes)
                                        new MyInput(EXCEPTION_DONT_HANDLE, false),
                                        // We will verify that second thread goes to HALTING then HALTED.
                                        // To ensure that it goes to another command, we make it execute the
                                        // task.
                                        new MyInput("success", true))))
                .waitForThreadRunStatus(1, LHStatus.EXCEPTION)
                .waitForThreadRunStatus(2, LHStatus.HALTED)
                .waitForStatus(LHStatus.EXCEPTION)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(3);

                    // Ensure that the second thread didn't advance past the taskrun
                    Assertions.assertThat(wfRun.getThreadRunsList().get(2).getCurrentNodePosition())
                            .isEqualTo(2);
                })
                .thenVerifyNodeRun(2, 2, nr -> {
                    // Make sure the second thread still executed the task
                    assertThat(nr.getNodeTypeCase()).isEqualTo(NodeTypeCase.TASK);
                    assertThat(nr.getStatus()).isEqualByComparingTo(LHStatus.HALTED);
                })
                .thenVerifyTaskRun(2, 2, task -> {
                    assertThat(task.getStatus()).isEqualTo(TaskStatus.TASK_SUCCESS);
                })
                .start();
    }

    // When one thread fails without a handler, then the NodeRun fails and it causes the
    // other children to be halted.
    @Test
    void testFailureHandlerIgnoredIfWaitForThreadsRunAlreadyFailed() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of(
                                "to-spawn",
                                List.of(
                                        // The first thread will execute a task before failing
                                        new MyInput(EXCEPTION_BUT_RECOVER, true),
                                        // The second thread immediately fails. Therefore, the
                                        // WaitForThreadsRun is failed before there is time to handle
                                        // the failure of the first thread.
                                        new MyInput(EXCEPTION_DONT_HANDLE, false))))
                .waitForStatus(LHStatus.EXCEPTION)
                .thenVerifyWfRun(wfRun -> {
                    // Should NOT have an exception handler for the first thread
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(3);
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.HALTED);
                })
                .thenVerifyNodeRun(0, 2, nr -> {
                    assertThat(nr.getFailuresCount()).isEqualTo(1);
                    Failure latestFailure = nr.getFailures(0);
                    assertThat(latestFailure.getFailureName()).isEqualTo(EXCEPTION_DONT_HANDLE);
                })
                .start();
    }

    @Test
    void testFirstThreadRecoversSecondThreadFinishes() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of(
                                "to-spawn",
                                List.of(
                                        // First thread fails immediately but exception handler passes
                                        new MyInput(EXCEPTION_BUT_RECOVER, false),
                                        // We will verify that second thread goes to HALTING then HALTED.
                                        // To ensure that it goes to another command, we make it execute the
                                        // task.
                                        new MyInput(SUCCEED, true))))
                .waitForThreadRunStatus(1, LHStatus.EXCEPTION)
                .waitForThreadRunStatus(2, LHStatus.COMPLETED)
                .waitForThreadRunStatus(3, COMPLETED)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyNodeRun(0, 2, nr -> {
                    // Make sure the second thread still executed the task
                    assertThat(nr.getNodeTypeCase()).isEqualTo(NodeTypeCase.WAIT_FOR_THREADS);

                    WaitForThreadsRun wftr = nr.getWaitForThreads();
                    WaitForThread oneThatFailed = wftr.getThreads(0);
                    assertThat(oneThatFailed.getThreadStatus()).isEqualTo(LHStatus.EXCEPTION);
                    assertThat(oneThatFailed.getWaitingStatus())
                            .isEqualTo(WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED);
                })
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRuns(3).getParentThreadId()).isEqualTo(1);
                    assertThat(wfRun.getThreadRuns(3).getType()).isEqualTo(ThreadType.FAILURE_HANDLER);
                })
                .start();
    }

    @Test
    void testBothThreadsFinishBeforeArrivingAtNode() {
        verifier.prepareRun(
                        handleExceptionOnChildren,
                        Arg.of(
                                "to-spawn",
                                List.of(
                                        // First thread succeeds immediately
                                        new MyInput(SUCCEED, false),
                                        // Second thread succeeds immediately
                                        new MyInput(SUCCEED, true))))
                .waitForStatus(LHStatus.COMPLETED)
                .start();
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
                                child.condition(input.jsonPath("$.executeTask"), Operation.EQUALS, true), ifBody -> {
                                    ifBody.execute("dummy-task-spawnthreadtest");
                                });

                        child.doIf(
                                child.condition(
                                        input.jsonPath("$.endResult"), Operation.EQUALS, EXCEPTION_BUT_RECOVER),
                                ifBody -> {
                                    ifBody.fail(EXCEPTION_BUT_RECOVER, "failed due to exception");
                                });
                        child.doIf(
                                child.condition(
                                        input.jsonPath("$.endResult"), Operation.EQUALS, EXCEPTION_BUT_STILL_FAIL),
                                ifBody -> {
                                    ifBody.fail(EXCEPTION_BUT_STILL_FAIL, "failed due to exception");
                                });
                        child.doIf(
                                child.condition(
                                        input.jsonPath("$.endResult"), Operation.EQUALS, EXCEPTION_DONT_HANDLE),
                                ifBody -> {
                                    ifBody.fail(EXCEPTION_DONT_HANDLE, "failed due to exception");
                                });

                        child.doIf(
                                child.condition(input.jsonPath("$.endResult"), Operation.EQUALS, "error"), ifBody -> {
                                    // Cause a VarSubError
                                    ifBody.mutate(
                                            emptyJson.jsonPath("$.notarealpath"),
                                            Operation.MULTIPLY,
                                            emptyJson.jsonPath("$.asdfasdf"));
                                });
                    },
                    Map.of());

            WaitForThreadsNodeOutput wftn = wf.waitForThreads(childThreads);
            wftn.handleErrorOnChild(LHErrorType.VAR_SUB_ERROR, handler -> {});

            wftn.handleExceptionOnChild(EXCEPTION_BUT_RECOVER, handler -> {
                handler.execute("dummy-task-spawnthreadtest");
            });

            wftn.handleExceptionOnChild(EXCEPTION_BUT_STILL_FAIL, handler -> {
                handler.fail(STILL_FAILED, "should still fail");
            });
        });
    }

    @LHTaskMethod("dummy-task-spawnthreadtest")
    public String obiwan() {
        return "hello there";
    }
}
