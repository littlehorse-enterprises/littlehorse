package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.HostInfo;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.tests.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ACSimpleTaskRebalancing extends Test {

    private String taskDefName;
    private Set<String> allHosts;
    private LHWorkerConfig config;

    public ACSimpleTaskRebalancing(LHClient client, LHWorkerConfig config) {
        super(client, config);
        this.config = config;
        taskDefName = "rebalancing-test-" + UUID.randomUUID().toString();
        allHosts = new HashSet<>();
    }

    public String getDescription() {
        return """
                Simple test to determine whether the task rebalancing works properly.
                It creates a task group of four 'task workers' and ensures that they
                are properly balanced. This test is a no-op if it is run against
                a LittleHorse cluster with only one server.
                """;
    }

    public void test() throws LHApiError, InterruptedException {
        // Create taskdef
        client.putTaskDef(PutTaskDefRequest.newBuilder().setName(taskDefName).build(), false);

        // Taskdef needs to propagate to all servers
        Thread.sleep(50);

        LHPublicApiBlockingStub stub = client.getGrpcClient();

        String client1 = "client-1";
        String client2 = "client-2";
        String client3 = "client-3";
        String client4 = "client-4";

        // This is the first worker to connect, so it should get ALL of the hosts
        RegisterTaskWorkerResponse reply1 = stub.registerTaskWorker(register(client1));
        for (HostInfo host : reply1.getYourHostsList()) {
            allHosts.add(hostToString(host));
        }

        // Since we require that each server has at least two connections on it,
        // we should check that when we add the worker #2, then it still gets all
        // the hosts.
        RegisterTaskWorkerResponse reply2 = stub.registerTaskWorker(register(client2));
        if (reply2.getYourHostsCount() != allHosts.size()) {
            throw new RuntimeException("Second worker should still get all hosts!");
        }

        reply1 = stub.registerTaskWorker(register(client1));
        if (reply1.getYourHostsCount() != allHosts.size()) {
            throw new RuntimeException("First worker should still get all hosts when only one other!");
        }

        // When we add a third and fourth worker, if there are more than one server,
        // then they shouldn't all get all the hosts
        stub.registerTaskWorker(register(client3));
        stub.registerTaskWorker(register(client4));

        reply1 = stub.registerTaskWorker(register(client1));
        int newCount = reply1.getYourHostsCount();

        if (newCount > 1 && newCount == allHosts.size()) {
            throw new RuntimeException("work isn't being balanced!");
        }
    }

    private String hostToString(HostInfo host) {
        return host.getHost() + ":" + host.getPort();
    }

    private RegisterTaskWorkerRequest register(String clientId) {
        return RegisterTaskWorkerRequest.newBuilder()
                .setClientId(clientId)
                .setTaskDefName(taskDefName)
                .setListenerName(config.getConnectListener())
                .build();
    }

    public void cleanup() throws LHApiError {
        client.deleteTaskDef(taskDefName);
    }
}
