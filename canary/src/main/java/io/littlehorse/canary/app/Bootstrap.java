package io.littlehorse.canary.app;

import io.littlehorse.canary.config.CanaryConfig;

public interface Bootstrap {
    void initialize(CanaryConfig config);

    void shutdown();
}
