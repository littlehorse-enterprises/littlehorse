package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.HostInfoPb;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.PutTaskDefPb;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb;
import io.littlehorse.tests.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ABSimpleDeleteDeadTaskWorker extends Test {

    private String taskDefName;
    private Set<String> allHosts;
    private LHWorkerConfig config;

    public ABSimpleDeleteDeadTaskWorker(LHClient client, LHWorkerConfig config) {
        super(client, config);
        this.config = config;
        taskDefName = "dead-workers-test-" + UUID.randomUUID().toString();
        allHosts = new HashSet<>();
    }

    public String getDescription() {
        return """
                Simple test to determine whether the delete dead task workers works properly.
                It creates a task userGroup of five 'task workers' and ensures that they
                are properly deleted. This test is a no-op if it is run against
                a LittleHorse cluster with only one server.
                """;
    }

    public void test() throws LHApiError, InterruptedException {
        // Create taskdef
        client.putTaskDef(
            PutTaskDefPb.newBuilder().setName(taskDefName).build(),
            false
        );

        // Taskdef needs to propagate to all servers
        Thread.sleep(50);

        LHPublicApiBlockingStub stub = client.getGrpcClient();

        String client1 = "client-1";
        String client2 = "client-2";
        String client3 = "client-3";
        String client4 = "client-4";
        String client5 = "client-5";

        RegisterTaskWorkerReplyPb reply1 = stub.registerTaskWorker(register(client1));
        for (HostInfoPb host : reply1.getYourHostsList()) {
            allHosts.add(hostToString(host));
        }

        // It distributes available worker (one server each worker)
        stub.registerTaskWorker(register(client2));
        stub.registerTaskWorker(register(client3));
        stub.registerTaskWorker(register(client4));

        // Wait until all workers are dead
        Thread.sleep(15000);

        RegisterTaskWorkerReplyPb reply5 = stub.registerTaskWorker(register(client5));
        int newCount = reply5.getYourHostsCount();

        // It should assign all the workers available for this only task worker
        if (newCount != allHosts.size()) {
            throw new RuntimeException("dead workers aren't being deleted!");
        }
    }

    private String hostToString(HostInfoPb host) {
        return host.getHost() + ":" + host.getPort();
    }

    private RegisterTaskWorkerPb register(String clientId) {
        return RegisterTaskWorkerPb
            .newBuilder()
            .setClientId(clientId)
            .setTaskDefName(taskDefName)
            .setListenerName(config.getConnectListener())
            .build();
    }

    public void cleanup() throws LHApiError {
        client.deleteTaskDef(taskDefName);
    }
}
