package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.InProgressRestoration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TaskMetadata;
import org.apache.kafka.streams.KafkaStreams.State;

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
            Map<TopicPartition, InProgressRestoration> restorations) {

        this();

        this.host = config.getInternalAdvertisedHost();
        this.port = config.getInternalAdvertisedPort();
        this.instanceId = config.getLHInstanceId();
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
                .filter(activeTask -> fromTask(activeTask, config) == LHProcessorType.CORE)
                .map(standbyTask -> new StandbyTaskState(standbyTask, restorations, config))
                .toList());

        this.repartitionStandbyTasks.addAll(coreStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.standbyTasks().stream())
                .filter(activeTask -> fromTask(activeTask, config) == LHProcessorType.REPARTITION)
                .map(standbyTask -> new StandbyTaskState(standbyTask, restorations, config))
                .toList());

        this.timerStandbyTasks.addAll(timerStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.standbyTasks().stream())
                .map(timerTask -> new StandbyTaskState(timerTask, restorations, config))
                .toList());

        this.coreState = coreStreams.state();
        this.timerState = timerStreams.state();
    }

    public static LHProcessorType fromTask(TaskMetadata task, LHServerConfig config) {
        Set<TopicPartition> topics = task.topicPartitions();
        if (topics.size() != 1) {
            throw new IllegalStateException("Impossible. All LH processors have only one input topic");
        }
        return fromTopic(topics.stream().findFirst().get().topic(), config);
    }

    public static LHProcessorType fromTopic(String topic, LHServerConfig config) {
        String truncated = topic.substring(config.getLHClusterId().length());

        if (truncated.contains("core-cmd")) {
            return LHProcessorType.CORE;
        }

        if (truncated.contains("repartition")) {
            return LHProcessorType.REPARTITION;
        }
        if (truncated.contains("changelog")) {
            return LHProcessorType.GLOBAL_METADATA;
        }
        if (truncated.contains("metadata")) {
            return LHProcessorType.METADATA;
        }
        if (truncated.contains("timer")) {
            return LHProcessorType.TIMER;
        }
        throw new IllegalArgumentException("Unrecognized topic " + topic);
    }
}
