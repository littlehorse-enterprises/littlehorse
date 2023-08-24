package io.littlehorse.driver;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class TestDriverExternal extends TestDriver {

    public TestDriverExternal(Set<Class<?>> tests, int threads) {
        super(tests, threads);
    }

    @Override
    public void setup() {
        workerConfig = new LHWorkerConfig();

        Path configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config");

        if (Files.exists(configPath)) {
            workerConfig = new LHWorkerConfig(configPath.toString());
        }
        try {
            client = workerConfig.getBlockingStub();
        } catch (IOException exn) {
            throw new RuntimeException(exn);
        }
    }
}
