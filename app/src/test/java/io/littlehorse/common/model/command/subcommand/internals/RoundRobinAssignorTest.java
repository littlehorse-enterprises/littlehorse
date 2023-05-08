package io.littlehorse.common.model.command.subcommand.internals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.littlehorse.common.model.meta.Host;
import io.littlehorse.common.model.meta.TaskWorkerMetadata;
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
        List<Host> hosts = generateHosts(1);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(2);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)));
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)));
    }

    @Test
    void assignWithTwoHostsAndTwoWorkers() {
        List<Host> hosts = generateHosts(2);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(2);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        for (Host host : hosts) {
            assertTrue(taskWorkersMetadata.get(0).hosts.contains(host));
            assertTrue(taskWorkersMetadata.get(1).hosts.contains(host));
        }
    }

    @Test
    void assignWithTwoHostsAndFourWorkers() {
        List<Host> hosts = generateHosts(2);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(4);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(
            taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)) &&
            !taskWorkersMetadata.get(0).hosts.contains(hosts.get(1))
        );
        assertTrue(
            taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)) &&
            !taskWorkersMetadata.get(1).hosts.contains(hosts.get(1))
        );
        assertTrue(
            taskWorkersMetadata.get(2).hosts.contains(hosts.get(1)) &&
            !taskWorkersMetadata.get(2).hosts.contains(hosts.get(0))
        );
        assertTrue(
            taskWorkersMetadata.get(3).hosts.contains(hosts.get(1)) &&
            !taskWorkersMetadata.get(3).hosts.contains(hosts.get(0))
        );
    }

    @Test
    void assignWithTwoHostsAndFiveWorkers() {
        List<Host> hosts = generateHosts(2);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(5);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)));
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)));
        assertTrue(taskWorkersMetadata.get(2).hosts.contains(hosts.get(1)));
        assertTrue(taskWorkersMetadata.get(3).hosts.contains(hosts.get(1)));
        assertTrue(taskWorkersMetadata.get(4).hosts.contains(hosts.get(0)));
    }

    @Test
    void assignWithFiveHostsAndTwoWorkers() {
        List<Host> hosts = generateHosts(5);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(2);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        for (Host host : hosts) {
            assertTrue(taskWorkersMetadata.get(0).hosts.contains(host));
            assertTrue(taskWorkersMetadata.get(1).hosts.contains(host));
        }
    }

    @Test
    void assignWithFiveHostsAndFiveWorkers() {
        List<Host> hosts = generateHosts(5);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(5);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(
            taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)) &&
            taskWorkersMetadata.get(0).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(0).hosts.size() == 2
        );
        assertTrue(
            taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)) &&
            taskWorkersMetadata.get(1).hosts.contains(hosts.get(3)) &&
            taskWorkersMetadata.get(1).hosts.size() == 2
        );
        assertTrue(
            taskWorkersMetadata.get(2).hosts.contains(hosts.get(1)) &&
            taskWorkersMetadata.get(2).hosts.contains(hosts.get(3)) &&
            taskWorkersMetadata.get(2).hosts.size() == 2
        );
        assertTrue(
            taskWorkersMetadata.get(3).hosts.contains(hosts.get(1)) &&
            taskWorkersMetadata.get(3).hosts.contains(hosts.get(4)) &&
            taskWorkersMetadata.get(3).hosts.size() == 2
        );
        assertTrue(
            taskWorkersMetadata.get(4).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(4).hosts.contains(hosts.get(4)) &&
            taskWorkersMetadata.get(4).hosts.size() == 2
        );
    }

    @Test
    void assignWithThreeHostsAndFourWorkers() {
        List<Host> hosts = generateHosts(3);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(4);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(
            taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)) &&
            taskWorkersMetadata.get(0).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(0).hosts.size() == 2
        );
        assertTrue(
            taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)) &&
            taskWorkersMetadata.get(1).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(1).hosts.size() == 2
        );
        assertTrue(
            taskWorkersMetadata.get(2).hosts.contains(hosts.get(1)) &&
            !taskWorkersMetadata.get(2).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(2).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(3).hosts.contains(hosts.get(1)) &&
            !taskWorkersMetadata.get(3).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(3).hosts.size() == 1
        );
    }

    @Test
    void assignWithThreeHostsAndNineWorkers() {
        List<Host> hosts = generateHosts(3);
        List<TaskWorkerMetadata> taskWorkersMetadata = generateTaskWorkersMetadata(9);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        for (TaskWorkerMetadata var : taskWorkersMetadata) {
            System.out.println(var.hosts);
        }

        assertTrue(
            taskWorkersMetadata.get(0).hosts.contains(hosts.get(0)) &&
            taskWorkersMetadata.get(0).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(1).hosts.contains(hosts.get(0)) &&
            taskWorkersMetadata.get(1).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(2).hosts.contains(hosts.get(1)) &&
            taskWorkersMetadata.get(2).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(3).hosts.contains(hosts.get(1)) &&
            taskWorkersMetadata.get(3).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(4).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(4).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(5).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(5).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(6).hosts.contains(hosts.get(0)) &&
            taskWorkersMetadata.get(6).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(7).hosts.contains(hosts.get(1)) &&
            taskWorkersMetadata.get(7).hosts.size() == 1
        );
        assertTrue(
            taskWorkersMetadata.get(8).hosts.contains(hosts.get(2)) &&
            taskWorkersMetadata.get(8).hosts.size() == 1
        );
    }

    public List<Host> generateHosts(int q) {
        List<Host> hosts = new ArrayList<Host>();
        for (int i = 0; i < q; i++) {
            Host host = new Host(
                faker.internet().domainName(),
                faker.number().numberBetween(5000, 5500)
            );
            hosts.add(host);
        }
        return hosts;
    }

    public List<TaskWorkerMetadata> generateTaskWorkersMetadata(int q) {
        List<TaskWorkerMetadata> taskWorkersMetadata = new ArrayList<TaskWorkerMetadata>();
        for (int i = 0; i < q; i++) {
            TaskWorkerMetadata taskWorker = new TaskWorkerMetadata();
            taskWorker.clientId = UUID.randomUUID().toString();
            taskWorkersMetadata.add(taskWorker);
        }
        return taskWorkersMetadata;
    }
}
