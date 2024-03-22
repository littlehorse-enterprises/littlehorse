package io.littlehorse.sdk.worker.internal;

import static io.littlehorse.sdk.worker.LHTaskWorkerHealthReason.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.worker.LHTaskWorkerHealth;
import org.junit.jupiter.api.Test;

class LHLivenessControllerTest {

    @Test
    void healthStatus() {
        LHLivenessController livenessController = new LHLivenessController();
        assertThat(livenessController.healthStatus())
                .isEqualTo(LHTaskWorkerHealth.builder()
                        .isHealthy(true)
                        .reason(HEALTHY)
                        .build());

        livenessController.notifyWorkerFailure();
        assertThat(livenessController.healthStatus())
                .isEqualTo(LHTaskWorkerHealth.builder()
                        .isHealthy(false)
                        .reason(UNHEALTHY)
                        .build());

        livenessController.notifySuccessCall(RegisterTaskWorkerResponse.newBuilder()
                .setIsClusterHealthy(false)
                .build());
        assertThat(livenessController.healthStatus())
                .isEqualTo(LHTaskWorkerHealth.builder()
                        .isHealthy(false)
                        .reason(SERVER_REBALANCING)
                        .build());

        livenessController.notifySuccessCall(RegisterTaskWorkerResponse.newBuilder()
                .setIsClusterHealthy(true)
                .build());
        assertThat(livenessController.healthStatus())
                .isEqualTo(LHTaskWorkerHealth.builder()
                        .isHealthy(true)
                        .reason(HEALTHY)
                        .build());
    }
}
