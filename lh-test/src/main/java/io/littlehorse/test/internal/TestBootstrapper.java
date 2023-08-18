package io.littlehorse.test.internal;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;

public interface TestBootstrapper {
    LHWorkerConfig getWorkerConfig();

    LHClient getLhClient();
}
