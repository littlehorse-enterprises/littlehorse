package io.littlehorse.container;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Empty;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.Principal;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LittleHorseContainerTest {

    @Container
    public LittleHorseCluster littleHorseCluster = LittleHorseCluster.newBuilder()
            .withInstances(2)
            .withKafkaImage("apache/kafka-native:4.0.1")
            .withLittlehorseImage("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:master")
            .build();

    @Test
    public void simpleTest() {
        LHConfig config = new LHConfig(littleHorseCluster.getClientProperties());

        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        Principal whoami = client.whoami(Empty.newBuilder().build());
        assertEquals("anonymous", whoami.getId().getId());
    }
}
