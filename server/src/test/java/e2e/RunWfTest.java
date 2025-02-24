package e2e;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;

public class RunWfTest {
    private LittleHorseBlockingStub client;
  
    @Test
    public void runWfShouldFailWithInvalidId() {
        StatusRuntimeException caught = null;
        try {
            client.runWf(RunWfRequest.newBuilder()
            .setId("my_workflow")
            .setWfSpecName("basic-example")
            .build());
        } catch (StatusRuntimeException exn) {
            caught = exn;
        }
        assertNotNull(caught);
        Assertions.assertThat(caught.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }
}
