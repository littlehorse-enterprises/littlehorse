package io.littlehorse.sdk.worker.internal;

import io.littlehorse.sdk.worker.LHTaskWorkerHealth;

public interface LHServerConnectionManager {

    void start();

    void close();

    LHTaskWorkerHealth healthStatus();
}
