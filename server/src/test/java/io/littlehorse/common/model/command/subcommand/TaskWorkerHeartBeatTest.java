package io.littlehorse.common.model.command.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.subcommand.internals.TaskWorkerAssignor;
import io.littlehorse.common.model.meta.HostModel;
import io.littlehorse.common.model.meta.TaskWorkerGroupModel;
import io.littlehorse.common.model.meta.TaskWorkerMetadataModel;
import io.littlehorse.server.streamsimpl.util.InternalHosts;
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
import org.mockito.ArgumentCaptor;

public class TaskWorkerHeartBeatTest {

    private Faker faker = new Faker();

    private LHDAO lhdao = mock(LHDAO.class);
    private TaskWorkerAssignor assignor = mock(TaskWorkerAssignor.class);
    private LHConfig lhConfig = mock(LHConfig.class);
    private TaskWorkerHeartBeat taskWorkerHeartBeat = new TaskWorkerHeartBeat(
        assignor
    );
    private ArgumentCaptor<TaskWorkerGroupModel> taskWorkerCaptor = ArgumentCaptor.forClass(
        TaskWorkerGroupModel.class
    );

    @Test
    void doNotRemoveTaskWorkerIfItJustSentAHeartbeat() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas = taskWorkerGroup.taskWorkers
            .values()
            .stream()
            .collect(Collectors.toList());

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.clientId = taskWorkerToKeep.clientId;

        taskWorkerToKeep.latestHeartbeat =
            Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        Set<HostModel> generateHosts = generateHosts(2);

        when(lhdao.getTaskWorkerGroup(any())).thenReturn(taskWorkerGroup);
        when(lhdao.getInternalHosts())
            .thenReturn(new InternalHosts(null, generateHosts));

        taskWorkerHeartBeat.process(lhdao, lhConfig);

        verify(lhdao).putTaskWorkerGroup(taskWorkerCaptor.capture());

