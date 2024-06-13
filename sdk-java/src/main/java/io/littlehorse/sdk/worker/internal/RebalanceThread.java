package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.TaskDef;
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
    private final LHConfig config;
    final Map<LHHostInfo, List<PollThread>> runningConnections = new ConcurrentHashMap<>();

    private final LHLivenessController livenessController;
    private final long heartbeatIntervalMs;
    private final PollThreadFactory pollThreadFactory;

    public RebalanceThread(
            LittleHorseGrpc.LittleHorseStub bootstrapStub,
            String taskWorkerId,
            String connectListenerName,
            TaskDef taskDef,
            LHConfig config,
            LHLivenessController livenessController,
            long heartbeatIntervalMs,
            PollThreadFactory pollThreadFactory) {
        this.bootstrapStub = bootstrapStub;
        this.taskWorkerId = taskWorkerId;
        this.connectListenerName = connectListenerName;
        this.taskDef = taskDef;
        this.pollThreadFactory = pollThreadFactory;
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

    PollThread createConnection(LHHostInfo hostInfo, String threadName) {
        return pollThreadFactory.create(threadName, hostInfo);
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
            for (LHHostInfo hostInfo : runningConnections.keySet()) {
                List<PollThread> currentThreads = runningConnections.get(hostInfo);
                List<PollThread> runningThreads = new ArrayList<>();
                for (PollThread currentThread : currentThreads) {
                    if (currentThread.isRunning()) {
                        runningThreads.add(currentThread);
                    }
                }
                int numberMissingPollThreads = config.getWorkerThreads() - runningThreads.size();
                for (int i = 0; i < numberMissingPollThreads; i++) {
                    String threadName = String.format("lh-poll-%s", runningThreads.size() + 1);
                    runningThreads.add(pollThreadFactory.create(threadName, hostInfo));
                }
                runningConnections.put(hostInfo, runningThreads);
            }
            List<LHHostInfo> toBeRemoved = new ArrayList<>();
            for (LHHostInfo lhHostInfo : runningConnections.keySet()) {
                if (!availableHosts.contains(lhHostInfo)) {
                    toBeRemoved.add(lhHostInfo);
                }
            }
            for (LHHostInfo toRemove : toBeRemoved) {
                List<PollThread> pollThreads = runningConnections.get(toRemove);
                for (PollThread pollThread : pollThreads) {
                    pollThread.close();
                }
                runningConnections.remove(toRemove);
            }
            for (LHHostInfo lhHostInfo : availableHosts) {
                if (!runningConnections.containsKey(lhHostInfo)) {
                    final List<PollThread> connections = new ArrayList<>();
                    for (int i = 0; i < config.getWorkerThreads(); i++) {
                        String threadName = String.format("lh-poll-%s", i);
                        PollThread connection = createConnection(lhHostInfo, threadName);
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
