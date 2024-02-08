package io.littlehorse.canary;

import io.littlehorse.canary.config.CanaryConfig;

public abstract class Bootstrap {
    protected final CanaryConfig config;

    protected Bootstrap(final CanaryConfig config) {
        this.config = config;
    }
}
