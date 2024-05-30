package io.littlehorse.common.model.corecommand.subcommand.internals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerMetadataModel;
import java.util.*;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
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
    void assignWithTwoHostsAndFiveWorkers() {
        HostModel hostA = new HostModel("a", 0);
        HostModel hostB = new HostModel("b", 0);
        Set<HostModel> hosts = new TreeSet<>(List.of(hostA, hostB));
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(5);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        Assertions.assertThat(taskWorkersMetadata.get(0).hosts).containsExactly(hostA);
        Assertions.assertThat(taskWorkersMetadata.get(1).hosts).containsExactly(hostB);
        Assertions.assertThat(taskWorkersMetadata.get(2).hosts).containsExactly(hostA);
        Assertions.assertThat(taskWorkersMetadata.get(3).hosts).containsExactly(hostB);
        Assertions.assertThat(taskWorkersMetadata.get(4).hosts).containsExactly(hostA);
    }

    @Test
    void assignWithFiveHostsAndTwoWorkers() {
        HostModel hostA = new HostModel("a", 0);
        HostModel hostB = new HostModel("b", 0);
        HostModel hostC = new HostModel("c", 0);
        HostModel hostD = new HostModel("d", 0);
        HostModel hostE = new HostModel("e", 0);
        Set<HostModel> hosts = new TreeSet<>(List.of(hostA, hostB, hostC, hostD, hostE));
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(2);

        robinAssignor.assign(hosts, taskWorkersMetadata);
        Assertions.assertThat(taskWorkersMetadata.get(0).hosts).containsExactly(hostA, hostC, hostE);
        Assertions.assertThat(taskWorkersMetadata.get(1).hosts).containsExactly(hostB, hostD);
    }

    @Test
    void assignWithFiveHostsAndFiveWorkers() {
        List<HostModel> hosts = generateHosts(5);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(5);
        robinAssignor.assign(hosts, taskWorkersMetadata);

        Assertions.assertThat(taskWorkersMetadata.get(0).hosts).containsExactly(hosts.get(0));
        Assertions.assertThat(taskWorkersMetadata.get(1).hosts).containsExactly(hosts.get(1));
        Assertions.assertThat(taskWorkersMetadata.get(2).hosts).containsExactly(hosts.get(2));
        Assertions.assertThat(taskWorkersMetadata.get(3).hosts).containsExactly(hosts.get(3));
        Assertions.assertThat(taskWorkersMetadata.get(4).hosts).containsExactly(hosts.get(4));
    }

    @Test
    void assignWithThreeHostsAndFourWorkers() {
        List<HostModel> hosts = generateHosts(3);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(4);

        robinAssignor.assign(hosts, taskWorkersMetadata);

        assertTrue(taskWorkersMetadata.get(0).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(0).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(1).hosts.contains(hosts.get(1))
                && taskWorkersMetadata.get(1).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(2).hosts.contains(hosts.get(2))
                && taskWorkersMetadata.get(1).hosts.size() == 1);
        assertTrue(taskWorkersMetadata.get(3).hosts.contains(hosts.get(0))
                && taskWorkersMetadata.get(1).hosts.size() == 1);
    }

    @Test
    void assignWithThreeHostsAndNineWorkers() {
        List<HostModel> hosts = generateHosts(3);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(9);

        // assign multiple times
        robinAssignor.assign(hosts, taskWorkersMetadata);
        robinAssignor.assign(hosts, taskWorkersMetadata);

        Assertions.assertThat(taskWorkersMetadata.get(0).hosts).containsExactly(hosts.get(0));
        Assertions.assertThat(taskWorkersMetadata.get(1).hosts).containsExactly(hosts.get(1));
        Assertions.assertThat(taskWorkersMetadata.get(2).hosts).containsExactly(hosts.get(2));
        Assertions.assertThat(taskWorkersMetadata.get(3).hosts).containsExactly(hosts.get(0));
        Assertions.assertThat(taskWorkersMetadata.get(4).hosts).containsExactly(hosts.get(1));
        Assertions.assertThat(taskWorkersMetadata.get(5).hosts).containsExactly(hosts.get(2));
        Assertions.assertThat(taskWorkersMetadata.get(6).hosts).containsExactly(hosts.get(0));
        Assertions.assertThat(taskWorkersMetadata.get(7).hosts).containsExactly(hosts.get(1));
        Assertions.assertThat(taskWorkersMetadata.get(8).hosts).containsExactly(hosts.get(2));
    }

    @Test
    void assignWithNineHostsAndSixWorkers() {
        List<HostModel> hosts = generateHosts(9);
        List<TaskWorkerMetadataModel> taskWorkersMetadata = generateTaskWorkersMetadata(6);

        // assign multiple times
        robinAssignor.assign(hosts, taskWorkersMetadata);
        robinAssignor.assign(hosts, taskWorkersMetadata);
        org.assertj.core.api.Assertions.assertThat(taskWorkersMetadata.get(0).hosts)
                .containsExactlyInAnyOrder((hosts.get(0)), (hosts.get(6)));
        org.assertj.core.api.Assertions.assertThat(taskWorkersMetadata.get(1).hosts)
                .containsExactlyInAnyOrder((hosts.get(1)), (hosts.get(7)));
        org.assertj.core.api.Assertions.assertThat(taskWorkersMetadata.get(2).hosts)
                .containsExactlyInAnyOrder((hosts.get(2)), (hosts.get(8)));
        org.assertj.core.api.Assertions.assertThat(taskWorkersMetadata.get(3).hosts)
                .containsExactlyInAnyOrder((hosts.get(3)));
        org.assertj.core.api.Assertions.assertThat(taskWorkersMetadata.get(4).hosts)
                .containsExactlyInAnyOrder((hosts.get(4)));
        org.assertj.core.api.Assertions.assertThat(taskWorkersMetadata.get(5).hosts)
                .containsExactlyInAnyOrder((hosts.get(5)));
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
