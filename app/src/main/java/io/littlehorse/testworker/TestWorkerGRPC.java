package io.littlehorse.testworker;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.HostInfoPb;
import io.littlehorse.common.proto.LHPublicApiGrpc;
import io.littlehorse.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import io.littlehorse.common.proto.RegisterTaskWorkerPb;
import io.littlehorse.common.proto.RegisterTaskWorkerReplyPb;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.List;

public class TestWorkerGRPC implements StreamObserver<RegisterTaskWorkerReplyPb> {

    private StreamObserver<RegisterTaskWorkerPb> registryClient;
    private TaskFunc func;
    private String clientId;
    private boolean running;
    private List<SingleServerConnector> runningWorkerThreads;
    private Channel channel;
    private String taskDefName;

    public TestWorkerGRPC(
        String clientId,
        String bootstrapHost,
        int bootstrapPort,
        String taskDefName,
        TaskFunc func
    ) {
        this.clientId = clientId;
        this.channel =
            ManagedChannelBuilder
                .forAddress(bootstrapHost, bootstrapPort)
                .usePlaintext()
                .build();

        LHPublicApiStub stub = LHPublicApiGrpc.newStub(this.channel);
        // this.registryClient = stub.registerTaskWorker(this);
        this.running = true;
        this.func = func;
        this.taskDefName = taskDefName;
        runningWorkerThreads = new ArrayList<>();
    }

    public void start() {
        new Thread(this::startRegistryloop).start();
    }

    private void startRegistryloop() {
        while (running) {
            try {
                this.registryClient.onNext(
                        RegisterTaskWorkerPb
                            .newBuilder()
                            .setClientId(this.clientId)
                            .setTaskDefName(taskDefName)
                            .build()
                    );
                Thread.sleep(5 * 1000);
            } catch (Exception exn) {
                exn.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
        this.registryClient.onCompleted();
    }

    public void onCompleted() {
        System.out.println("Yikes, exiting");
    }

    public void onNext(RegisterTaskWorkerReplyPb next) {
        // Reconcile what's running
        for (HostInfoPb host : next.getYourEndpointsList()) {
            if (!isAlreadyRunning(host)) {
                System.out.println(
                    "Adding for host : " + host.getHost() + ":" + host.getPort()
                );
                runningWorkerThreads.add(
                    new SingleServerConnector(
                        func,
                        host.getHost(),
                        host.getPort(),
                        taskDefName,
                        clientId
                    )
                );
            }
        }

        for (int i = runningWorkerThreads.size() - 1; i >= 0; i--) {
            SingleServerConnector runningThread = runningWorkerThreads.get(i);
            if (!shouldBeRunning(runningThread, next.getYourEndpointsList())) {
                LHUtil.log("stopping thread");
                runningThread.stop();
                runningWorkerThreads.remove(i);
            }
        }
    }

    private boolean shouldBeRunning(
        SingleServerConnector ssc,
        List<HostInfoPb> hosts
    ) {
        for (HostInfoPb h : hosts) {
            if (ssc.matches(h.getHost(), h.getPort())) return true;
        }
        return false;
    }

    private boolean isAlreadyRunning(HostInfoPb host) {
        for (SingleServerConnector ssc : runningWorkerThreads) {
            if (ssc.matches(host.getHost(), host.getPort())) {
                return true;
            }
        }
        return false;
    }

    public void onError(Throwable t) {
        System.out.println("Yikes, got error");
        t.printStackTrace();
    }
}
