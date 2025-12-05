package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.StandbyStoresOnInstance;
import io.littlehorse.server.monitoring.StandbyTopicPartitionMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.TaskMetadata;

@Data
public class ServerHealthState {

    private String host;
    private int port;
    private String instanceId;

    private List<ActiveTaskState> coreActiveTasks;
    private List<ActiveTaskState> repartitionActiveTasks;
    private List<ActiveTaskState> timerActiveTasks;
    private List<StandbyTaskState> coreStandbyTasks;
    private List<StandbyTaskState> repartitionStandbyTasks;
    private List<StandbyTaskState> timerStandbyTasks;

    private State coreState;
    private State timerState;
    private List<InProgressRestoration> restorations;

    public ServerHealthState() {
        coreActiveTasks = new ArrayList<>();
        repartitionActiveTasks = new ArrayList<>();
        timerActiveTasks = new ArrayList<>();
        coreStandbyTasks = new ArrayList<>();
        repartitionStandbyTasks = new ArrayList<>();
        timerStandbyTasks = new ArrayList<>();
    }

    public ServerHealthState(
            LHServerConfig config,
            KafkaStreams coreStreams,
            KafkaStreams timerStreams,
            Map<TopicPartition, InProgressRestoration> restorations,
            Map<String, StandbyStoresOnInstance> standbyTasks) {

        this();

        this.host = config.getInternalAdvertisedHost();
        this.port = config.getInternalAdvertisedPort();
        this.instanceId = config.getLHInstanceName();
        this.restorations = restorations.values().stream().toList();

        this.coreActiveTasks.addAll(coreStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.activeTasks().stream())
                .filter(activeTask -> fromTask(activeTask, config) == LHProcessorType.CORE)
                .map(coreTask -> new ActiveTaskState(coreTask, restorations, config))
                .toList());

        this.repartitionActiveTasks.addAll(coreStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.activeTasks().stream())
                .filter(activeTask -> fromTask(activeTask, config) == LHProcessorType.REPARTITION)
                .map(coreTask -> new ActiveTaskState(coreTask, restorations, config))
                .toList());

        this.timerActiveTasks.addAll(timerStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.activeTasks().stream())
                .map(timerTask -> new ActiveTaskState(timerTask, restorations, config))
                .toList());

        this.coreStandbyTasks.addAll(coreStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.standbyTasks().stream())
                .filter(standbyTask -> fromTask(standbyTask, config) == LHProcessorType.CORE)
                .filter(standbyTask -> !standbyTask.topicPartitions().isEmpty())
                .map(standbyTask -> createStandbyState(LHProcessorType.CORE.getStoreName(), standbyTasks, standbyTask))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList());

        this.repartitionStandbyTasks.addAll(coreStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.standbyTasks().stream())
                .filter(standbyTask -> fromTask(standbyTask, config) == LHProcessorType.REPARTITION)
                .filter(standbyTask -> !standbyTask.topicPartitions().isEmpty())
                .map(standbyTask ->
                        createStandbyState(LHProcessorType.REPARTITION.getStoreName(), standbyTasks, standbyTask))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList());

        this.timerStandbyTasks.addAll(timerStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.standbyTasks().stream())
                .filter(timerTask -> !timerTask.topicPartitions().isEmpty())
                .map(timerTask -> createStandbyState(LHProcessorType.TIMER.getStoreName(), standbyTasks, timerTask))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList());

        this.coreState = coreStreams.state();
        this.timerState = timerStreams.state();
    }

    public static LHProcessorType fromTask(TaskMetadata task, LHServerConfig config) {
        Set<TopicPartition> topics = task.topicPartitions();
        return topics.stream()
                .map(TopicPartition::topic)
                .map(t -> fromTopic(t, config))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow();
    }

    private Optional<StandbyTaskState> createStandbyState(
            String storeName, Map<String, StandbyStoresOnInstance> standbyTasks, TaskMetadata metadata) {
        StandbyStoresOnInstance registeredStandbyTasks = standbyTasks.get(storeName);
        StandbyTopicPartitionMetrics lagInfo = registeredStandbyTasks
                .lagInfoForPartition(
                        metadata.topicPartitions().stream().findFirst().get().partition())
                .orElse(null);
        if (lagInfo != null) {
            return Optional.of(new StandbyTaskState(metadata, lagInfo));
        } else {
            return Optional.empty();
        }
    }

    public static LHProcessorType fromTopic(String topic, LHServerConfig config) {
        String truncated = topic.substring(config.getLHClusterId().length());

        if (truncated.contains("core-store") || truncated.contains("core-cmd")) {
            return LHProcessorType.CORE;
        }

        if (truncated.contains("repartition-store") || truncated.contains("repartition-cmd")) {
            return LHProcessorType.REPARTITION;
        }
        if (truncated.contains("metadata")) {
            return LHProcessorType.METADATA;
        }
        if (truncated.contains("timer")) {
            return LHProcessorType.TIMER;
        }
        return null;
    }
}
