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

final class RebalanceThread extends Thread {
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub;
    private final String taskWorkerId;
    private final String connectListenerName;
    private final TaskDef taskDef;
    private static final long HEARTBEAT_INTERVAL_MS = 5000L;
    private final HeartBeatCallback heartBeatCallback = new HeartBeatCallback();
    private final Method taskMethod;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final LHTaskExecutor executor;
    private final LHConfig config;
    private final Map<LHHostInfo, LHServerConnectionV2> runningConnections = new ConcurrentHashMap<>();
    private final LHLivenessController livenessController;

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
            LHLivenessController livenessController) {
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
    }

    @Override
    public void run() {
        while (livenessController.keepWorkerRunning()) {
            doHeartBeat();
            waitForInterval();
        }
    }

    private void doHeartBeat() {
        bootstrapStub.registerTaskWorker(
                RegisterTaskWorkerRequest.newBuilder()
                        .setTaskDefId(taskDef.getId())
                        .setTaskWorkerId(taskWorkerId)
                        .setListenerName(connectListenerName)
                        .build(),
                heartBeatCallback // the callbacks come back to this manager.
                );
    }

    private LHServerConnectionV2 createConnection(LHHostInfo host) {
        LittleHorseGrpc.LittleHorseStub stub = config.getAsyncStub(host.getHost(), host.getPort());
        return new LHServerConnectionV2(
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
            Thread.sleep(HEARTBEAT_INTERVAL_MS);
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
                    LHServerConnectionV2 removed = runningConnections.remove(runningConnection);
                    removed.close();
                }
            }
            for (LHHostInfo lhHostInfo : availableHosts) {
                if (!runningConnections.containsKey(lhHostInfo)) {
                    runningConnections.put(lhHostInfo, createConnection(lhHostInfo));
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            livenessController.notifyWorkerFailure();
        }

        @Override
        public void onCompleted() {}
    }
}
