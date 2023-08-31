package io.littlehorse.e2e;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
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
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {"person-1-approves", "person-2-approves", "person-3-approves"})
public class WaitForThreadsTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("wait-for-threads-with-exception-handler")
    private Workflow waitForThreadsWithExceptionHandlerWorkflow;

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
                    thread.spawnThread(buildChildThread.apply(person1Approved, "person-2"), "person-2", null);
            SpawnedThread p3Thread =
                    thread.spawnThread(buildChildThread.apply(person1Approved, "person-3"), "person-3", null);

            NodeOutput nodeOutput = thread.waitForThreads(p1Thread, p2Thread, p3Thread);

            thread.handleException(nodeOutput, "denied-by-user", xnHandler -> {
                xnHandler.execute("exc-handler");
            });

            // Tell the reminder workflow to stop
            thread.mutate(allApproved, VariableMutationType.ASSIGN, true);
        });
    }

    @LHTaskMethod("exc-handler")
    public String exceptionHandler() {
        System.out.println("Ok, handler executed");
        return "result";
    }
}
