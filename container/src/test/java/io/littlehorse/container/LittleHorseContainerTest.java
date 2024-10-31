package io.littlehorse.container;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Empty;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.Principal;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class LittleHorseContainerTest {

    @Container
    public LittleHorseContainer littlehorse = new LittleHorseContainer(
            DockerImageName.parse("apache/kafka-native:latest"),
            DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest"));

    @Test
    public void simpleTest() {
        LHConfig config = new LHConfig(littlehorse.getProperties());

        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        Principal whoami = client.whoami(Empty.newBuilder().build());
        assertEquals("anonymous", whoami.getId().getId());
    }
}
