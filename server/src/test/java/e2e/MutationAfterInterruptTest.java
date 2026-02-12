package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Both of these test cases failed prior to this PR, and they demonstrate the concerns outlined
 * in GitHub Issue #656.
 */
@LHTest(
        externalEventNames = {
            MutationAfterInterruptTest.INTERRUPT_TRIGGER,
            MutationAfterInterruptTest.COMPLETE_INTERRUPT_EVENT,
            MutationAfterInterruptTest.PARENT_EVENT,
        })
public class MutationAfterInterruptTest {

    public static final String INTERRUPT_TRIGGER = "mutation-after-interrupt-trigger";
    public static final String PARENT_EVENT = "mutation-after-interrupt-parent-event";
    public static final String COMPLETE_INTERRUPT_EVENT = "mutation-after-interrupt-child-event";

    @LHWorkflow("mutation-after-interrupt-on-task")
    public Workflow mutationAfterInterruptOnTask;

    @LHWorkflow("mutation-after-interrupt-on-extevt")
    public Workflow mutationAfterInterruptOnExtEvt;

    private WorkflowVerifier verifier;

    @Test
    void variableMutationsOnTaskNodeShouldWorkAfterInterrupt() {
        verifier.prepareRun(mutationAfterInterruptOnTask)
                .thenSendExternalEventWithContent(INTERRUPT_TRIGGER, Map.of())
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    Assertions.assertThat(taskRun.getStatus()).isIn(TaskStatus.TASK_SCHEDULED, TaskStatus.TASK_RUNNING);
                })
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isIn(LHStatus.HALTED, LHStatus.HALTING);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "task-output", variable -> {
                    Assertions.assertThat(variable.getStr()).isEqualTo("hello there");
                })
                .start();
    }

    @Test
    void variableMutationsOnExternalEventNodeShouldWorkAfterInterrupt() {
        verifier.prepareRun(mutationAfterInterruptOnExtEvt)
                .thenSendExternalEventWithContent(INTERRUPT_TRIGGER, Map.of())
                .thenSendExternalEventWithContent(PARENT_EVENT, "Obi-Wan Kenobi")
                .thenSendExternalEventWithContent(COMPLETE_INTERRUPT_EVENT, Map.of())
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .thenVerifyVariable(0, "event-content", variable -> {
                    Assertions.assertThat(variable.getStr()).isEqualTo("Obi-Wan Kenobi");
                })
                .start();
    }

    @LHWorkflow("mutation-after-interrupt-on-task")
    public Workflow getMutationAfterInterruptOnTaskWf() {
        return Workflow.newWorkflow("mutation-after-interrupt-on-task", wf -> {
            WfRunVariable taskOutputVar = wf.addVariable("task-output", VariableType.STR);

            NodeOutput taskOutput = wf.execute("mutation-after-interrupt-return-string-slowly");
            wf.mutate(taskOutputVar, Operation.ASSIGN, taskOutput);

            wf.registerInterruptHandler(INTERRUPT_TRIGGER, handler -> {
                handler.execute("mutation-after-interrupt-return-string-slowly");
            });
        });
    }

    @LHTaskMethod("mutation-after-interrupt-return-string-slowly")
    public String returnStringSlowly() throws InterruptedException {
        Thread.sleep(200);
        return "hello there";
    }

    @LHWorkflow("mutation-after-interrupt-on-extevt")
    public Workflow getMutationAfterInterruptOnExtEvtWf() {
        return Workflow.newWorkflow("mutation-after-interrupt-on-ext-evt", wf -> {
            WfRunVariable extEvtOutputVar = wf.addVariable("event-content", VariableType.STR);

            NodeOutput extEvtOutput = wf.waitForEvent(PARENT_EVENT);
            wf.mutate(extEvtOutputVar, Operation.ASSIGN, extEvtOutput);

            wf.registerInterruptHandler(INTERRUPT_TRIGGER, handler -> {
                handler.waitForEvent(COMPLETE_INTERRUPT_EVENT);
            });
        });
    }
}
