package e2e;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.test.internal.TestBootstrapper;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalTestBootstrapper implements TestBootstrapper {

    private static final String LH_CONFIG_FILE = ".config/littlehorse.config";
    private final LHConfig workerConfig;
    private Path configPath = Path.of(System.getProperty("user.home"), LH_CONFIG_FILE);

    public ExternalTestBootstrapper() {
        if (Files.notExists(configPath)) {
            throw new IllegalStateException(String.format("Configuration file %s doesn't exist", LH_CONFIG_FILE));
        }
        workerConfig = new LHConfig(configPath.toString());
    }

    @Override
    public LHConfig getWorkerConfig() {
        return workerConfig;
    }
}
