package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.tests.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ABSimpleDeleteDeadTaskWorker extends Test {

    private String taskDefName;
    private Set<String> allHosts;
    private LHConfig config;

    public ABSimpleDeleteDeadTaskWorker(LHPublicApiBlockingStub client, LHConfig config) {
        super(client, config);
        this.config = config;
        taskDefName = "dead-workers-test-" + UUID.randomUUID().toString();
        allHosts = new HashSet<>();
    }

    public String getDescription() {
        return """
                Simple test to determine whether the delete dead task workers works properly.
                It creates a task group of five 'task workers' and ensures that they
                are properly deleted. This test is a no-op if it is run against
                a LittleHorse cluster with only one server.
                """;
    }

    public void test() throws InterruptedException {
        // Create taskdef
        client.putTaskDef(PutTaskDefRequest.newBuilder().setName(taskDefName).build());

        // Taskdef needs to propagate to all servers
        Thread.sleep(50);

        String client1 = "client-1";
        String client2 = "client-2";
        String client3 = "client-3";
        String client4 = "client-4";
        String client5 = "client-5";

        RegisterTaskWorkerResponse reply1 = client.registerTaskWorker(register(client1));
        for (LHHostInfo host : reply1.getYourHostsList()) {
            allHosts.add(hostToString(host));
        }

        // It distributes available worker (one server each worker)
        client.registerTaskWorker(register(client2));
        client.registerTaskWorker(register(client3));
        client.registerTaskWorker(register(client4));

        // Wait until all workers are dead
        Thread.sleep(15000);

        RegisterTaskWorkerResponse reply5 = client.registerTaskWorker(register(client5));
        int newCount = reply5.getYourHostsCount();

        // It should assign all the workers available for this only task worker
        if (newCount != allHosts.size()) {
            throw new RuntimeException("dead workers aren't being deleted!");
        }
    }

    private String hostToString(LHHostInfo host) {
        return host.getHost() + ":" + host.getPort();
    }

    private RegisterTaskWorkerRequest register(String clientId) {
        return RegisterTaskWorkerRequest.newBuilder()
                .setClientId(clientId)
                .setTaskDefId(LHLibUtil.taskDefId(taskDefName))
                .setListenerName(config.getConnectListener())
                .build();
    }

    public void cleanup() {
        client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                .setId(LHLibUtil.taskDefId(taskDefName))
                .build());
    }
}
