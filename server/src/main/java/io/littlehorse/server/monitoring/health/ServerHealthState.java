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
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.TaskMetadata;

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

        if (timerStreams != null) {
            this.timerStandbyTasks.addAll(timerStreams.metadataForLocalThreads().stream()
                    .flatMap(thread -> thread.standbyTasks().stream())
                    .filter(timerTask -> !timerTask.topicPartitions().isEmpty())
                    .map(timerTask -> createStandbyState(LHProcessorType.TIMER.getStoreName(), standbyTasks, timerTask))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList());
            this.timerActiveTasks.addAll(timerStreams.metadataForLocalThreads().stream()
                    .flatMap(thread -> thread.activeTasks().stream())
                    .map(timerTask -> new ActiveTaskState(timerTask, restorations, config))
                    .toList());
            this.timerState = timerStreams.state();
        }

        this.coreState = coreStreams.state();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerHealthState that = (ServerHealthState) o;
        return port == that.port
                && Objects.equals(host, that.host)
                && Objects.equals(instanceId, that.instanceId)
                && Objects.equals(coreActiveTasks, that.coreActiveTasks)
                && Objects.equals(repartitionActiveTasks, that.repartitionActiveTasks)
                && Objects.equals(timerActiveTasks, that.timerActiveTasks)
                && Objects.equals(coreStandbyTasks, that.coreStandbyTasks)
                && Objects.equals(repartitionStandbyTasks, that.repartitionStandbyTasks)
                && Objects.equals(timerStandbyTasks, that.timerStandbyTasks)
                && coreState == that.coreState
                && timerState == that.timerState
                && Objects.equals(restorations, that.restorations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                host,
                port,
                instanceId,
                coreActiveTasks,
                repartitionActiveTasks,
                timerActiveTasks,
                coreStandbyTasks,
                repartitionStandbyTasks,
                timerStandbyTasks,
                coreState,
                timerState,
                restorations);
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public List<ActiveTaskState> getCoreActiveTasks() {
        return this.coreActiveTasks;
    }

    public List<ActiveTaskState> getRepartitionActiveTasks() {
        return this.repartitionActiveTasks;
    }

    public List<ActiveTaskState> getTimerActiveTasks() {
        return this.timerActiveTasks;
    }

    public List<StandbyTaskState> getCoreStandbyTasks() {
        return this.coreStandbyTasks;
    }

    public List<StandbyTaskState> getRepartitionStandbyTasks() {
        return this.repartitionStandbyTasks;
    }

    public List<StandbyTaskState> getTimerStandbyTasks() {
        return this.timerStandbyTasks;
    }

    public State getCoreState() {
        return this.coreState;
    }

    public State getTimerState() {
        return this.timerState;
    }

    public List<InProgressRestoration> getRestorations() {
        return this.restorations;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

    public void setCoreActiveTasks(final List<ActiveTaskState> coreActiveTasks) {
        this.coreActiveTasks = coreActiveTasks;
    }

    public void setRepartitionActiveTasks(final List<ActiveTaskState> repartitionActiveTasks) {
        this.repartitionActiveTasks = repartitionActiveTasks;
    }

    public void setTimerActiveTasks(final List<ActiveTaskState> timerActiveTasks) {
        this.timerActiveTasks = timerActiveTasks;
    }

    public void setCoreStandbyTasks(final List<StandbyTaskState> coreStandbyTasks) {
        this.coreStandbyTasks = coreStandbyTasks;
    }

    public void setRepartitionStandbyTasks(final List<StandbyTaskState> repartitionStandbyTasks) {
        this.repartitionStandbyTasks = repartitionStandbyTasks;
    }

    public void setTimerStandbyTasks(final List<StandbyTaskState> timerStandbyTasks) {
        this.timerStandbyTasks = timerStandbyTasks;
    }

    public void setCoreState(final State coreState) {
        this.coreState = coreState;
    }

    public void setTimerState(final State timerState) {
        this.timerState = timerState;
    }

    public void setRestorations(final List<InProgressRestoration> restorations) {
        this.restorations = restorations;
    }
}
