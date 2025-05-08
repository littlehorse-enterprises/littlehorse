package e2e;

import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.LHWorkflowEvent;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@LHTest
@Disabled
public class WorkflowEventsTest {

    @LHWorkflow("events")
    private Workflow eventsWf;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @Test
    void shouldBeAbleToGetWorkflowEventThrownFirst() {
        verifier.prepareRun(eventsWf, Arg.of("sleep-time", 0))
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(2))
                .thenVerifyWfRun(wfRun -> {
                    WorkflowEvent result = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("user-created"))
                            .build());

                    Assertions.assertThat(result.getContent().getStr()).isEqualTo("hello there");
                })
                .start();
    }

    @Test
    void shouldBeAbleToGetWorkflowEventThrownAfterRequest() {
        long startTime = System.currentTimeMillis();

        verifier.prepareRun(eventsWf, Arg.of("sleep-time", 2))
                .thenAwaitWorkflowEvent("user-created", event -> {
                    // Verify that we actually slept
                    Assertions.assertThat(System.currentTimeMillis() - startTime)
                            .isGreaterThan(2000);

                    Assertions.assertThat(event.getContent().getStr()).isEqualTo("hello there");
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHWorkflow("events")
    public Workflow eventsWf() {
        return new WorkflowImpl("events", entrypoint -> {
            WfRunVariable sleepTime = entrypoint.addVariable("sleep-time", 0);
            WfRunVariable input = entrypoint.addVariable("input", "hello there");
            entrypoint.sleepSeconds(sleepTime);
            entrypoint.throwEvent("user-created", input);
        });
    }

    @LHWorkflowEvent
    public final PutWorkflowEventDefRequest eventDef = PutWorkflowEventDefRequest.newBuilder()
            .setContentType(ReturnType.newBuilder()
                    .setReturnType(TypeDefinition.newBuilder().setType(VariableType.STR)))
            .setName("user-created")
            .build();
}
