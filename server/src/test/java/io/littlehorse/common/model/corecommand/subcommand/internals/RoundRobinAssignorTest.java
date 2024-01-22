package io.littlehorse.common.model.corecommand.subcommand.internals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerMetadataModel;
import java.util.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

public class RoundRobinAssignorTest {

    private final Faker faker = new Faker();
    private final RoundRobinAssignor robinAssignor = new RoundRobinAssignor();

    @Test
    void assignWithOneHostAndTwoWorkers() {
        List<HostModel> hosts = generateHosts(1);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(2);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)));
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)));
    }

    @Test
    void assignWithTwoHostsAndTwoWorkers() {
        List<HostModel> hosts = generateHosts(2);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(2);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        for (HostModel host : hosts) {
            assertTrue(taskWorkersMetadata.get(0).hosts.contains(host));
            assertTrue(taskWorkersMetadata.get(1).hosts.contains(host));
        }
    }

    @Test
    void assignWithTwoHostsAndFourWorkers() {
        List<HostModel> hosts = generateHosts(2);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(4);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0))
                && !taskWorkersMetadata.get(0).hosts.contains(hosts.get(1)));
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0))
                && !taskWorkersMetadata.get(1).hosts.contains(hosts.get(1)));
        assertTrue(taskWorkersMetadata.get(2).hosts.contains(hosts.get(1))
                && !taskWorkersMetadata.get(2).hosts.contains(hosts.get(0)));
        assertTrue(taskWorkersMetadata.get(3).hosts.contains(hosts.get(1))
                && !taskWorkersMetadata.get(3).hosts.contains(hosts.get(0)));
    }

    @Test
    void assignWithTwoHostsAndFiveWorkers() {
        List<HostModel> hosts = generateHosts(2);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(5);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)));
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)));
        assertTrue(taskWorkersMetadata.get(2).hosts.contains(hosts.get(1)));
        assertTrue(taskWorkersMetadata.get(3).hosts.contains(hosts.get(1)));
        assertTrue(taskWorkersMetadata.get(4).hosts.contains(hosts.get(0)));
    }

    @Test
    void assignWithFiveHostsAndTwoWorkers() {
        List<HostModel> hosts = generateHosts(5);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(2);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        for (HostModel host : hosts) {
            assertTrue(taskWorkersMetadata.get(0).hosts.contains(host));
            assertTrue(taskWorkersMetadata.get(1).hosts.contains(host));
        }
    }

    @Test
    void assignWithFiveHostsAndFiveWorkers() {
        List<HostModel> hosts = generateHosts(5);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(5);
        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(0).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(0).hosts.size() == 2);
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(1).hosts.contains(hosts.get(3))
                && taskWorkersMetadata.get(1).hosts.size() == 2);
        assertTrue(taskWorkersMetadata.get(2).hosts.contains(hosts.get(1))
                && taskWorkersMetadata.get(2).hosts.contains(hosts.get(3))
                && taskWorkersMetadata.get(2).hosts.size() == 2);
        assertTrue(taskWorkersMetadata.get(3).hosts.contains(hosts.get(1))
                && taskWorkersMetadata.get(3).hosts.contains(hosts.get(4))
                && taskWorkersMetadata.get(3).hosts.size() == 2);
        assertTrue(taskWorkersMetadata.get(4).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(4).hosts.contains(hosts.get(4))
                && taskWorkersMetadata.get(4).hosts.size() == 2);
    }

    @Test
    void assignWithThreeHostsAndFourWorkers() {
        List<HostModel> hosts = generateHosts(3);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(4);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(0).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(0).hosts.size() == 2);
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(1).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(1).hosts.size() == 2);
        assertTrue(taskWorkersMetadata.get(2).hosts.contains(hosts.get(1))
                && !taskWorkersMetadata.get(2).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(2).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(3).hosts.contains(hosts.get(1))
                && !taskWorkersMetadata.get(3).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(3).hosts.size() == 1);
    }

    @Test
    void assignWithThreeHostsAndNineWorkers() {
        List<HostModel> hosts = generateHosts(3);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(9);

        // assign multiple times
        robinAssignor.assign(hosts, taskWorkersMetadata);
        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(0).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(1).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(2).hosts.contains(hosts.get(1))
                && taskWorkersMetadata.get(2).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(3).hosts.contains(hosts.get(1))
                && taskWorkersMetadata.get(3).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(4).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(4).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(5).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(5).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(6).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(6).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(7).hosts.contains(hosts.get(1))
                && taskWorkersMetadata.get(7).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(8).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(8).hosts.size() == 1);
    }

    public List<HostModel> generateHosts(int q) {
        char[] domains = "abcdefghijklmnopqrstuvwxyz".substring(0, q).toCharArray();
        List<HostModel> hosts = new ArrayList<>();
        for (char domain : domains) {
            HostModel host = new HostModel(String.valueOf(domain), 2023);
            hosts.add(host);
        }
        return hosts;
    }

    public List<TaskWorkerMetadataModel> generateTaskWorkersMetadata(int q) {
        char[] clientIds = "abcdefghijklmnopqrstuvwxyz".substring(0, q).toCharArray();
        List<TaskWorkerMetadataModel> taskWorkersMetadata = new ArrayList<>();
        for (char clientId : clientIds) {
            TaskWorkerMetadataModel taskWorker = new TaskWorkerMetadataModel();
            taskWorker.taskWorkerId = String.valueOf(clientId);
            taskWorker.latestHeartbeat = new Date();
            taskWorkersMetadata.add(taskWorker);
        }
        return taskWorkersMetadata;
    }
}
