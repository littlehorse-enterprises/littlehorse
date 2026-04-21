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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ServerHealthState)) return false;
        final ServerHealthState other = (ServerHealthState) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPort() != other.getPort()) return false;
        final Object this$host = this.getHost();
        final Object other$host = other.getHost();
        if (this$host == null ? other$host != null : !this$host.equals(other$host)) return false;
        final Object this$instanceId = this.getInstanceId();
        final Object other$instanceId = other.getInstanceId();
        if (this$instanceId == null ? other$instanceId != null : !this$instanceId.equals(other$instanceId))
            return false;
        final Object this$coreActiveTasks = this.getCoreActiveTasks();
        final Object other$coreActiveTasks = other.getCoreActiveTasks();
        if (this$coreActiveTasks == null
                ? other$coreActiveTasks != null
                : !this$coreActiveTasks.equals(other$coreActiveTasks)) return false;
        final Object this$repartitionActiveTasks = this.getRepartitionActiveTasks();
        final Object other$repartitionActiveTasks = other.getRepartitionActiveTasks();
        if (this$repartitionActiveTasks == null
                ? other$repartitionActiveTasks != null
                : !this$repartitionActiveTasks.equals(other$repartitionActiveTasks)) return false;
        final Object this$timerActiveTasks = this.getTimerActiveTasks();
        final Object other$timerActiveTasks = other.getTimerActiveTasks();
        if (this$timerActiveTasks == null
                ? other$timerActiveTasks != null
                : !this$timerActiveTasks.equals(other$timerActiveTasks)) return false;
        final Object this$coreStandbyTasks = this.getCoreStandbyTasks();
        final Object other$coreStandbyTasks = other.getCoreStandbyTasks();
        if (this$coreStandbyTasks == null
                ? other$coreStandbyTasks != null
                : !this$coreStandbyTasks.equals(other$coreStandbyTasks)) return false;
        final Object this$repartitionStandbyTasks = this.getRepartitionStandbyTasks();
        final Object other$repartitionStandbyTasks = other.getRepartitionStandbyTasks();
        if (this$repartitionStandbyTasks == null
                ? other$repartitionStandbyTasks != null
                : !this$repartitionStandbyTasks.equals(other$repartitionStandbyTasks)) return false;
        final Object this$timerStandbyTasks = this.getTimerStandbyTasks();
        final Object other$timerStandbyTasks = other.getTimerStandbyTasks();
        if (this$timerStandbyTasks == null
                ? other$timerStandbyTasks != null
                : !this$timerStandbyTasks.equals(other$timerStandbyTasks)) return false;
        final Object this$coreState = this.getCoreState();
        final Object other$coreState = other.getCoreState();
        if (this$coreState == null ? other$coreState != null : !this$coreState.equals(other$coreState)) return false;
        final Object this$timerState = this.getTimerState();
        final Object other$timerState = other.getTimerState();
        if (this$timerState == null ? other$timerState != null : !this$timerState.equals(other$timerState))
            return false;
        final Object this$restorations = this.getRestorations();
        final Object other$restorations = other.getRestorations();
        if (this$restorations == null ? other$restorations != null : !this$restorations.equals(other$restorations))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ServerHealthState;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPort();
        final Object $host = this.getHost();
        result = result * PRIME + ($host == null ? 43 : $host.hashCode());
        final Object $instanceId = this.getInstanceId();
        result = result * PRIME + ($instanceId == null ? 43 : $instanceId.hashCode());
        final Object $coreActiveTasks = this.getCoreActiveTasks();
        result = result * PRIME + ($coreActiveTasks == null ? 43 : $coreActiveTasks.hashCode());
        final Object $repartitionActiveTasks = this.getRepartitionActiveTasks();
        result = result * PRIME + ($repartitionActiveTasks == null ? 43 : $repartitionActiveTasks.hashCode());
        final Object $timerActiveTasks = this.getTimerActiveTasks();
        result = result * PRIME + ($timerActiveTasks == null ? 43 : $timerActiveTasks.hashCode());
        final Object $coreStandbyTasks = this.getCoreStandbyTasks();
        result = result * PRIME + ($coreStandbyTasks == null ? 43 : $coreStandbyTasks.hashCode());
        final Object $repartitionStandbyTasks = this.getRepartitionStandbyTasks();
        result = result * PRIME + ($repartitionStandbyTasks == null ? 43 : $repartitionStandbyTasks.hashCode());
        final Object $timerStandbyTasks = this.getTimerStandbyTasks();
        result = result * PRIME + ($timerStandbyTasks == null ? 43 : $timerStandbyTasks.hashCode());
        final Object $coreState = this.getCoreState();
        result = result * PRIME + ($coreState == null ? 43 : $coreState.hashCode());
        final Object $timerState = this.getTimerState();
        result = result * PRIME + ($timerState == null ? 43 : $timerState.hashCode());
        final Object $restorations = this.getRestorations();
        result = result * PRIME + ($restorations == null ? 43 : $restorations.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ServerHealthState(host=" + this.getHost() + ", port=" + this.getPort() + ", instanceId="
                + this.getInstanceId() + ", coreActiveTasks=" + this.getCoreActiveTasks() + ", repartitionActiveTasks="
                + this.getRepartitionActiveTasks() + ", timerActiveTasks=" + this.getTimerActiveTasks()
                + ", coreStandbyTasks=" + this.getCoreStandbyTasks() + ", repartitionStandbyTasks="
                + this.getRepartitionStandbyTasks() + ", timerStandbyTasks=" + this.getTimerStandbyTasks()
                + ", coreState=" + this.getCoreState() + ", timerState=" + this.getTimerState() + ", restorations="
                + this.getRestorations() + ")";
    }
}
