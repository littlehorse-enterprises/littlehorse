package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Tests that Interrupts cause NodeRun lifecycle to move properly.
 */
@LHTest
public class InterruptLifecycleTest {
    public static final String PARENT_EVENT = "ilt-parent-event";
    public static final String CHILD_EVENT = "ilt-child-event";
    public static final String INTERRUPT_TRIGGER = "ilt-interrupt-trigger";
    public static final String COMPLETE_INTERRUPT_HANDLER = "ilt-complete-interrupt-handler";

    @LHWorkflow("interrupt-lifecycle-test")
    public Workflow interruptLifecycleTest;

    private WorkflowVerifier verifier;
    private ConcurrentHashMap<String, Boolean> pedro = new ConcurrentHashMap<>();

    @Test
    void shouldCompleteWithNoInterrupts() {
        String theKey = UUID.randomUUID().toString();
        pedro.put(theKey, true);
        verifier.prepareRun(interruptLifecycleTest, Arg.of("key", theKey))
                .thenSendExternalEventWithContent(PARENT_EVENT, null)
                .thenSendExternalEventWithContent(CHILD_EVENT, null)
                .thenVerifyWfRun(wfRun -> {
                    pedro.put(theKey, false);
                })
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(3))
                .start();
    }

    @Test
    void shouldCompleteAfterInterruptingTaskRun() {
        String theKey = UUID.randomUUID().toString();
        pedro.put(theKey, true);
        verifier.prepareRun(interruptLifecycleTest, Arg.of("key", theKey))
                .thenSendExternalEventWithContent(PARENT_EVENT, null)
                // Interrupt on taskNode
                .thenSendExternalEventWithContent(INTERRUPT_TRIGGER, null)
                .thenVerifyTaskRun(0, 2, taskRun -> {
                    Assertions.assertThat(taskRun.getStatus())
                            .isIn(TaskStatus.TASK_SCHEDULED, TaskStatus.TASK_RUNNING, TaskStatus.TASK_SUCCESS);
                    pedro.put(theKey, false);
                })
                .thenVerifyNodeRun(0, 2, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isIn(LHStatus.HALTED, LHStatus.HALTING);
                })
                .waitForNodeRunStatus(0, 2, LHStatus.HALTED)
                .start();
    }

    @LHWorkflow("interrupt-lifecycle-test")
    public Workflow getInterruptLifecycleTestWf() {
        return Workflow.newWorkflow("interrupt-lifecycle-test", wf -> {
            WfRunVariable theKey = wf.addVariable("key", VariableType.STR).required();
            wf.registerInterruptHandler(INTERRUPT_TRIGGER, handler -> {
                        handler.waitForEvent(COMPLETE_INTERRUPT_HANDLER).registeredAs(null);
                    })
                    .withEventType(null);
            ;

            wf.waitForEvent(PARENT_EVENT).registeredAs(null);
            wf.execute("dummy-task", theKey);

            // Spawn and wait for child
            wf.waitForThreads(SpawnedThreads.of(wf.spawnThread(
                    child -> {
                        child.waitForEvent(CHILD_EVENT).registeredAs(null);
                    },
                    "child",
                    Map.of())));
        });
    }

    @LHTaskMethod("dummy-task")
    public String obiwan(String theKey) throws InterruptedException {
        while (pedro.containsKey(theKey) && pedro.get(theKey)) {
            Thread.sleep(10);
        }
        return "hello there";
    }
}
