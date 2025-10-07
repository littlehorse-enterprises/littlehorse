package e2e;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {"run-child-wf-ext-evt"})
public class RunChildWfTest {

    private LittleHorseBlockingStub client;

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
        // TODO
    }

    @Test
    void canPassInputsToChildWorkflow() {
        // TODO
    }

    @Test
    void childWorkflowBusinessExceptionsPropagateToParent() {
        // TODO
    }

    @Test
    void childErrorPropagatesToParentAsChildFailed() {
        // TODO
    }

    // Randomness guarantees that there are not conflicts between test runs.
    private String randomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
