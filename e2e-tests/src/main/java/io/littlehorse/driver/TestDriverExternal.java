package io.littlehorse.driver;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class TestDriverExternal extends TestDriver {

    public TestDriverExternal(Set<Class<?>> tests, int threads) {
        super(tests, threads);
    }

    @Override
    public void arrange() {
        workerConfig = new LHWorkerConfig();

        Path configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        );

        if (Files.exists(configPath)) {
            workerConfig = new LHWorkerConfig(configPath.toString());
        }

        client = new LHClient(workerConfig);
    }

    @Override
    public void teardown() {}
}
