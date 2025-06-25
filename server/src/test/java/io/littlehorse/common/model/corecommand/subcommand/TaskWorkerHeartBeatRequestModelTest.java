package io.littlehorse.common.model.corecommand.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.subcommand.internals.RoundRobinAssignor;
import io.littlehorse.common.model.corecommand.subcommand.internals.TaskWorkerAssignor;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerMetadataModel;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

public class TaskWorkerHeartBeatRequestModelTest {

    private final CoreProcessorContext executionContext = mock(Answers.RETURNS_DEEP_STUBS);
    private final TaskWorkerAssignor assignor = spy(new RoundRobinAssignor());
    private final LHServerConfig lhConfig = mock(LHServerConfig.class);
    private final TaskWorkerHeartBeatRequestModel taskWorkerHeartBeat = new TaskWorkerHeartBeatRequestModel(assignor);
    private final ArgumentCaptor<TaskWorkerGroupModel> taskWorkerGroupCaptor =
            ArgumentCaptor.forClass(TaskWorkerGroupModel.class);

    @Test
    void removeTaskWorkerIfItIsConsideredDead() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2, 2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas =
                taskWorkerGroup.taskWorkers.values().stream().toList();

        TaskWorkerMetadataModel taskWorkerToBeRemoved = taskWorkerMetadatas.get(0);
        TaskWorkerMetadataModel taskWorkerToBeKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.setClientId(taskWorkerToBeKeep.taskWorkerId);

        taskWorkerToBeRemoved.latestHeartbeat = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        Set<HostModel> generateHosts = generateHosts(2);

        when(executionContext.getableManager().get(any())).thenReturn(taskWorkerGroup);
        when(executionContext.getInternalHosts()).thenReturn(generateHosts);

        taskWorkerHeartBeat.process(executionContext, lhConfig);

        verify(assignor).assign(generateHosts, taskWorkerGroup.taskWorkers.values());
        assertThat(taskWorkerGroup.taskWorkers)
                .doesNotContain(Map.entry(taskWorkerToBeRemoved.taskWorkerId, taskWorkerToBeRemoved));
        assertThat(taskWorkerGroup.taskWorkers).hasSize(1);
    }

    @Test
    void assignNewHosts() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(10, 1);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas =
                taskWorkerGroup.taskWorkers.values().stream().toList();

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.setClientId(taskWorkerToKeep.taskWorkerId);

        when(executionContext.getableManager().get(any())).thenReturn(taskWorkerGroup);
        when(executionContext.getInternalHosts()).thenReturn(generateHosts(10));

        taskWorkerHeartBeat.process(executionContext, lhConfig);

        assertThat(taskWorkerMetadatas.get(0).hosts).hasSize(1);
        assertThat(taskWorkerMetadatas.get(1).hosts).hasSize(1);
    }

    @Test
    void assignServers() {
        Set<HostModel> generateHosts = generateHosts(2);
        when(executionContext.getableManager().get(any())).thenReturn(null);
        when(executionContext.getInternalHosts()).thenReturn(generateHosts);

        taskWorkerHeartBeat.process(executionContext, lhConfig);

        verify(executionContext.getableManager()).put(taskWorkerGroupCaptor.capture());

        assertThat(taskWorkerGroupCaptor.getValue().taskWorkers).hasSize(1);
    }

    public Map<String, TaskWorkerMetadataModel> generateTaskWorkersMetadata(int numberOfTaskWorkers) {
        return generateTaskWorkersMetadata(numberOfTaskWorkers, 0);
    }

    public Map<String, TaskWorkerMetadataModel> generateTaskWorkersMetadata(
            int numberOfTaskWorkers, int numberOfHosts) {
        char[] clientIds =
                "abcdefghijklmnopqrstuvwxyz".substring(0, numberOfTaskWorkers).toCharArray();
        Map<String, TaskWorkerMetadataModel> taskWorkersMetadata = new HashMap<>();
        for (char clientId : clientIds) {
            TaskWorkerMetadataModel taskWorker = new TaskWorkerMetadataModel();
            taskWorker.taskWorkerId = String.valueOf(clientId);
            taskWorker.latestHeartbeat = new Date();
            taskWorkersMetadata.put(taskWorker.taskWorkerId, taskWorker);
            taskWorker.hosts = generateHosts(numberOfHosts);
        }
        return taskWorkersMetadata;
    }

    public Set<HostModel> generateHosts(int numberOfHosts) {
        Set<HostModel> hosts = new TreeSet<>();
        char[] domains =
                "abcdefghijklmnopqrstuvwxyz".substring(0, numberOfHosts).toCharArray();
        for (char domain : domains) {
            HostModel host = new HostModel(String.valueOf(domain), 2023);
            hosts.add(host);
        }
        return hosts;
    }
}
