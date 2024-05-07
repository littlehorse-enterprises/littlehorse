package io.littlehorse.sdk.worker.internal;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseStub;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.worker.LHTaskWorkerHealth;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.List;

public class LHServerConnectionManager {

    private final RebalanceThread rebalanceThread;
    private final TaskDef taskDef;
    private final LHLivenessController livenessController;
    private static final long HEARTBEAT_INTERVAL_MS = 5000L;

    public LHServerConnectionManager(
            TaskDef taskDef,
            LittleHorseStub bootstrapStub,
            String taskWorkerId,
            String connectListenerName,
            LHLivenessController livenessController,
            Method taskMethod,
            List<VariableMapping> mappings,
            Object executable,
            LHConfig config) {
        this.rebalanceThread = new RebalanceThread(
                bootstrapStub,
                taskWorkerId,
                connectListenerName,
                taskDef,
                taskMethod,
                mappings,
                executable,
                new LHTaskExecutor(config.getWorkerThreads()),
                config,
                livenessController,
                HEARTBEAT_INTERVAL_MS);
        this.livenessController = livenessController;
        this.taskDef = taskDef;
    }

    public void start() {
        rebalanceThread.start();
    }

    public void close() {
        livenessController.stop();
    }

    public LHTaskWorkerHealth healthStatus() {
        return livenessController.healthStatus();
    }
}
