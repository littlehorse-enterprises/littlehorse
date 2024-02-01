package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalTestBootstrapper implements TestBootstrapper {

    private static final String LH_CONFIG_FILE = ".config/littlehorse.config";
    private final LHConfig workerConfig;
    private final LittleHorseBlockingStub lhClient;
    private Path configPath = Path.of(System.getProperty("user.home"), LH_CONFIG_FILE);

    public ExternalTestBootstrapper() {
        if (Files.notExists(configPath)) {
            throw new IllegalStateException(String.format("Configuration file %s doesn't exist", LH_CONFIG_FILE));
        }
        workerConfig = new LHConfig(configPath.toString());
        lhClient = workerConfig.getBlockingStub();
    }

    @Override
    public LHConfig getWorkerConfig() {
        return workerConfig;
    }

    @Override
    public LittleHorseBlockingStub getLhClient() {
        return lhClient;
    }
}
