package e2e;

import io.littlehorse.container.LittleHorseCluster;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.test.internal.TestBootstrapper;

public class LittleHorseContainerBootstrapper implements TestBootstrapper {
    private final LittleHorseCluster littleHorseCluster = LittleHorseCluster.newBuilder()
            .withInstances(2)
            .withKafkaImage("apache/kafka-native:latest")
            .withLittlehorseImage("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:master")
            .build();

    public LittleHorseContainerBootstrapper() {
        littleHorseCluster.start();
    }

    @Override
    public LHConfig getWorkerConfig() {
        return new LHConfig(littleHorseCluster.getClientProperties());
    }
}
