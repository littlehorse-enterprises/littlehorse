package io.littlehorse.common.model.command.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.subcommand.TaskWorkerHeartBeatRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.internals.RoundRobinAssignor;
import io.littlehorse.common.model.corecommand.subcommand.internals.TaskWorkerAssignor;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerMetadataModel;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.server.streams.util.InternalHosts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

public class TaskWorkerHeartBeatTest {

    private Faker faker = new Faker();

    private final ProcessorExecutionContext executionContext = mock(Answers.RETURNS_DEEP_STUBS);
    private final TaskWorkerAssignor assignor = spy(new RoundRobinAssignor());
    private final LHServerConfig lhConfig = mock(LHServerConfig.class);
    private final TaskWorkerHeartBeatRequestModel taskWorkerHeartBeat = new TaskWorkerHeartBeatRequestModel(assignor);
    private final ArgumentCaptor<TaskWorkerGroupModel> taskWorkerCaptor =
            ArgumentCaptor.forClass(TaskWorkerGroupModel.class);

    @Test
    void doNotRemoveTaskWorkerIfItJustSentAHeartbeat() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas =
                taskWorkerGroup.taskWorkers.values().stream().toList();

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.setClientId(taskWorkerToKeep.clientId);

        taskWorkerToKeep.latestHeartbeat = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        Set<HostModel> generateHosts = generateHosts(2);

        when(executionContext.getableManager().get(any())).thenReturn(taskWorkerGroup);
        when(executionContext.getInternalHosts()).thenReturn(new InternalHosts(null, generateHosts));
        when(executionContext.getAdvertisedHost(any(), any())).thenReturn(mock());

        taskWorkerHeartBeat.process(executionContext, lhConfig);

        verify(executionContext.getableManager()).put(taskWorkerCaptor.capture());

        assertThat(taskWorkerGroup.taskWorkers).contains(Map.entry(taskWorkerToKeep.clientId, taskWorkerToKeep));
        assertThat(taskWorkerGroup.taskWorkers).hasSize(2);
    }

    @Test
    void triggerRebalanceIfThereIsNewHosts() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas =
                taskWorkerGroup.taskWorkers.values().stream().toList();
        Set<HostModel> generateHosts = generateHosts(2);

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.setClientId(taskWorkerToKeep.clientId);

        taskWorkerToKeep.latestHeartbeat = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        when(executionContext.getableManager().get(any())).thenReturn(taskWorkerGroup);
        when(executionContext.getInternalHosts()).thenReturn(new InternalHosts(null, generateHosts));
        when(executionContext.getAdvertisedHost(any(), any())).thenReturn(mock());

        taskWorkerHeartBeat.process(executionContext, lhConfig);

        verify(executionContext.getableManager()).put(taskWorkerCaptor.capture());
        verify(assignor).assign(anyCollection(), anyCollection());
    }

    @Test
    void triggerRebalanceOnly0neTimeIfTheHostsAreTheSame() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas =
                taskWorkerGroup.taskWorkers.values().stream().collect(Collectors.toList());
        Set<HostModel> generateHosts = generateHosts(2);

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.setClientId(taskWorkerToKeep.clientId);

        taskWorkerToKeep.latestHeartbeat = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        when(executionContext.getableManager().get(any())).thenReturn(taskWorkerGroup);
        when(executionContext.getInternalHosts())
                .thenReturn(new InternalHosts(null, generateHosts))
                .thenReturn(new InternalHosts(generateHosts, generateHosts));
        when(executionContext.getAdvertisedHost(any(), any())).thenReturn(mock());

        taskWorkerHeartBeat.process(executionContext, lhConfig);
        taskWorkerHeartBeat.process(executionContext, lhConfig);

        verify(assignor).assign(anyCollection(), anyCollection());
    }

    @Test
    void triggerRebalanceTwiceIfFirstTheHostsAreTheSameButThenTheAreNewHosts() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas =
                taskWorkerGroup.taskWorkers.values().stream().collect(Collectors.toList());
        Set<HostModel> generateHosts = generateHosts(2);

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.setClientId(taskWorkerToKeep.clientId);

        taskWorkerToKeep.latestHeartbeat = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        when(executionContext.getableManager().get(any())).thenReturn(taskWorkerGroup);
        when(executionContext.getInternalHosts())
                .thenReturn(new InternalHosts(null, generateHosts))
                .thenReturn(new InternalHosts(generateHosts, generateHosts));
        when(executionContext.getAdvertisedHost(any(), any())).thenReturn(mock());

        taskWorkerHeartBeat.process(executionContext, lhConfig);
        taskWorkerHeartBeat.process(executionContext, lhConfig);
        verify(assignor).assign(anyCollection(), anyCollection());

        Set<HostModel> newHost = new HashSet<>(generateHosts);
        newHost.addAll(generateHosts(1));

        when(executionContext.getInternalHosts()).thenReturn(new InternalHosts(generateHosts, newHost));
        taskWorkerHeartBeat.process(executionContext, lhConfig);

        verify(assignor, times(2)).assign(anyCollection(), anyCollection());
    }

    @Test
    void removeTaskWorkerIfItIsConsideredDead() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2, 2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas =
                taskWorkerGroup.taskWorkers.values().stream().collect(Collectors.toList());

        TaskWorkerMetadataModel taskWorkerToBeRemoved = taskWorkerMetadatas.get(0);
        TaskWorkerMetadataModel taskWorkerToBeKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.setClientId(taskWorkerToBeKeep.clientId);

        taskWorkerToBeRemoved.latestHeartbeat = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        Set<HostModel> generateHosts = generateHosts(2);

        when(executionContext.getableManager().get(any())).thenReturn(taskWorkerGroup);
        when(executionContext.getInternalHosts()).thenReturn(new InternalHosts(null, generateHosts));
        when(executionContext.getAdvertisedHost(any(), any())).thenReturn(mock());

        taskWorkerHeartBeat.process(executionContext, lhConfig);

        verify(executionContext.getableManager()).put(taskWorkerCaptor.capture());
        verify(assignor).assign(anyCollection(), anyCollection());
        assertThat(taskWorkerGroup.taskWorkers)
                .doesNotContain(Map.entry(taskWorkerToBeRemoved.clientId, taskWorkerToBeRemoved));
        assertThat(taskWorkerGroup.taskWorkers).hasSize(1);
    }

    public Map<String, TaskWorkerMetadataModel> generateTaskWorkersMetadata(int numberOfTaskWorkers) {
        return generateTaskWorkersMetadata(numberOfTaskWorkers, 0);
    }

    public Map<String, TaskWorkerMetadataModel> generateTaskWorkersMetadata(
            int numberOfTaskWorkers, int numberOfHosts) {
        Map<String, TaskWorkerMetadataModel> taskWorkersMetadata = new HashMap<>();
        for (int i = 0; i < numberOfTaskWorkers; i++) {
            TaskWorkerMetadataModel taskWorker = new TaskWorkerMetadataModel();
            taskWorker.clientId = UUID.randomUUID().toString();
            taskWorker.latestHeartbeat = new Date();
            taskWorkersMetadata.put(taskWorker.clientId, taskWorker);
            taskWorker.hosts = generateHosts(numberOfHosts);
        }
        return taskWorkersMetadata;
    }

    public Set<HostModel> generateHosts(int numberOfHosts) {
        Set<HostModel> hosts = new TreeSet<>();
        for (int i = 0; i < numberOfHosts; i++) {
            HostModel host =
                    new HostModel(faker.internet().domainName(), faker.number().numberBetween(5000, 5500));
            hosts.add(host);
        }
        return hosts;
    }
}
