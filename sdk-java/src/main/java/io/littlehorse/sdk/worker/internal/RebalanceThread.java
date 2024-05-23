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
import java.util.ArrayList;
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
    private final LHConfig config;
    private final Map<LHHostInfo, List<PollThread>> runningConnections = new ConcurrentHashMap<>();
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

    private PollThread createConnection(LittleHorseGrpc.LittleHorseStub stub, String threadName) {
        return new PollThread(
                threadName,
                stub,
                bootstrapStub,
                taskDef.getId(),
                taskWorkerId,
                config.getTaskWorkerVersion(),
                mappings,
                executable,
                taskMethod);
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
                    for (PollThread removed : runningConnections.remove(runningConnection)) {
                        removed.close();
                    }
                }
                LittleHorseGrpc.LittleHorseStub stub =
                        config.getAsyncStub(runningConnection.getHost(), runningConnection.getPort());

                List<PollThread> originalPollThreads = runningConnections.get(runningConnection);
                ArrayList<PollThread> newPollThreads = new ArrayList<>();

                // This loop replaces each PollThread that stops running with a fresh PollThread
                for (int i = 0; i < originalPollThreads.size(); i++) {
                    PollThread currentPollThread = originalPollThreads.get(i);
                    if (currentPollThread.isRunning()) {
                        newPollThreads.add(currentPollThread);
                    } else {
                        String threadName = String.format("lh-poll-%s", i);
                        PollThread connection = createConnection(stub, threadName);
                        connection.start();
                        newPollThreads.add(connection);
                    }
                }

                runningConnections.put(runningConnection, newPollThreads);
            }
            for (LHHostInfo lhHostInfo : availableHosts) {
                if (!runningConnections.containsKey(lhHostInfo)) {
                    final List<PollThread> connections = new ArrayList<>();
                    LittleHorseGrpc.LittleHorseStub stub =
                            config.getAsyncStub(lhHostInfo.getHost(), lhHostInfo.getPort());
                    for (int i = 0; i < config.getWorkerThreads(); i++) {
                        String threadName = String.format("lh-poll-%s", i);
                        PollThread connection = createConnection(stub, threadName);
                        connection.start();
                        connections.add(connection);
                    }
                    runningConnections.put(lhHostInfo, connections);
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
