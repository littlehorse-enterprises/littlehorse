package e2e;

import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.LHWorkflowEvent;
import io.littlehorse.test.WorkflowVerifier;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@LHTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkflowEventsTest {

    @LHWorkflow("events")
    private Workflow eventsWf;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @Test
    @Order(1)
    void shouldBeAbleToGetWorkflowEventThrownFirst() {
        verifier.prepareRun(eventsWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    WorkflowEvent result = client.withDeadlineAfter(1, TimeUnit.SECONDS)
                            .awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                                    .setWfRunId(wfRun.getId())
                                    .addEventDefIds(
                                            WorkflowEventDefId.newBuilder().setName("user-created"))
                                    .build());

                    Assertions.assertThat(result.getContent().getStr()).isEqualTo("hello there");
                })
                .start();
    }

    @Test
    @Order(2) // has to go after the first one, which registers the WfSpec
    void shouldBeAbleToGetWorkflowEventThrownAfterRequest() {
        WfRunId id = WfRunId.newBuilder().setId(UUID.randomUUID().toString()).build();

        // Delay the WfRun so that the rpc AwaitWorkflowEvent gets there before the WorkflowEvent is thrown.
        new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                        // Nothing to do
                    }
                    client.runWf(RunWfRequest.newBuilder()
                            .setWfSpecName("events")
                            .setId(id.getId())
                            .build());
                })
                .start();

        WorkflowEvent event = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                .setWfRunId(id)
                .addEventDefIds(WorkflowEventDefId.newBuilder().setName("user-created"))
                .build());
        Assertions.assertThat(event.getContent().getStr()).isEqualTo("hello there");
    }

    @LHWorkflow("events")
    public Workflow eventsWf() {
        return new WorkflowImpl("events", entrypoint -> {
            WfRunVariable input = entrypoint.addVariable("input", "hello there");
            entrypoint.throwEvent("user-created", input);
        });
    }

    @LHWorkflowEvent
    public final PutWorkflowEventDefRequest eventDef = PutWorkflowEventDefRequest.newBuilder()
            .setType(VariableType.STR)
            .setName("user-created")
            .build();
}
