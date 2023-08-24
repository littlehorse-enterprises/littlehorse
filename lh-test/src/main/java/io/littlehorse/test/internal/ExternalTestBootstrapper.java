package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalTestBootstrapper implements TestBootstrapper {

    private static final String LH_CONFIG_FILE = ".config/littlehorse.config";
    private Path configPath = Path.of(System.getProperty("user.home"), LH_CONFIG_FILE);

    private final LHWorkerConfig workerConfig;
    private final LHPublicApiBlockingStub lhClient;

    public ExternalTestBootstrapper() throws IOException {
        if (Files.notExists(configPath)) {
            throw new IllegalStateException(String.format("Configuration file %s doesn't exist", LH_CONFIG_FILE));
        }
        workerConfig = new LHWorkerConfig(configPath.toString());
        lhClient = workerConfig.getBlockingStub();
    }

    @Override
    public LHWorkerConfig getWorkerConfig() {
        return workerConfig;
    }

    @Override
    public LHPublicApiBlockingStub getLhClient() {
        return lhClient;
    }
}
