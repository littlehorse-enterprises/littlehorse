package io.littlehorse.common.app;

import io.littlehorse.common.config.CanaryConfig;

public interface Bootstrap {
    void initialize(CanaryConfig config) throws BoostrapInitializationException;

    void shutdown();
}