        assertThat(taskWorkerGroup.taskWorkers)
            .contains(Map.entry(taskWorkerToKeep.clientId, taskWorkerToKeep));
        assertThat(taskWorkerGroup.taskWorkers).hasSize(2);
    }

    @Test
    void triggerRebalanceIfThereIsNewHosts() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas = taskWorkerGroup.taskWorkers
            .values()
            .stream()
            .collect(Collectors.toList());
        Set<HostModel> generateHosts = generateHosts(2);

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.clientId = taskWorkerToKeep.clientId;

        taskWorkerToKeep.latestHeartbeat =
            Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        when(lhdao.getTaskWorkerGroup(any())).thenReturn(taskWorkerGroup);
        when(lhdao.getInternalHosts())
            .thenReturn(new InternalHosts(null, generateHosts));

        taskWorkerHeartBeat.process(lhdao, lhConfig);

        verify(lhdao).putTaskWorkerGroup(taskWorkerCaptor.capture());
        verify(assignor).assign(anyCollection(), anyCollection());
    }

    @Test
    void triggerRebalanceOnly0neTimeIfTheHostsAreTheSame() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas = taskWorkerGroup.taskWorkers
            .values()
            .stream()
            .collect(Collectors.toList());
        Set<HostModel> generateHosts = generateHosts(2);

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.clientId = taskWorkerToKeep.clientId;

        taskWorkerToKeep.latestHeartbeat =
            Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        when(lhdao.getTaskWorkerGroup(any())).thenReturn(taskWorkerGroup);
        when(lhdao.getInternalHosts())
            .thenReturn(new InternalHosts(null, generateHosts))
            .thenReturn(new InternalHosts(generateHosts, generateHosts));

        taskWorkerHeartBeat.process(lhdao, lhConfig);
        taskWorkerHeartBeat.process(lhdao, lhConfig);

        verify(assignor).assign(anyCollection(), anyCollection());
    }

    @Test
    void triggerRebalanceTwiceIfFirstTheHostsAreTheSameButThenTheAreNewHosts() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas = taskWorkerGroup.taskWorkers
            .values()
            .stream()
            .collect(Collectors.toList());
        Set<HostModel> generateHosts = generateHosts(2);

        TaskWorkerMetadataModel taskWorkerToKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.clientId = taskWorkerToKeep.clientId;

        taskWorkerToKeep.latestHeartbeat =
            Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        when(lhdao.getTaskWorkerGroup(any())).thenReturn(taskWorkerGroup);
        when(lhdao.getInternalHosts())
            .thenReturn(new InternalHosts(null, generateHosts))
            .thenReturn(new InternalHosts(generateHosts, generateHosts));

        taskWorkerHeartBeat.process(lhdao, lhConfig);
        taskWorkerHeartBeat.process(lhdao, lhConfig);
        verify(assignor).assign(anyCollection(), anyCollection());

        Set<HostModel> newHost = new HashSet<>(generateHosts);
        newHost.addAll(generateHosts(1));

        when(lhdao.getInternalHosts())
            .thenReturn(new InternalHosts(generateHosts, newHost));
        taskWorkerHeartBeat.process(lhdao, lhConfig);

        verify(assignor, times(2)).assign(anyCollection(), anyCollection());
    }

    @Test
    void removeTaskWorkerIfItIsConsideredDead() {
        TaskWorkerGroupModel taskWorkerGroup = new TaskWorkerGroupModel();
        taskWorkerGroup.taskWorkers = generateTaskWorkersMetadata(2);

        List<TaskWorkerMetadataModel> taskWorkerMetadatas = taskWorkerGroup.taskWorkers
            .values()
            .stream()
            .collect(Collectors.toList());

        TaskWorkerMetadataModel taskWorkerToBeRemoved = taskWorkerMetadatas.get(0);
        TaskWorkerMetadataModel taskWorkerToBeKeep = taskWorkerMetadatas.get(1);
        taskWorkerHeartBeat.clientId = taskWorkerToBeKeep.clientId;

        taskWorkerToBeRemoved.latestHeartbeat =
            Date.from(Instant.now().minus(1, ChronoUnit.DAYS));

        Set<HostModel> generateHosts = generateHosts(2);

        when(lhdao.getTaskWorkerGroup(any())).thenReturn(taskWorkerGroup);
        when(lhdao.getInternalHosts())
            .thenReturn(new InternalHosts(null, generateHosts));

        taskWorkerHeartBeat.process(lhdao, lhConfig);

        verify(lhdao).putTaskWorkerGroup(taskWorkerCaptor.capture());
        verify(assignor).assign(anyCollection(), anyCollection());
        assertThat(taskWorkerGroup.taskWorkers)
            .doesNotContain(
                Map.entry(taskWorkerToBeRemoved.clientId, taskWorkerToBeRemoved)
            );
        assertThat(taskWorkerGroup.taskWorkers).hasSize(1);
    }

    public Map<String, TaskWorkerMetadataModel> generateTaskWorkersMetadata(int q) {
        Map<String, TaskWorkerMetadataModel> taskWorkersMetadata = new HashMap<>();
        for (int i = 0; i < q; i++) {
            TaskWorkerMetadataModel taskWorker = new TaskWorkerMetadataModel();
            taskWorker.clientId = UUID.randomUUID().toString();
            taskWorker.latestHeartbeat = new Date();
            taskWorkersMetadata.put(taskWorker.clientId, taskWorker);
        }
        return taskWorkersMetadata;
    }

    public Set<HostModel> generateHosts(int q) {
        Set<HostModel> hosts = new TreeSet<HostModel>();
        for (int i = 0; i < q; i++) {
            HostModel host = new HostModel(
                faker.internet().domainName(),
                faker.number().numberBetween(5000, 5500)
            );
            hosts.add(host);
        }
        return hosts;
    }
}
