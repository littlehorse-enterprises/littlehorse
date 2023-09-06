package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.InProgressRestoration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;

@Data
public class ServerHealthState {

    private String host;
    private int port;
    private String instanceId;

    private List<ActiveTaskState> activeTasks;
    private List<StandbyTaskState> standbyTasks;

    private State coreState;
    private State timerState;

    public ServerHealthState() {
        this.activeTasks = new ArrayList<>();
        this.standbyTasks = new ArrayList<>();
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

        this.activeTasks.addAll(coreStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.activeTasks().stream())
                .map(coreTask -> new ActiveTaskState(coreTask, restorations, config))
                .toList());

        // this.activeTasks.addAll(timerStreams.metadataForLocalThreads().stream()
        //         .flatMap(thread -> thread.activeTasks().stream())
        //         .map(timerTask -> new ActiveTaskState(timerTask, restorations, config))
        //         .toList());

        this.standbyTasks.addAll(coreStreams.metadataForLocalThreads().stream()
                .flatMap(thread -> thread.standbyTasks().stream())
                .map(standbyTask -> new StandbyTaskState(standbyTask, restorations, config))
                .toList());

        // this.standbyTasks.addAll(timerStreams.metadataForLocalThreads().stream()
        //         .flatMap(thread -> thread.standbyTasks().stream())
        //         .map(timerTask -> new StandbyTaskState(timerTask, restorations, config))
        //         .toList());

        this.coreState = coreStreams.state();
        this.timerState = timerStreams.state();
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
