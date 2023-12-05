package e2e;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {"basic-test-event"})
public class ExternalEventTest {

    private static final String EVENT_NAME = "basic-test-event";

    private WorkflowVerifier verifier;

    @LHWorkflow("basic-ext-evt")
    private Workflow basicExtEvt;

    @Test
    public void sendEventBeforeWfRun() {
        LHPublicApiBlockingStub stub = verifier.getLhClient();
        String wfRunId = UUID.randomUUID().toString();

        // Send before executing wfRun
        sendEvent(wfRunId, EVENT_NAME, "some-content");

        // Run workflow. TODO: Put this into test framework.
        stub.runWf(RunWfRequest.newBuilder()
                .setWfSpecName("basic-ext-evt")
                .setId(wfRunId)
                .build());

        // This could also be put into the test framework; however, at least we are using
        // awaitility so it avoids the flakiness we had in the old framework.
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> {
                    WfRun wfRun = stub.getWfRun(WfRunId.newBuilder().setId(wfRunId).build());
                    return wfRun.getStatus() == LHStatus.COMPLETED;
                });

        // Verify variable
        Variable var = stub.getVariable(VariableId.newBuilder().setWfRunId(WfRunId.newBuilder().setId(wfRunId)).setName("evt-output").setThreadRunNumber(0).build());
        Assertions.assertEquals(var.getValue().getStr(), "some-content");
    }

    

    private void sendEvent(String wfRunId, String eventName, Object content) {
        verifier.getLhClient()
                .putExternalEvent(PutExternalEventRequest.newBuilder()
                        .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(eventName))
                        .setContent(LHLibUtil.objToVarVal(content))
                        .setWfRunId(WfRunId.newBuilder().setId(wfRunId))
                        .build());
    }

    @LHWorkflow("basic-ext-evt")
    public Workflow buildWorkflow() {
        return Workflow.newWorkflow("basic-ext-evt", (wf) -> {
            WfRunVariable evtOutput = wf.addVariable("evt-output", VariableType.STR);
            wf.mutate(evtOutput, VariableMutationType.ASSIGN, wf.waitForEvent("ao-my-event"));
        });
    }
}
