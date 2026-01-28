package e2e;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.RunChildWfNodeRun;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.SpawnedChildWf;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {"run-child-wf-ext-evt"})
public class RunChildWfTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("test-dynamic-child-workflow")
    private Workflow dynamicChildWf;

    @Test
    void cannotReferToNonexistingWfSpec() {
        String parent = randomString();
        String child = randomString();
        Workflow invalid = Workflow.newWorkflow(parent, wf -> {
            wf.runWf(child, Map.of());
        });

        Assertions.assertThatThrownBy(() -> {
                    invalid.registerWfSpec(client);
                })
                .matches(exn -> {
                    return exn instanceof StatusRuntimeException;
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("could not find wfspec");
                });
    }

    @Test
    void validateInputstToChildWfSpec() {
        String parent = randomString();
        String child = randomString();
        Workflow childWf = Workflow.newWorkflow(child, wf -> {
            wf.declareStr("required-input").required();
        });

        Workflow invalidParent = Workflow.newWorkflow(parent, wf -> {
            wf.runWf(child, Map.of());
        });

        childWf.registerWfSpec(client);

        Assertions.assertThatThrownBy(() -> {
                    invalidParent.registerWfSpec(client);
                })
                .matches(exn -> {
                    return exn instanceof StatusRuntimeException;
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().contains("required-input");
                });
    }

    @Test
    void shouldRunLatestRevisionOfPinnedWfSpec() {
        String child = "child-" + randomString();
        String parent = "parent-" + randomString();

        // Child Version 0.0
        Workflow.newWorkflow(child, wf -> {
                    wf.declareStr("user-id").required();
                })
                .registerWfSpec(client);

        // Parent should refer to Child Version 0.X
        Workflow.newWorkflow(parent, wf -> {
                    wf.runWf(child, Map.of("user-id", "obi-wan"));
                })
                .registerWfSpec(client);

        // Child Version 0.1
        Workflow.newWorkflow(child, wf -> {
                    wf.declareStr("user-id").required();
                    wf.declareStr("some-hidden-var");
                })
                .registerWfSpec(client);

        // Child Version 1.0
        Workflow.newWorkflow(child, wf -> {
                    wf.declareStr("new-required-variable").required();
                    wf.declareBool("not-used");
                })
                .registerWfSpec(client);

        WfSpecId lastChildWfSpecId = WfSpecId.newBuilder()
                .setName(child)
                .setMajorVersion(1)
                .setRevision(0)
                .build();
        Awaitility.await().ignoreExceptions().atMost(Duration.ofSeconds(1)).until(() -> {
            client.getWfSpec(lastChildWfSpecId);
            return true;
        });

        WfRun parentWfRun =
                client.runWf(RunWfRequest.newBuilder().setWfSpecName(parent).build());
        WfRunId childWfRunId = client.getNodeRun(NodeRunId.newBuilder()
                        .setWfRunId(parentWfRun.getId())
                        .setPosition(1)
                        .build())
                .getRunChildWf()
                .getChildWfRunId();

        WfRun childWfRun = client.getWfRun(childWfRunId);

        Assertions.assertThat(childWfRun.getWfSpecId().getMajorVersion()).isEqualTo(0);
        Assertions.assertThat(childWfRun.getWfSpecId().getRevision()).isEqualTo(1);
    }

    @Test
    void childWorkflowBusinessExceptionsPropagateToParent() {
        String child = "child-fail-" + randomString();
        String parent = "parent-fail-" + randomString();

        Workflow.newWorkflow(child, wf -> {
                    wf.fail("some-exn", "a business message");
                })
                .registerWfSpec(client);

        Workflow.newWorkflow(parent, wf -> {
                    wf.waitForChildWf(wf.runWf(child, Map.of()));
                })
                .registerWfSpec(client);

        Awaitility.await().ignoreExceptions().atMost(Duration.ofSeconds(1)).until(() -> {
            client.getWfSpec(WfSpecId.newBuilder().setName(parent).build());
            return true;
        });

        WfRunId parentId = client.runWf(
                        RunWfRequest.newBuilder().setWfSpecName(parent).build())
                .getId();
        NodeRun failedNode = client.getNodeRun(
                NodeRunId.newBuilder().setPosition(2).setWfRunId(parentId).build());

        Assertions.assertThat(failedNode.getFailuresCount()).isEqualTo(1);

        Failure failure = failedNode.getFailures(0);

        Assertions.assertThat(failure.getFailureName()).isEqualTo("some-exn");
    }

    @Test
    void childOutputsAreReturned() {
        String child = "child-return-" + randomString();
        String parent = "parent-return-" + randomString();

        Workflow.newWorkflow(child, wf -> {
                    wf.complete("hello");
                })
                .registerWfSpec(client);

        Workflow.newWorkflow(parent, wf -> {
                    WfRunVariable resultFromChild = wf.declareStr("result-from-child");
                    resultFromChild.assign(wf.waitForChildWf(wf.runWf(child, Map.of())));
                })
                .registerWfSpec(client);

        Awaitility.await().ignoreExceptions().atMost(Duration.ofSeconds(1)).until(() -> {
            client.getWfSpec(WfSpecId.newBuilder().setName(parent).build());
            return true;
        });

        WfRunId parentId = client.runWf(
                        RunWfRequest.newBuilder().setWfSpecName(parent).build())
                .getId();
        Variable result = client.getVariable(VariableId.newBuilder()
                .setName("result-from-child")
                .setWfRunId(parentId)
                .build());

        Assertions.assertThat(result.getValue().getStr()).isEqualTo("hello");
    }

    @Test
    void shouldRunDynamicChildWorkflow() {
        final String childWfName = UUID.randomUUID().toString();
        final String invalidChildWfName = UUID.randomUUID().toString();
        Workflow emptyWorkflow = Workflow.newWorkflow(childWfName, wf -> {
            // Empty workflow
        });

        emptyWorkflow.registerWfSpec(client);

        verifier.prepareRun(dynamicChildWf, Arg.of("wf-name", childWfName))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.hasRunChildWf()).isTrue();
                    RunChildWfNodeRun childWfNodeRun = nodeRun.getRunChildWf();
                    Assertions.assertThat(childWfNodeRun.hasChildWfRunId()).isTrue();
                    Assertions.assertThat(childWfNodeRun.hasWfSpecId()).isTrue();
                })
                .start();

        verifier.prepareRun(dynamicChildWf, Arg.of("wf-name", invalidChildWfName))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    List<Failure> nodeRunFailures = nodeRun.getFailuresList();
                    Assertions.assertThat(nodeRunFailures).hasSize(1);
                    Failure failure = nodeRunFailures.getFirst();
                    Assertions.assertThat(failure.getFailureName()).isEqualTo("CHILD_FAILURE");
                    Assertions.assertThat(failure.getMessage()).isEqualTo("Couldn't find WfSpec " + invalidChildWfName);
                    Assertions.assertThat(nodeRun.hasRunChildWf()).isTrue();
                    RunChildWfNodeRun childWfNodeRun = nodeRun.getRunChildWf();
                    Assertions.assertThat(childWfNodeRun.hasChildWfRunId()).isFalse();
                    Assertions.assertThat(childWfNodeRun.hasWfSpecId()).isFalse();
                })
                .start();
    }

    @LHWorkflow("test-dynamic-child-workflow")
    public Workflow dynamicChildWorkflow() {
        return Workflow.newWorkflow("test-dynamic-child-workflow", wf -> {
            WfRunVariable required = wf.declareStr("wf-name").required();
            SpawnedChildWf spawnedChildWf = wf.runWf(required, Map.of());
            wf.waitForChildWf(spawnedChildWf);
        });
    }

    // Randomness guarantees that there are not conflicts between test runs.
    private String randomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
