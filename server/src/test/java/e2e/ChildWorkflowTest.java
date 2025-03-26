package e2e;


import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@LHTest(externalEventNames = {"child-workflow-event"})
public class ChildWorkflowTest {

    private static final String EVENT_NAME = "child-workflow-event";
    private LittleHorseBlockingStub client;

    // Randomness guarantees that there are not conflicts between test runs.
    private String randomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Test
    void shouldRejectInvalidWfSpecReference() {
        Workflow workflow = Workflow.newWorkflow("asfjawepoig-not-a-valid-workflow", wf -> {
            wf.sleepSeconds(1);
        });

        String nonexistentWfSpec = randomString();
        workflow.setParent(nonexistentWfSpec);

        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            workflow.registerWfSpec(client);
        });
    }

    @Test
    void shouldRejectWhenParentNotSpecified() {
        Pair<String, String> parentChild = createParentAndChild();

        // JUnit exceptions don't let you inspect the type of exception, so I just
        // do it manually. Need to assert INVALID_ARGUMENT code.
        StatusRuntimeException caught = null;
        String childWfSpecName = parentChild.getRight();
        try {
            client.runWf(
                    RunWfRequest.newBuilder().setWfSpecName(childWfSpecName).build());
        } catch (StatusRuntimeException exn) {
            caught = exn;
        }
        assertThat(caught).isNotNull();
        Assertions.assertEquals(caught.getStatus().getCode(), Code.INVALID_ARGUMENT);

        cleanupParentChild(parentChild);
    }

    @Test
    void shouldCanPostEventToChild() {
        Pair<String, String> parentChild = createParentAndChild();
        WfRunId parent = client.runWf(RunWfRequest.newBuilder()
                        .setWfSpecName(parentChild.getLeft())
                        .build())
                .getId();

        WfRun child = client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName(parentChild.getRight())
                .setParentWfRunId(parent)
                .build());

        Assertions.assertEquals(child.getId().getParentWfRunId().getId(), parent.getId());

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setWfRunId(child.getId())
                .setContent(LHLibUtil.objToVarVal("ignored"))
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(EVENT_NAME))
                .build());

        child = client.getWfRun(child.getId());
        Assertions.assertEquals(child.getStatus(), LHStatus.COMPLETED);

        cleanupParentChild(parentChild);
    }

    /*
     * Creates a unique parent and child WfSpec. Returns the names of both in a pair.
     * Future work: only do this once for all workflows...?
     */
    private Pair<String, String> createParentAndChild() {
        String parentName = randomString();
        Workflow parent = Workflow.newWorkflow(parentName, wf -> {
            wf.sleepSeconds(0);
        });

        String childName = randomString();
        Workflow child = Workflow.newWorkflow(childName, wf -> {
            wf.waitForEvent(EVENT_NAME);
        });
        child.setParent(parentName);

        // Register parent and child
        parent.registerWfSpec(client);
        child.registerWfSpec(client);

        Awaitility.await()
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    client.getWfSpec(WfSpecId.newBuilder().setName(childName).build());
                    return true;
                });

        return Pair.of(parentName, childName);
    }

    private void cleanupParentChild(Pair<String, String> parentChildPair) {
        client.deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                .setId(WfSpecId.newBuilder().setName(parentChildPair.getKey()))
                .build());
        client.deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                .setId(WfSpecId.newBuilder().setName(parentChildPair.getValue()))
                .build());
    }
}
