package io.littlehorse.common.model.command.subcommand.internals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.littlehorse.common.model.meta.HostModel;
import io.littlehorse.common.model.meta.TaskWorkerMetadataModel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

public class RoundRobinAssignorTest {

    private Faker faker = new Faker();
    private RoundRobinAssignor robinAssignor = new RoundRobinAssignor();

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

        robinAssignor.assign(hosts, taskWorkersMetadata);

        for (TaskWorkerMetadataModel var : taskWorkersMetadata) {
            System.out.println(var.hosts);
        }

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
        List<HostModel> hosts = new ArrayList<HostModel>();
        for (int i = 0; i < q; i++) {
            HostModel host =
                    new HostModel(faker.internet().domainName(), faker.number().numberBetween(5000, 5500));
            hosts.add(host);
        }
        return hosts;
    }

    public List<TaskWorkerMetadataModel> generateTaskWorkersMetadata(int q) {
        List<TaskWorkerMetadataModel> taskWorkersMetadata = new ArrayList<TaskWorkerMetadataModel>();
        for (int i = 0; i < q; i++) {
            TaskWorkerMetadataModel taskWorker = new TaskWorkerMetadataModel();
            taskWorker.clientId = UUID.randomUUID().toString();
            taskWorkersMetadata.add(taskWorker);
        }
        return taskWorkersMetadata;
    }
}
