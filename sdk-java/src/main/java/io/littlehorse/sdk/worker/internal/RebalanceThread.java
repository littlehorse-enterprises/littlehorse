package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class RebalanceThread extends Thread {
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub;
    private final String taskWorkerId;
    private final String connectListenerName;
    private final TaskDef taskDef;
    private final HeartBeatCallback heartBeatCallback = new HeartBeatCallback();
    private final Method taskMethod;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final LHTaskExecutor executor;
    private final LHConfig config;
    private final Map<LHHostInfo, PollingConnection> runningConnections = new ConcurrentHashMap<>();
    private final LHLivenessController livenessController;
    private final long heartbeatIntervalMs;

    public RebalanceThread(
            LittleHorseGrpc.LittleHorseStub bootstrapStub,
            String taskWorkerId,
            String connectListenerName,
            TaskDef taskDef,
            Method taskMethod,
            List<VariableMapping> mappings,
            Object executable,
            LHTaskExecutor executor,
            LHConfig config,
            LHLivenessController livenessController,
            long heartbeatIntervalMs) {
        this.bootstrapStub = bootstrapStub;
        this.taskWorkerId = taskWorkerId;
        this.connectListenerName = connectListenerName;
        this.taskDef = taskDef;
        this.taskMethod = taskMethod;
        this.mappings = mappings;
        this.executable = executable;
        this.executor = executor;
        this.config = config;
        this.livenessController = livenessController;
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    @Override
    public void run() {
        while (livenessController.keepWorkerRunning()) {
            doHeartBeat();
            waitForInterval();
        }
    }

    public void doHeartBeat() {
        bootstrapStub.registerTaskWorker(
                RegisterTaskWorkerRequest.newBuilder()
                        .setTaskDefId(taskDef.getId())
                        .setTaskWorkerId(taskWorkerId)
                        .setListenerName(connectListenerName)
                        .build(),
                heartBeatCallback);
    }

    private PollingConnection createConnection(LHHostInfo host) {
        LittleHorseGrpc.LittleHorseStub stub = config.getAsyncStub(host.getHost(), host.getPort());
        return new PollingConnection(
                executor,
                host,
                stub,
                mappings,
                executable,
                taskMethod,
                taskWorkerId,
                taskDef.getId(),
                config.getTaskWorkerVersion());
    }

    private void waitForInterval() {
        try {
            Thread.sleep(heartbeatIntervalMs);
        } catch (Exception ignored) {
            // Ignored
        }
    }

    private final class HeartBeatCallback implements StreamObserver<RegisterTaskWorkerResponse> {

        @Override
        public void onNext(RegisterTaskWorkerResponse response) {
            livenessController.notifySuccessCall(response);
            List<LHHostInfo> availableHosts = response.getYourHostsList();
            for (LHHostInfo runningConnection : runningConnections.keySet()) {
                if (!availableHosts.contains(runningConnection)) {
                    PollingConnection removed = runningConnections.remove(runningConnection);
                    removed.close();
                }
            }
            for (LHHostInfo lhHostInfo : availableHosts) {
                if (!runningConnections.containsKey(lhHostInfo)
                        || !runningConnections.get(lhHostInfo).isStillRunning()) {
                    runningConnections.put(lhHostInfo, createConnection(lhHostInfo));
                }
            }
            log.info("Current connections: " + runningConnections.values());
        }

        @Override
        public void onError(Throwable t) {
            livenessController.notifyWorkerFailure();
        }

        @Override
        public void onCompleted() {}
    }
}
