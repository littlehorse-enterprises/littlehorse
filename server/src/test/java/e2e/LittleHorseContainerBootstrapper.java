package e2e;

import io.littlehorse.container.LittleHorseContainer;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.test.internal.TestBootstrapper;
import org.testcontainers.utility.DockerImageName;

public class LittleHorseContainerBootstrapper implements TestBootstrapper {

    private final LittleHorseContainer littlehorse = new LittleHorseContainer(
            DockerImageName.parse("apache/kafka-native:latest"),
            DockerImageName.parse("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:latest"));

    public LittleHorseContainerBootstrapper() {
        littlehorse.start();
    }

    @Override
    public LHConfig getWorkerConfig() {
        return new LHConfig(littlehorse.getProperties());
    }
}
